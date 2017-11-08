package info.block123.btc.kit;

import java.math.BigInteger;

/**
 * 常量信息
 * @author v2future
 *
 */
public class BtcConstant {

	/**
     * How many "nanocoins" there are in a Bitcoin.
     */
    public static final BigInteger COIN = new BigInteger("100000000", 10);
    
    /**
     * How many "nanocoins" there are in 0.01 BitCoins.
     */
    public static final BigInteger CENT = new BigInteger("1000000", 10);
    
    /**
     * The maximum money to be generated
     */
    public static final BigInteger MAX_MONEY = new BigInteger("21000000", 10).multiply(COIN);
}
