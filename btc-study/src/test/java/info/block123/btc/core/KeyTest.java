package info.block123.btc.core;

import org.junit.Assert;
import org.junit.Test;

import info.block123.btc.format.KeyFormat;
import info.block123.btc.format.PrivKeyFormat;
import info.block123.btc.kit.Base58;
import info.block123.btc.kit.BtcKit;

/**
 * 公钥私钥测试
 * @author v2future
 *
 */
public class KeyTest {

	//测试公钥到比特币地址
	@Test
	public void testPubKey () {
    	ECKey key = new ECKey();
    	byte[] pubKeyBytes = key.getPubKey();
    	
    	//公钥计算出地址
    	
    	/*
    	byte[] hash160 = BtcKit.sha256hash160(pubKeyBytes);
    	byte[] versionHash160 = new byte[ 1 + hash160.length];
    	//版本
    	versionHash160[0] = 0x00;
    	System.arraycopy(hash160, 0, versionHash160, 1, hash160.length);
    	
    	byte[] checkByte = BtcKit.doubleSha256(versionHash160);
    	byte[] checkedHash160 = new byte[ 1 + hash160.length + 4];
    	System.arraycopy(versionHash160, 0, checkedHash160, 0, versionHash160.length);
    	//检验字节
        System.arraycopy(checkByte, 0, checkedHash160, versionHash160.length, 4);
        
    	String btcAddress = Base58.encode(checkedHash160);
    	System.out.println("btcAddress: " + btcAddress);
    	*/
	}
	
	//测试私钥格式化
	@Test
	public void testPrivKeyFormat () {
		
	}
	
	//测试根据私钥生成比特币地址
	@Test
	public void testPrivKey () {
		ECKey key = new ECKey();
    	byte[] privKeyBytes = key.getPrivKeyBytes();
    	String addr0 = BtcKit.getBtcAddress( key.getPubKey());
    	System.out.println( "btc地址：" + addr0);
    	
    	byte[] pubKeyBytes = ECKey.publicKeyFromPrivate( BtcKit.byte32toBigInteger(privKeyBytes), true);
    	String addr = BtcKit.getBtcAddress(pubKeyBytes);
    	System.out.println("btc地址：" + addr);
    	Assert.assertEquals(addr0, addr);
	}
	
	/**
	 */
	@Test
	public void testPrivKeyToAddress() {
		String formatPrivKey = "5MN9hT3beGTJuUAmCQEmNaxAuMacCTfXuw1R3FCXig23RQHMr4K";
		KeyFormat format = new PrivKeyFormat(KeyFormat.PRIV_KEY_WIF);
		byte[] privKeyBytes = format.parse(formatPrivKey);
		byte[] pubKeyBytes = ECKey.publicKeyFromPrivate(BtcKit.byte32toBigInteger(privKeyBytes), false);
		String address = BtcKit.getBtcAddress(pubKeyBytes);
		System.out.println("计算后的地址：" + address);
		Assert.assertEquals(address, "1thMirt546nngXqyPEz532S8fLwbozud8");
	}
}
