package info.block123.btc.core;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import info.block123.btc.core.Transaction.SigHash;
import info.block123.btc.core.TransactionConfidence.ConfidenceType;
import info.block123.btc.core.WalletTx.Pool;
import info.block123.btc.kit.BtcConstant;
import info.block123.btc.kit.BtcKit;

/**
 * btc钱包
 * 简单模式：只有一个密钥对
 * @author v2future
 * 
 */
public class Wallet implements Serializable {
	
	protected final ReentrantLock lock = new ReentrantLock();
	
	private ECKey key;
	
	/**
	 * 未确认的UTXO
	 */
	final Map<Sha256Hash, Transaction> pending;
	
	/**
	 * 未花费的UTXO
	 */
    final Map<Sha256Hash, Transaction> unspent;

    /**
     * 已花费的UTXO
     */
    final Map<Sha256Hash, Transaction> spent;
	
    /**
     * 创建空钱包
     */
    public Wallet() {
        this(null);
    }

    /**
     * 创建带私钥的钱包
     * Create a wallet with a keyCrypter to use in encrypting and decrypting keys.
     */
    public Wallet(BigInteger privKey) {
        key = new ECKey(privKey);
        unspent = new HashMap<Sha256Hash, Transaction>();
        spent = new HashMap<Sha256Hash, Transaction>();
        pending = new HashMap<Sha256Hash, Transaction>();
    }
	
	public ECKey getKey() {
		return key;
	}

	public void setKey(ECKey key) {
		this.key = key;
	}
	
	/**
	 * 返回公钥的hash160字节
	 * @return
	 */
	public byte[] getHash160 () {
		return BtcKit.sha256hash160(key.getPubKey());
	}
	
	/**
	 * 发送交易
	 * @param address
	 * @param value
	 * @return
	 */
	public boolean sendMoney (Address to, BigInteger value) {
		lock.lock();
		try {
			//1.生成离线交易
			Transaction tx = createTx(to, value);
			if ( tx == null)
				return false;		//余额不足
			//2.广播
			commitTx(tx);
		} finally {
			lock.unlock();
		}
		return true;
	}
	
	public void commitTx(Transaction tx) {
		BigInteger balance = getBalance();
        tx.setUpdateTime(new Date());
        //更新钱包内UTXO信息
        updateForSpends(tx, false);
        //添加到交易池中
        addWalletTransaction(Pool.PENDING, tx);
        
        /*
		// Event listeners may re-enter so we cannot make assumptions about
		// wallet state after this loop completes.
		try {
			BigInteger valueSentFromMe = tx.getValueSentFromMe(this);
			BigInteger valueSentToMe = tx.getValueSentToMe(this);
			BigInteger newBalance = balance.add(valueSentToMe).subtract(valueSentFromMe);
			if (valueSentToMe.compareTo(BigInteger.ZERO) > 0)
				invokeOnCoinsReceived(tx, balance, newBalance);
			if (valueSentFromMe.compareTo(BigInteger.ZERO) > 0)
				invokeOnCoinsSent(tx, balance, newBalance);

			invokeOnWalletChanged();
		} catch (ScriptException e) {
			// Cannot happen as we just created this transaction ourselves.
			throw new RuntimeException(e);
		}
		// queueAutoSave();
        */
    }
	
	/**
	 * 创建交易
	 * @param to
	 * @param value
	 * @return
	 */
	public Transaction createTx (Address to, BigInteger value) {
		Transaction tx = new Transaction();
		tx.addOutput(value, to);
		//检查
		lock.lock();
		try {
			BigInteger fee = BigInteger.ZERO;
			BigInteger totalValue = BigInteger.ZERO;
			totalValue = value.add(fee);
			
			LinkedList<TxOut> candidates = calculateSpendCandidates(true);
			SelectCoins selectCoins = selectCoin(totalValue, candidates);
			if ( selectCoins.valueGathered.compareTo( totalValue) < 0)
				return null;
			if ( selectCoins.gathered.size() == 0)
				throw new BtcException("no balance!");
			
			TransactionConfidence confidence = new TransactionConfidence(tx);
			confidence.setConfidenceType(ConfidenceType.NOT_SEEN_IN_CHAIN);
			BigInteger change = selectCoins.valueGathered.subtract(value);
			if (change.compareTo(BigInteger.ZERO) > 0) {
				Address changeAddress = new Address(this.getHash160());
				tx.addOutput(new TxOut(tx, change, to));
			}
			for ( TxOut output : selectCoins.gathered) {
				tx.addOutput(output);
			}
			//签名
			tx.signInputs(SigHash.ALL, this, null);
			//检查交易数据大小
			int size = tx.bitcoinSerialize().length;
			if ( size > Transaction.MAX_STANDARD_TX_SIZE ) {
				throw new BtcException("数据包大小超限!");
			}
			tx.getConfidence().setSource(TransactionConfidence.Source.SELF);
		} finally {
			lock.unlock();
		}
		return tx;
	}
	
	private LinkedList<TxOut> calculateSpendCandidates(boolean excludeImmatureCoinbases) {
        LinkedList<TxOut> candidates = new LinkedList();
        List<Transaction> valueList = new ArrayList();
        valueList.addAll(unspent.values());
        valueList.addAll(pending.values());
        for (Transaction tx : valueList) {
        	// Do not try and spend coinbases that were mined too recently, the protocol forbids it.
            if (excludeImmatureCoinbases && !tx.isMature()) continue;
            for (TxOut output : tx.getTxOutVector()) { 
                if (!output.isAvailableForSpending()) continue;
                if (!output.isMine(this)) continue;
                candidates.add(output);
            }
        }
        return candidates;
    }
	
	public class SelectCoins {
        public BigInteger valueGathered;
        public List<TxOut> gathered;
        public SelectCoins(BigInteger valueGathered, List<TxOut> gathered) {
            this.valueGathered = valueGathered;
            this.gathered = gathered;
        }
    }
	
	public SelectCoins selectCoin (BigInteger value, List<TxOut> candidates) {
		long target = value.longValue();
        long total = 0;
        LinkedList<TxOut> selected = new LinkedList<TxOut>();
        //TODO 未添加排序
        for (TxOut output : selected) {
            if (total >= target) break;
            // Only pick chain-included transactions, or transactions that are ours and pending.
            if (!shouldSelect(output.getTx())) 
            	continue;
            selected.add(output);
            total += output.getValue().longValue();
        }
        return new SelectCoins(BigInteger.valueOf(total), selected);
	}
	
	/**
	 * 检测tx状态
	 * @param tx
	 * @return
	 */
	private static boolean shouldSelect(Transaction tx) {
        TransactionConfidence confidence = tx.getConfidence();
        ConfidenceType type = confidence.getConfidenceType();
        boolean pending = type.equals(ConfidenceType.NOT_SEEN_IN_CHAIN) ||
                		  type.equals(ConfidenceType.NOT_IN_BEST_CHAIN);
        boolean confirmed = type.equals(ConfidenceType.BUILDING);
        if (!confirmed) {
            // If the transaction is still pending ...
            if (!pending) return false;
            // And it was created by us ...
            if (!confidence.getSource().equals(TransactionConfidence.Source.SELF)) return false;
            // And it's been seen by the network and propagated ...
            if (confidence.numBroadcastPeers() <= 1) return false;
            // Then it's OK to select.
        }
        return true;
    }
	
	//----------------------------------------------------------------
	//余额信息
	//----------------------------------------------------------------
	public enum BalanceType {
        ESTIMATED,	//预估有效
        AVAILABLE
    }
	/**
	 * 获取余额信息
	 * @return
	 */
	public BigInteger getBalance() {
        return getBalance(BalanceType.AVAILABLE);
    }

    public BigInteger getBalance(BalanceType balanceType) {
        lock.lock();
        try {
            if (balanceType == BalanceType.AVAILABLE) {
                return getAvaibleBalance();
            } else if (balanceType == BalanceType.ESTIMATED) {
                LinkedList<TxOut> all = calculateSpendCandidates(false);
                BigInteger value = BigInteger.ZERO;
                for (TxOut out : all) value = value.add(out.getValue());
                return value;
            } 
            return null;
        } finally {
            lock.unlock();
        }
    }
    
    private BigInteger getAvaibleBalance() {
        lock.lock();
        try {
            long total = 0;
        	List<TxOut> candidates = calculateSpendCandidates(true);
        	//排序 根据确认情况及高度排序
        	List<TxOut> sortedOutputs = new ArrayList(candidates);
        	Collections.sort(sortedOutputs, new Comparator<TxOut>() {
                public int compare(TxOut a, TxOut b) {
                    int depth1 = 0;
                    int depth2 = 0;
                    TransactionConfidence conf1 = a.getTx().getConfidence();
                    TransactionConfidence conf2 = b.getTx().getConfidence();
                    if (conf1.getConfidenceType() == ConfidenceType.BUILDING) depth1 = conf1.getDepthInBlocks();
                    if (conf2.getConfidenceType() == ConfidenceType.BUILDING) depth2 = conf2.getDepthInBlocks();
                    if (depth1 < depth2)
                        return 1;
                    else if (depth1 > depth2)
                        return -1;
                    // Their depths are equal (possibly pending) so sort by hash to ensure a total ordering.
                    BigInteger aHash = a.getTx().getHash().toBigInteger();
                    BigInteger bHash = b.getTx().getHash().toBigInteger();
                    return aHash.compareTo(bHash);
                }
            });
        	for (TxOut output : sortedOutputs) {
                 if (total >= BtcConstant.MAX_MONEY.longValue()) break;
                 // Only pick chain-included transactions, or transactions that are ours and pending.
                 if (!shouldSelect(output.getTx())) continue;
                 total += output.getValue().longValue();
            }
        	return BigInteger.valueOf(total);
        } finally {
            lock.unlock();
        }
    }
    
    //更新交易数据
    private void updateForSpends(Transaction tx, boolean fromChain) {
        for (TxIn input : tx.getTxInVector()) {
        	TxIn.ConnectionResult result = input.connect(unspent, TxIn.ConnectMode.ABORT_ON_CONFLICT);
            if (result == TxIn.ConnectionResult.NO_SUCH_TX) {
                // Not found in the unspent map. Try again with the spent map.
                result = input.connect(spent, TxIn.ConnectMode.ABORT_ON_CONFLICT);
                if (result == TxIn.ConnectionResult.NO_SUCH_TX) {
                    // Not found in the unspent and spent maps. Try again with the pending map.
                    result = input.connect(pending, TxIn.ConnectMode.ABORT_ON_CONFLICT);
                    if (result == TxIn.ConnectionResult.NO_SUCH_TX) {
                        // Doesn't spend any of our outputs or is coinbase.
                        continue;
                    }
                }
            } 
            
            if (result == TxIn.ConnectionResult.ALREADY_SPENT) {
                //null
            } else if (result == TxIn.ConnectionResult.SUCCESS) {
                // Otherwise we saw a transaction spend our coins, but we didn't try and spend them ourselves yet.
                // The outputs are already marked as spent by the connect call above, so check if there are any more for
                // us to use. Move if not.
                Transaction connected = input.getTxOutPoint().getFromTx();
                maybeMovePool(connected, "prevtx");
            }
        }
        // Now check each output and see if there is a pending transaction which spends it. This shouldn't normally
        // ever occur because we expect transactions to arrive in temporal order, but this assumption can be violated
        // when we receive a pending transaction from the mempool that is relevant to us, which spends coins that we
        // didn't see arrive on the best chain yet. For instance, because of a chain replay or because of our keys were
        // used by another wallet somewhere else.
        
        /*
        if (fromChain) {
            for (Transaction pendingTx : pending.values()) {
                for (TxIn input : pendingTx.getInputs()) {
                	TxIn.ConnectionResult result = input.connect(tx, TransactionInput.ConnectMode.ABORT_ON_CONFLICT);
                    // This TX is supposed to have just appeared on the best chain, so its outputs should not be marked
                    // as spent yet. If they are, it means something is happening out of order.
                    checkState(result != TxIn.ConnectionResult.ALREADY_SPENT);
                    if (result == TxIn.ConnectionResult.SUCCESS) {
                        log.info("Connected pending tx input {}:{}",
                                pendingTx.getHashAsString(), pendingTx.getInputs().indexOf(input));
                    }
                }
                // If the transactions outputs are now all spent, it will be moved into the spent pool by the
                // processTxFromBestChain method.
            }
        }
        */
    }
	
    private void maybeMovePool(Transaction tx, String context) {
        if (tx.isEveryOwnedOutputSpent(this)) {
            // There's nothing left I can spend in this transaction.
            if (unspent.remove(tx.getHash()) != null) {
                spent.put(tx.getHash(), tx);
            }
        } else {
            if (spent.remove(tx.getHash()) != null) {
                unspent.put(tx.getHash(), tx);
            }
        }
    }
    
    public void addWalletTransaction(WalletTx wtx) {
        lock.lock();
        try {
            addWalletTransaction(wtx.getPool(), wtx.getTransaction());
        } finally {
            lock.unlock();
        }
    }

    private void addWalletTransaction(Pool pool, Transaction tx) {
        switch (pool) {
        case UNSPENT:
            unspent.put(tx.getHash(), tx);
            break;
        case SPENT:
            spent.put(tx.getHash(), tx);
            break;
        case PENDING:
            pending.put(tx.getHash(), tx);
            break;
        //case DEAD:
        //    dead.put(tx.getHash(), tx);
        //    break;
        //case INACTIVE:
        //    inactive.put(tx.getHash(), tx);
        //    break;
        //case PENDING_INACTIVE:
        //    pending.put(tx.getHash(), tx);
        //    inactive.put(tx.getHash(), tx);
        //    break;
        default:
            throw new RuntimeException("Unknown wallet transaction type " + pool);
        }
        // This is safe even if the listener has been added before, as TransactionConfidence ignores duplicate
        // registration requests. That makes the code in the wallet simpler.
        //tx.getConfidence().addEventListener(txConfidenceListener);
    }
}
