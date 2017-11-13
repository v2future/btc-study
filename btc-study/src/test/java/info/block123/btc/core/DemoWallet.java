package info.block123.btc.core;

import info.block123.btc.format.KeyFormat;
import info.block123.btc.format.PrivKeyFormat;
import info.block123.btc.kit.BtcKit;

/**
 * 测试钱包类
 * @author v2future
 *
 */
public class DemoWallet {

	public static void main(String[] args) {
		System.out.println("start...");
		new DemoWallet();
	}
	
	public DemoWallet () {
		String formatPrivKey = "5MN9hT3beGTJuUAmCQEmNaxAuMacCTfXuw1R3FCXig23RQHMr4K";
		KeyFormat format = new PrivKeyFormat(KeyFormat.PRIV_KEY_WIF);
		byte[] privKeyBytes = format.parse(formatPrivKey);
		Wallet wallet = new Wallet( BtcKit.byte32toBigInteger(privKeyBytes));
		byte[] pubKeyBytes = ECKey.publicKeyFromPrivate(BtcKit.byte32toBigInteger(privKeyBytes), false);
		System.out.println("钱包地址：" + BtcKit.getBtcAddress(pubKeyBytes));
		
		
		
		
	}
}





