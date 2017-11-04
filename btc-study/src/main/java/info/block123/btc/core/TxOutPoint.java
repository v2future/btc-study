package info.block123.btc.core;

/**
 * 输出交易信息
 * @author v2future
 *
 */
public class TxOutPoint {
	
	/**交易的hash值**/
	private String txHash;
	private long index;
	
	//虚拟属性
	private Transaction fromTx;
	public String getTxHash() {
		return txHash;
	}
	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}
	public long getIndex() {
		return index;
	}
	public void setIndex(long index) {
		this.index = index;
	}
	
	
	//-------------------------------
	public TxOut getConnectedOutput() {
        if (fromTx == null) return null;
        return fromTx.getTxOutVector().get((int) index);
    }

    public byte[] getConnectedPubKeyScript() {
        return getConnectedOutput().getScriptBytes();
    }

    public byte[] getConnectedPubKeyHash() {
        return getConnectedOutput().getScriptPubKey().getPubKeyHash();
    }

}
