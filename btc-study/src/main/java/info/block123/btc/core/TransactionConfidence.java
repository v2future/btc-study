package info.block123.btc.core;

import java.math.BigInteger;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransactionConfidence {

    private Transaction transaction;
    
    /**
     * The peers that have announced the transaction to us. Network nodes don't have stable identities, so we use
     * IP address as an approximation. It's obviously vulnerable to being gamed if we allow arbitrary people to connect
     * to us, so only peers we explicitly connected to should go here.
     */
    private CopyOnWriteArrayList broadcastBy;

    // The depth of the transaction on the best chain in blocks. An unconfirmed block has depth 0.
    private int depth;
    // The cumulative work done for the blocks that bury this transaction.
    private BigInteger workDone = BigInteger.ZERO;

    public enum ConfidenceType {
        BUILDING(1),
        NOT_SEEN_IN_CHAIN(2),
        NOT_IN_BEST_CHAIN(3),
        DEAD(4),
        UNKNOWN(0);
        
        private int value;
        ConfidenceType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }

        public static ConfidenceType valueOf(int value) {
            switch (value) {
            case 0: return UNKNOWN;
            case 1: return BUILDING;
            case 2: return NOT_SEEN_IN_CHAIN;
            case 3: return NOT_IN_BEST_CHAIN;
            case 4: return DEAD;
            default: return null;
            }
        }

    }

    private ConfidenceType confidenceType = ConfidenceType.UNKNOWN;
    // The transaction that double spent this one, if any.
    private Transaction overridingTransaction;

    /**
     * Information about where the transaction was first seen (network, sent direct from peer, created by ourselves).
     * Useful for risk analyzing pending transactions. Probably not that useful after a tx is included in the chain,
     * unless re-org double spends start happening frequently.
     */
    public enum Source {
        UNKNOWN,
        NETWORK,
        SELF
    }
    private Source source = Source.UNKNOWN;

    public TransactionConfidence(Transaction tx) {
        transaction = tx;
    }

    /**
     * Returns a general statement of the level of confidence you can have in this transaction.
     */
    public synchronized ConfidenceType getConfidenceType() {
        return confidenceType;
    }

    /**
     * Called by other objects in the system, like a {@link Wallet}, when new information about the confidence of a 
     * transaction becomes available.
     */
    public void setConfidenceType(ConfidenceType confidenceType) {
        // Don't inform the event listeners if the confidence didn't really change.
        synchronized (this) {
            if (confidenceType == this.confidenceType)
                return;
            this.confidenceType = confidenceType;
        }
    }
    
    public int numBroadcastPeers() {
        return broadcastBy.size();
    }


    public synchronized int getDepthInBlocks() {
        if (getConfidenceType() != ConfidenceType.BUILDING) {
            throw new IllegalStateException("Confidence type is not BUILDING");
        }
        return depth;
    }

    /*
     * Set the depth in blocks. Having one block confirmation is a depth of one.
     */
    public synchronized void setDepthInBlocks(int depth) {
        this.depth = depth;
    }

    public synchronized BigInteger getWorkDone() {
        if (getConfidenceType() != ConfidenceType.BUILDING) {
            throw new IllegalStateException("Confidence type is not BUILDING");
        }
        return workDone;
    }

    public synchronized void setWorkDone(BigInteger workDone) {
        this.workDone = workDone;
    }

    public synchronized Transaction getOverridingTransaction() {
        if (getConfidenceType() != ConfidenceType.DEAD)
            throw new IllegalStateException("Confidence type is " + getConfidenceType() +
                                            ", not OVERRIDDEN_BY_DOUBLE_SPEND");
        return overridingTransaction;
    }

    /**
     * Called when the transaction becomes newly dead, that is, we learn that one of its inputs has already been spent
     * in such a way that the double-spending transaction takes precedence over this one. It will not become valid now
     * unless there is a re-org. Automatically sets the confidence type to DEAD.
     */
    public synchronized void setOverridingTransaction(Transaction overridingTransaction) {
        this.overridingTransaction = overridingTransaction;
        setConfidenceType(ConfidenceType.DEAD);
    }

    /**
     * The source of a transaction tries to identify where it came from originally. For instance, did we download it
     * from the peer to peer network, or make it ourselves, or receive it via Bluetooth, or import it from another app,
     * and so on. This information is useful for {@link Wallet.CoinSelector} implementations to risk analyze
     * transactions and decide when to spend them.
     */
    public synchronized Source getSource() {
        return source;
    }

    /**
     * The source of a transaction tries to identify where it came from originally. For instance, did we download it
     * from the peer to peer network, or make it ourselves, or receive it via Bluetooth, or import it from another app,
     * and so on. This information is useful for {@link Wallet.CoinSelector} implementations to risk analyze
     * transactions and decide when to spend them.
     */
    public synchronized void setSource(Source source) {
        this.source = source;
    }
}
