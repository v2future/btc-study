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
}
