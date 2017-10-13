package info.block123.btc.core;

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
	 
	 public String toString() {
		 
		 return null;
	 }
}
