package info.block123.btc.core;

import info.block123.btc.kit.BtcKit;

/**
 * 比特币地址
 * @author v2future
 *
 */
public class Address {
	
	public static final int LENGTH = 20;
	 private int version;
	 private byte[] bytes;
	 
	 public Address(byte[] hash160) {
		 this.bytes = hash160;
	 }
	 
	 /**
	  * 返回公钥
	  * @return
	  */
	 public byte[] getHash160() {
	        return bytes;
	 }
	 
	 /**
	  * 返回btc地址
	  */
	 public String toString() {
		 return BtcKit.getBtcAddress(this.bytes);
	 }
}
