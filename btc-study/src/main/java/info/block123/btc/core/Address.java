package info.block123.btc.core;

import info.block123.btc.kit.BtcKit;

/**
 * 比特币地址
 * @author v2future
 *
 */
public class Address {

	 private int version;
	 private byte[] bytes;
	 
	 public Address(byte[] hash160) {
		 this.bytes = hash160;
	 }
	 
	 /**
	  * 返回btc地址
	  */
	 public String toString() {
		 return BtcKit.getBtcAddress(this.bytes);
	 }
}
