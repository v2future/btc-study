package info.block123.btc.core;

/**
 * tx的输入
 * @author v2future
 *
 */
public class TxIn {
	
	private TxOutPoint txOutPoint;
	private byte[] scriptBytes;
	
	public TxOutPoint getTxOutPoint() {
		return txOutPoint;
	}
	public void setTxOutPoint(TxOutPoint txOutPoint) {
		this.txOutPoint = txOutPoint;
	}
	public byte[] getScriptBytes() {
		return scriptBytes;
	}
	public void setScriptBytes(byte[] scriptBytes) {
		this.scriptBytes = scriptBytes;
	}
	
}
