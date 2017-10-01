package info.block123.btc.core;

import org.junit.Assert;
import org.junit.Test;

import info.block123.btc.format.KeyFormat;
import info.block123.btc.format.PrivKeyFormat;
import info.block123.btc.kit.BtcKit;

/**
 * 密钥格式化测试类
 * @author v2future
 *
 */
public class KeyFormatTest {

	/**
	 * 测试转换为wif格式
	 */
	@Test
	public void testWIFFormat () {
		byte[] privKeyBytes = BtcKit.hexStringToByte("1E99423A4ED27608A15A2616A2B0E9E52CED330AC530EDCC32C8FFC6A526AEDD");
		KeyFormat format = new PrivKeyFormat(KeyFormat.PRIV_KEY_WIF);
		String wifStr = format.format(privKeyBytes);
		System.out.println("WIF: " + wifStr);
		Assert.assertEquals(wifStr, "5J3mBbAH58CpQ3Y5RNJpUKPE62SQ5tfcvU2JpbnkeyhfsYB1Jcn");
	}
	
	/**
	 * 测试转换为wif-c格式
	 */
	@Test
	public void testWIFCFormat () {
		byte[] privKeyBytes = BtcKit.hexStringToByte("1E99423A4ED27608A15A2616A2B0E9E52CED330AC530EDCC32C8FFC6A526AEDD");
		KeyFormat format = new PrivKeyFormat(KeyFormat.PRIV_KEY_WIFC);
		String wifcStr = format.format(privKeyBytes);
		System.out.println("WIF-C: " + wifcStr);
		Assert.assertEquals(wifcStr, "KxFC1jmwwCoACiCAWZ3eXa96mBM6tb3TYzGmf6YwgdGWZgawvrtJ");
	}
	
	/**
	 * 测试解析wif格式
	 */
	@Test
	public void testWifParse () {
		String wifStr = "5J3mBbAH58CpQ3Y5RNJpUKPE62SQ5tfcvU2JpbnkeyhfsYB1Jcn";
		KeyFormat format = new PrivKeyFormat(KeyFormat.PRIV_KEY_WIF);
		byte[] privKeyBytes = format.parse(wifStr);
		System.out.println("Hex String : " + BtcKit.toHexString(privKeyBytes));
		Assert.assertEquals( BtcKit.toHexString(privKeyBytes), 
				"1E99423A4ED27608A15A2616A2B0E9E52CED330AC530EDCC32C8FFC6A526AEDD");
	}
	
	/**
	 * 测试解析wif-c格式
	 */
	@Test
	public void testWifcParse () {
		String wifcStr = "KxFC1jmwwCoACiCAWZ3eXa96mBM6tb3TYzGmf6YwgdGWZgawvrtJ";
		KeyFormat format = new PrivKeyFormat(KeyFormat.PRIV_KEY_WIFC);
		byte[] privKeyBytes = format.parse(wifcStr);
		System.out.println("Hex String : " + BtcKit.toHexString(privKeyBytes));
		Assert.assertEquals( BtcKit.toHexString(privKeyBytes), 
				"1E99423A4ED27608A15A2616A2B0E9E52CED330AC530EDCC32C8FFC6A526AEDD");
	}
	
	
}
