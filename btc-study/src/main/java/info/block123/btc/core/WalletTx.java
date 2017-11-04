package info.block123.btc.core;

/**
 * 
 * 包含交易的额外信息 如还在交易池的tx
 * 
 * A Transaction in a Wallet - includes the pool ID
 * @author v2future
 *
 */
public class WalletTx {

	public enum Pool {
		UNSPENT(4), // unspent in best chain
		SPENT(5), // spent in best chain
		INACTIVE(2), // in alt chain
		DEAD(10), // double-spend in alt chain
		PENDING(16), // a pending tx we would like to go into the best chain
		PENDING_INACTIVE(18), // a pending tx in alt but not in best yet
		ALL(-1);

		private int value;

		Pool(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static Pool valueOf(int value) {
			switch (value) {
			case 4:
				return UNSPENT;
			case 5:
				return SPENT;
			case 2:
				return INACTIVE;
			case 10:
				return DEAD;
			case 16:
				return PENDING;
			case 18:
				return PENDING_INACTIVE;
			default:
				return null;
			}
		}
	}

	private Transaction transaction;
	
	/* 
	 * TODO
	private Pool pool;

	public WalletTx(Pool pool, Transaction transaction) {
	        this.pool = checkNotNull(pool);
	        this.transaction = transaction;
	}
	*/    

	public Transaction getTransaction() {
		return transaction;
	}

	//public Pool getPool() {
	//	return pool;
	//}
}
