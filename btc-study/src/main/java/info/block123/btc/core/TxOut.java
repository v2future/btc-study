package info.block123.btc.core;

import java.math.BigInteger;

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
	
	public byte[] getScriptBytes() {
		return scriptBytes;
	}

	public void setScriptBytes(byte[] scriptBytes) {
		this.scriptBytes = scriptBytes;
	}

	public BigInteger getValue() {
		return value;
	}

	public void setValue(BigInteger value) {
		this.value = value;
	}
	
}
