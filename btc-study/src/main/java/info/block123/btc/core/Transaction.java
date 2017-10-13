package info.block123.btc.core;

import java.math.BigInteger;
import java.util.Vector;

/**
 * btc交易
 * @author v2future
 *
 */
public class Transaction {

	private int vesion;
	private Vector<TxIn> txInVector;
	private Vector<TxOut> txOutVector;
	
	//----------------------------------------------
	/**交易的hash值**/
	private byte[] hash;
	
	public byte[] getHash() {
		/*
	        if (hash == null) {
	            byte[] bits = bitcoinSerialize();
	            hash = new Sha256Hash(reverseBytes(doubleDigest(bits)));
	        }
	        return hash;
	    */
		return null;
	}
	
	public BigInteger getValueSentToMe(Wallet wallet, boolean includeSpent) {
        //maybeParse();
        // This is tested in WalletTest.
        BigInteger v = BigInteger.ZERO;
        /*
        for (TxOut o : txOutVector) {
            if (!o.isMine(wallet)) continue;
            if (!includeSpent && !o.isAvailableForSpending()) continue;
            v = v.add(o.getValue());
        }
        */
        return v;
    }
	 
	
	public int getVesion() {
		return vesion;
	}
	public void setVesion(int vesion) {
		this.vesion = vesion;
	}
	public Vector<TxIn> getTxInVector() {
		return txInVector;
	}
	public void setTxInVector(Vector<TxIn> txInVector) {
		this.txInVector = txInVector;
	}
	public Vector<TxOut> getTxOutVector() {
		return txOutVector;
	}
	public void setTxOutVector(Vector<TxOut> txOutVector) {
		this.txOutVector = txOutVector;
	}
	
}
