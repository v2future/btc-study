package info.block123.btc.core;

import java.util.Map;

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
	
	enum ConnectMode {
        DISCONNECT_ON_CONFLICT,
        ABORT_ON_CONFLICT
    }
	enum ConnectionResult {
        NO_SUCH_TX,
        ALREADY_SPENT,
        SUCCESS
    }
	
    public ConnectionResult connect(Map<Sha256Hash, Transaction> transactions, ConnectMode mode) {
        Transaction tx = transactions.get(txOutPoint.getTxHash());
        if (tx == null) {
            return ConnectionResult.NO_SUCH_TX;
        }
        return connect(tx, mode);
    }

    /**
     * Connects this input to the relevant output of the referenced transaction.
     * Connecting means updating the internal pointers and spent flags. If the mode is to ABORT_ON_CONFLICT then
     * the spent output won't be changed, but the outpoint.fromTx pointer will still be updated.
     *
     * @param transaction The transaction to try.
     * @param mode   Whether to abort if there's a pre-existing connection or not.
     * @return NO_SUCH_TX if transaction is not the prevtx, ALREADY_SPENT if there was a conflict, SUCCESS if not.
     */
    public ConnectionResult connect(Transaction transaction, ConnectMode mode) {
        if (!transaction.getHash().equals(txOutPoint.getTxHash()) && 
        	mode != ConnectMode.DISCONNECT_ON_CONFLICT)
            return ConnectionResult.NO_SUCH_TX;
        
        TxOut out = transaction.getTxOutVector().get((int)txOutPoint.getIndex());
        if (!out.isAvailableForSpending()) {
            if (mode == ConnectMode.DISCONNECT_ON_CONFLICT) {
                out.markAsUnspent();
            } else if (mode == ConnectMode.ABORT_ON_CONFLICT) {
            	txOutPoint.setFromTx(out.getTx());
                return ConnectionResult.ALREADY_SPENT;
            }
        }
        connect(out);
        return ConnectionResult.SUCCESS;
    }
    
    public void connect(TxOut out) {
    	txOutPoint.setFromTx(out.getTx());
        out.markAsSpent(this);
    }
	
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
