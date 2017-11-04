package info.block123.btc.core;

/**
 * tx的输入
 * @author v2future
 *
 */
public class TxIn {
	
	public static final long NO_SEQUENCE = 0xFFFFFFFFL;
	
	//重新加入交易池时，增加序列号
    private long sequence;
    
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
	
	public boolean hasSequence() {
        return sequence != NO_SEQUENCE;
    }
	
	public long getSequenceNumber() {
        return sequence;
    }
	
	public void setSequenceNumber(long sequence) {
		this.sequence = sequence;
	}
	
}
