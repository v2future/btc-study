package info.block123.btc.core;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * tx的输出
 * A TransactionOutput message contains a scriptPubKey that controls who is able to spend its value. 
 * It is a sub-part of the Transaction message.
 * @author v2future
 *
 */
public class TxOut {
	
	/**金额**/
	private BigInteger value;
	private byte[] scriptBytes;
	private transient Script scriptPubKey;
	//归属的tx
	private Transaction tx;
	//-------------------------------------------------------
	private boolean availableForSpending;
	private TxIn spentBy;
	
	
	public TxOut() {}
	
	public TxOut(Transaction tx, BigInteger value, Address to) {
		this.tx = tx;
		this.value = value;
		this.scriptBytes = to.getHash160();
		this.availableForSpending = true;
	}
	
	/**
	 * 判断该输出交易是否属于本钱包
	 * @param wallet
	 * @return
	 */
	public boolean isMine (Wallet wallet) {
		return Arrays.equals(wallet.getHash160(), this.scriptBytes);
	}
	
	public byte[] getScriptBytes() {
		return scriptBytes;
	}

	public void setScriptBytes(byte[] scriptBytes) {
		this.scriptBytes = scriptBytes;
	}

	public BigInteger getValue() {
		return value;
	}
	
	public Script getScriptPubKey() {
        if (scriptPubKey == null) {
            scriptPubKey = new Script(scriptBytes, 0, scriptBytes.length);
        }
        return scriptPubKey;
    }
	
	public boolean isAvailableForSpending() {
		return availableForSpending;
	}
	
	public void markAsSpent(TxIn input) {
		availableForSpending = false;
		spentBy = input;
	}

	public void markAsUnspent() {
		availableForSpending = true;
		spentBy = null;
	}

	public void setValue(BigInteger value) {
		this.value = value;
	}

	public Transaction getTx() {
		return tx;
	}

	public void setTx(Transaction tx) {
		this.tx = tx;
	}
	
}
