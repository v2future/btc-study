package info.block123.btc.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.spongycastle.crypto.params.KeyParameter;

import info.block123.btc.core.TransactionConfidence.ConfidenceType;
import info.block123.btc.kit.BtcKit;
import info.block123.btc.kit.Utils;

/**
 * btc交易
 * @author v2future
 *
 */
public class Transaction {

	private int vesion;
	private List<TxIn> txInVector;
	private List<TxOut> txOutVector;
	private long lockTime;
	private TransactionConfidence confidence;	//交易确认信息
	public static final int UNKNOWN_LENGTH = Integer.MIN_VALUE;
	protected transient int length = UNKNOWN_LENGTH;
	//交易原始数据
	private TxData txData = null;
    private Date updatedAt;
	//----------------------------------------------
	public static final int LOCKTIME_THRESHOLD = 500000000;
	public static final int MAX_STANDARD_TX_SIZE = 100 * 1024;
	/**交易的hash值**/
	private byte[] hash;
	
	/**
     * These constants are a part of a scriptSig signature on the inputs. They define the details of how a
     * transaction can be redeemed, specifically, they control how the hash of the transaction is calculated.
     * <p/>
     * In the official client, this enum also has another flag, SIGHASH_ANYONECANPAY. In this implementation,
     * that's kept separate. Only SIGHASH_ALL is actually used in the official client today. The other flags
     * exist to allow for distributed contracts.
     */
    public enum SigHash {
        ALL,         // 1
        NONE,        // 2
        SINGLE,      // 3
    }
	
	public void setNull () {
		this.vesion = 1;
		txInVector = new Vector<TxIn>();
		txOutVector = new Vector<TxOut>();
	}
	
	public Transaction () {
		setNull();
	}
	
	public boolean isNull () {
		return txInVector.isEmpty() || txOutVector.isEmpty();
	}
	
	public boolean isMine (Wallet wallet) {
		for ( TxOut txOut : txOutVector) {
			if (txOut.isMine(wallet))
				return true;
		}
		return false;
	}
	
	public void setUpdateTime(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
	
	public Sha256Hash getHash() {
		/*
	        if (hash == null) {
	            byte[] bits = bitcoinSerialize();
	            hash = new Sha256Hash(reverseBytes(doubleDigest(bits)));
	        }
	        return hash;
	    */
		return null;
	}
	
	/**
	 * 如果该交易被确认返回true，将会放到区块中。
	 * 非确认的交易不会由矿工打包，将会替换新的version
	 * @param height
	 * @param blockTimeSeconds
	 * @return
	 */
    public boolean isFinal(int height, long blockTimeSeconds) {
        if (lockTime == 0)
            return true;
        if (lockTime < (lockTime < LOCKTIME_THRESHOLD ? height : blockTimeSeconds))
            return true;
        for (TxIn in : txInVector)
        	if (in.hasSequence())
        		return false;
        return true;
    }
    
    public boolean isCoinBase () {
    	return txInVector.size() == 1 && txInVector.get(0).getTxOutPoint() == null;
    }
    
    /**
     * 判断交易是否有效
     * @return
     */
    public boolean checkTransaction () {
    	if ( this.txInVector.isEmpty() || this.txOutVector.isEmpty() ) {
    		throw new BtcException("输入交易和输出交易不可以为空!");
    	}
    	for (TxOut out : txOutVector) {
    		if ( out.getValue().compareTo( BigInteger.ZERO ) < 0 ) {
    			throw new BtcException("输出交易value小于0!");
    		}
    	}
		if (isCoinBase()) {
			//TODO
			// if (vin[0].scriptSig.size() < 2 || vin[0].scriptSig.size() > 100)
			// return error("CTransaction::CheckTransaction() : coinbase script
			// size");
		} else {
			for ( TxIn in : txInVector) {
				if ( in.getTxOutPoint() == null)
					throw new BtcException("TxIn.outpoint为null!");
			}
			
		}
    	return true;
    }
	
	/**
	 * 给交易创建交易输出
	 * @param value
	 * @param address
	 */
    public void addOutput(BigInteger value, Address address) {
        addOutput(new TxOut(this, value, address));
    }
    
    public BigInteger getValueOut () {
    	BigInteger v = BigInteger.ZERO;
    	 for (TxOut o : txOutVector) {
    		 if ( o.getValue().compareTo(BigInteger.ZERO) < 0) {
    			 throw new BtcException("输出交易value小于0!");
    		 }
    		 v.add(o.getValue());
    	 }
    	return v;
    }
    
    /**
     * 计算交易最小费用
     * @return
     */
    public BigInteger getMinFee () {
    	//TODO
    	return null;
    }
    
	
	//TODO
	public void addOutput(TxOut to) {
		//unCache();
		to.setTx(this); 
		txOutVector.add(to);
	}
	
	public synchronized TransactionConfidence getConfidence() {
		if (confidence == null) {
			confidence = new TransactionConfidence(this);
		}
		return confidence;
	}
	
    /**
     * 交易签名
     * @param hashType
     * @param wallet
     */
    public synchronized void signInputs(SigHash hashType, Wallet wallet) {
        signInputs(hashType, wallet, null);
    }

	/**
	 * 交易签名
	 * SignatureHash in script.cpp
	 * @param hashType
	 * @param wallet
	 * @param aesKey
	 */
    public synchronized void signInputs(SigHash hashType, Wallet wallet, KeyParameter aesKey) {
    	if ( this.txInVector.isEmpty() || this.txOutVector.isEmpty() ) {
    		throw new BtcException("输入交易和输出交易不可以为空!");
    	}
    	if (hashType != SigHash.ALL) {
    		throw new BtcException("只支持ALL模式!");
    	}
    	
    	byte[][] signs = new byte[this.txInVector.size()][];
    	ECKey[] signKeys = new ECKey[this.txInVector.size()];
    	for ( int i=0; i<this.txInVector.size(); i++) {
    		TxIn input = txInVector.get(i);
    		ECKey key = wallet.getKey();
    		// Keep the key around for the script creation step below.
            signKeys[i] = key;
            //anyoneCanPay默认为false
            boolean anyoneCanPay = false;
            byte[] connectedPubKeyScript = input.getTxOutPoint().getConnectedPubKeyScript();
            byte[] hash = hashTransactionForSignature(i, connectedPubKeyScript, hashType, anyoneCanPay);
            //开始签名
            try {
                // Usually 71-73 bytes.
                ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(73);
                bos.write(key.sign(hash, aesKey).encodeToDER());
                bos.write((hashType.ordinal() + 1) | (anyoneCanPay ? 0x80 : 0));
                signs[i] = bos.toByteArray();
                bos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);  // Cannot happen.
            }
    		
    	}
    	//生成签名脚本
        for (int i = 0; i < txInVector.size(); i++) {
            TxIn input = txInVector.get(i);
            ECKey key = signKeys[i];
            Script scriptPubKey = input.getTxOutPoint().getConnectedOutput().getScriptPubKey();
            if (scriptPubKey.isSentToAddress()) {
                input.setScriptBytes(Script.createInputScript(signs[i], key.getPubKey()));
            } else if (scriptPubKey.isSentToRawPubKey()) {
                input.setScriptBytes(Script.createInputScript(signs[i]));
            } else {
            	//impossible
                throw new RuntimeException("Do not understand script type: " + scriptPubKey);
            }
        }
    }
    
    //签名相关
	public synchronized byte[] hashTransactionForSignature(int inputIndex, 
			byte[] connectedScript, 
			SigHash type,
			boolean anyoneCanPay) throws BtcException {
		return hashTransactionForSignature(inputIndex, connectedScript,
				(byte) ((type.ordinal() + 1) | (anyoneCanPay ? 0x80 : 0x00)));
	}

	/**
	 * This is required for signatures which use a sigHashType which cannot be
	 * represented using SigHash and anyoneCanPay See transaction
	 * c99c49da4c38af669dea436d3e73780dfdb6c1ecf9958baa52960e8baee30e73, which
	 * has sigHashType 0
	 */
	public synchronized byte[] hashTransactionForSignature(int inputIndex, 
			byte[] connectedScript, 
			byte sigHashType)
			throws BtcException {
		try {
			byte[][] inputScripts = new byte[this.txInVector.size()][];
			long[] inputSequenceNumbers = new long[this.txInVector.size()];
			for (int i = 0; i < txInVector.size(); i++) {
				inputScripts[i] = txInVector.get(i).getScriptBytes();
				inputSequenceNumbers[i] = txInVector.get(i).getSequenceNumber();
				txInVector.get(i).setScriptBytes( new byte[0] );
			}
			connectedScript = Script.removeAllInstancesOfOp(connectedScript, Script.OP_CODESEPARATOR);
			TxIn input = txInVector.get(inputIndex);
			input.setScriptBytes(connectedScript);
			List<TxOut> outputs = this.txOutVector;
			if ((sigHashType & 0x1f) == (SigHash.NONE.ordinal() + 1)) {
				// SIGHASH_NONE means no outputs are signed at all - the
				// signature is effectively for a "blank cheque".
				this.txOutVector = new ArrayList<TxOut>();
				for (int i=0; i< this.txInVector.size(); i++) {
					if (i != inputIndex)
						this.txInVector.get(i).setSequenceNumber(0L);
				} 
			}  else if ((sigHashType & 0x1f) == (SigHash.SINGLE.ordinal() + 1)) {
				// SIGHASH_SINGLE means only sign the output at the same index
				// as the input (ie, my output).
				if (inputIndex >= this.txOutVector.size()) {
					//错误
					for (int i = 0; i < this.txInVector.size(); i++) {
						this.txInVector.get(i).setScriptBytes(inputScripts[i]);
						this.txInVector.get(i).setSequenceNumber(inputSequenceNumbers[i]);
					}
					this.txOutVector = outputs;
					return BtcKit.hexStringToByte("0100000000000000000000000000000000000000000000000000000000000000");
				}
				// In SIGHASH_SINGLE the outputs after the matching input index
				// are deleted, and the outputs before
				// that position are "nulled out". Unintuitively, the value in a
				// "null" transaction is set to -1.
				this.txOutVector = new ArrayList<TxOut>(this.txOutVector.subList(0, inputIndex + 1));
				for (int i = 0; i < inputIndex; i++) {
					Address nullAddress = new Address(new byte[]{});
					this.txOutVector.set(i, new TxOut(this, BigInteger.valueOf(-1), nullAddress));
				}
				// The signature isn't broken by new versions of the transaction
				// issued by other parties.
				for (int i = 0; i < txInVector.size(); i++)
					if (i != inputIndex)
						txInVector.get(i).setSequenceNumber(0);
			}
			List<TxIn> inputs = this.txInVector;
			if ((sigHashType & 0x80) == 0x80) {
				this.txInVector = new ArrayList();
				this.txInVector.add(input);
			}
			ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(length == UNKNOWN_LENGTH ? 256 : length + 4);
			//bitcoinSerialize(bos);
			Utils.uint32ToByteStreamLE(0x000000ff & sigHashType, bos);
			byte[] hash = BtcKit.sha256hash160( Utils.doubleDigest(bos.toByteArray()));
			bos.close();
			this.txInVector = inputs;
			for (int i = 0; i < inputs.size(); i++) {
				inputs.get(i).setScriptBytes(inputScripts[i]);
				inputs.get(i).setSequenceNumber(inputSequenceNumbers[i]);
			}
			this.txOutVector = outputs;
			return hash;
		} catch (Exception e) {
			throw new RuntimeException(e);  // Cannot happen.
		}
		
	}
	
	
	public byte[] bitcoinSerialize() {
		 if ( this.txData == null)
			 throw new BtcException("缺失交易数据");
		 return this.txData.bitcoinSerialize();
	}
	
	 /**
	  * 是否是成熟的交易
	  * @return
	  */
    public boolean isMature() {
        if (!isCoinBase())
            return true;
        if (getConfidence().getConfidenceType() != ConfidenceType.BUILDING)
            return false;
        //return getConfidence().getDepthInBlocks() >= params.getSpendableCoinbaseDepth();
        //TODO
        return true;
    }
    
    
    /**
     * 属于自己可花费的返回false
     * @param wallet
     * @return
     */
    public boolean isEveryOwnedOutputSpent(Wallet wallet) {
        //maybeParse();
        for (TxOut output : txOutVector) {
            if (output.isAvailableForSpending() && output.isMine(wallet))
                return false;
        }
        return true;
    }
	
	
	public int getVesion() {
		return vesion;
	}
	public void setVesion(int vesion) {
		this.vesion = vesion;
	}
	public List<TxIn> getTxInVector() {
		return txInVector;
	}
	public void setTxInVector(List<TxIn> txInVector) {
		this.txInVector = txInVector;
	}
	public List<TxOut> getTxOutVector() {
		return txOutVector;
	}
	public void setTxOutVector(List<TxOut> txOutVector) {
		this.txOutVector = txOutVector;
	}
	
}
