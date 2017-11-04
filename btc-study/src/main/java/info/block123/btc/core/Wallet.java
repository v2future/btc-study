package info.block123.btc.core;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import info.block123.btc.core.Transaction.SigHash;
import info.block123.btc.core.TransactionConfidence.ConfidenceType;
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
	
	public boolean sendMoney (Address address, BigInteger value) {
		
		
		return false;
	}
	
	/**
	 * 创建交易
	 * @param to
	 * @param value
	 * @return
	 */
	public boolean createTx (Address to, BigInteger value) {
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
				return false;
			if ( selectCoins.gathered.size() == 0)
				throw new BtcException("no balance!");
			
			TransactionConfidence confidence = new TransactionConfidence(tx);
			confidence.setConfidenceType(ConfidenceType.NOT_SEEN_IN_CHAIN);
			BigInteger change = selectCoins.valueGathered.subtract(value);
			if (change.compareTo(BigInteger.ZERO) > 0) {
				Address changeAddress = null; //TODO 当前钱包地址
				tx.addOutput(new TxOut(tx, change, to));
			}
			for ( TxOut output : selectCoins.gathered) {
				tx.addOutput(output);
			}
			//签名
			tx.signInputs(SigHash.ALL, this, null);
			
			//检查交易数据大小 TODO
			int size = 0;
			
			
			tx.getConfidence().setSource(TransactionConfidence.Source.SELF);
			/**
			 * 

            // Check size.
            int size = req.tx.bitcoinSerialize().length;
            if (size > Transaction.MAX_STANDARD_TX_SIZE) {
                // TODO: Throw an exception here.
                log.error("Transaction could not be created without exceeding max size: {} vs {}", size,
                          Transaction.MAX_STANDARD_TX_SIZE);
                return false;
            }
			 */
		} finally {
			lock.unlock();
		}
		return false;
	}
	
	//TODO
	private LinkedList<TxOut> calculateSpendCandidates(boolean excludeImmatureCoinbases) {
		
		/*
        checkState(lock.isLocked());
        LinkedList<TransactionOutput> candidates = Lists.newLinkedList();
        for (Transaction tx : Iterables.concat(unspent.values(), pending.values())) {
            // Do not try and spend coinbases that were mined too recently, the protocol forbids it.
            if (excludeImmatureCoinbases && !tx.isMature()) continue;
            for (TransactionOutput output : tx.getOutputs()) {
                if (!output.isAvailableForSpending()) continue;
                if (!output.isMine(this)) continue;
                candidates.add(output);
            }
        }
        return candidates;
        */
		
		return null;
    }
	
	/*
	bool CreateTransaction(CScript scriptPubKey, int64 nValue, CWalletTx& wtxNew, int64& nFeeRequiredRet)
	{
	    nFeeRequiredRet = 0;
	    CRITICAL_BLOCK(cs_main)
	    {
	        // txdb must be opened before the mapWallet lock
	        CTxDB txdb("r");
	        CRITICAL_BLOCK(cs_mapWallet)
	        {
	            int64 nFee = nTransactionFee;
	            loop
	            {
	                wtxNew.vin.clear();
	                wtxNew.vout.clear();
	                if (nValue < 0)
	                    return false;
	                int64 nValueOut = nValue;
	                nValue += nFee;

	                // Choose coins to use
	                set<CWalletTx*> setCoins;
	                if (!SelectCoins(nValue, setCoins))
	                    return false;
	                int64 nValueIn = 0;
	                foreach(CWalletTx* pcoin, setCoins)
	                    nValueIn += pcoin->GetCredit();

	                // Fill vout[0] to the payee
	                wtxNew.vout.push_back(CTxOut(nValueOut, scriptPubKey));

	                // Fill vout[1] back to self with any change
	                if (nValueIn > nValue)
	                {
	                    /// todo: for privacy, should randomize the order of outputs,
	                    //        would also have to use a new key for the change.
	                    // Use the same key as one of the coins
	                    vector<unsigned char> vchPubKey;
	                    CTransaction& txFirst = *(*setCoins.begin());
	                    foreach(const CTxOut& txout, txFirst.vout)
	                        if (txout.IsMine())
	                            if (ExtractPubKey(txout.scriptPubKey, true, vchPubKey))
	                                break;
	                    if (vchPubKey.empty())
	                        return false;

	                    // Fill vout[1] to ourself
	                    CScript scriptPubKey;
	                    scriptPubKey << vchPubKey << OP_CHECKSIG;
	                    wtxNew.vout.push_back(CTxOut(nValueIn - nValue, scriptPubKey));
	                }

	                // Fill vin
	                foreach(CWalletTx* pcoin, setCoins)
	                    for (int nOut = 0; nOut < pcoin->vout.size(); nOut++)
	                        if (pcoin->vout[nOut].IsMine())
	                            wtxNew.vin.push_back(CTxIn(pcoin->GetHash(), nOut));

	                // Sign
	                int nIn = 0;
	                foreach(CWalletTx* pcoin, setCoins)
	                    for (int nOut = 0; nOut < pcoin->vout.size(); nOut++)
	                        if (pcoin->vout[nOut].IsMine())
	                            SignSignature(*pcoin, wtxNew, nIn++);

	                // Check that enough fee is included
	                if (nFee < wtxNew.GetMinFee(true))
	                {
	                    nFee = nFeeRequiredRet = wtxNew.GetMinFee(true);
	                    continue;
	                }

	                // Fill vtxPrev by copying from previous transactions vtxPrev
	                wtxNew.AddSupportingTransactions(txdb);
	                wtxNew.fTimeReceivedIsTxTime = true;

	                break;
	            }
	        }
	    }
	    return true;
	}
	*/
	
	
	//TODO
	private boolean createTransaction () {
		
		return false;
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
	
	
	
}
