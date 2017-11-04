package info.block123.btc.kit;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.spongycastle.crypto.digests.RIPEMD160Digest;

import info.block123.btc.core.BtcException;

/**
 * btc学习 工具集
 * @author v2future
 *
 */
public class BtcKit {

	/**
	 * BigInteger 转为指定位数的byte数组
	 * @param b
	 * @param numBytes
	 * @return
	 */
	public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
		if (b == null) {
			return null;
		}
		byte[] bytes = new byte[numBytes];
		byte[] biBytes = b.toByteArray();
		int start = (biBytes.length == numBytes + 1) ? 1 : 0;
		int length = Math.min(biBytes.length, numBytes);
		System.arraycopy(biBytes, start, bytes, numBytes - length, length);
		return bytes;
	}
	
	/**
	 * 32位数组转换为BigInteger
	 * @param byte32
	 * @return
	 */
	public static BigInteger byte32toBigInteger (byte[] byte32) {
		if ( byte32 == null ||
			 byte32.length != 32 )
			throw new BtcException("输入字节数字位数不等于32");
		//注意，数字为正数
		return new BigInteger(1, byte32); 
	}
	
	/**
	 * byte数组转成16进制字符串
	 * @param b
	 * @return
	 */
	public static String toHexString(byte[] b) {
		String str = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			str += hex.toUpperCase();
		}
		return str;
	}
	
	/**
	 * 把16进制字符串转换为byte数组
	 * @param hexString
	 * @return
	 */
	public static byte[] hexStringToByte(String hexString) {
		int length = hexString.length();
		byte[] bytes = new byte[length / 2];
		for (int i = 0; i < length; i += 2) {
			// 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
			bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
					+ Character.digit(hexString.charAt(i + 1), 16));
		}
		return bytes;
	}
	
	//---------------------------------------------------------------------------
	/**
	 * ripemd160(sha256(in))
	 * 双hash，计算比特币地址时使用
	 */
	public static byte[] sha256hash160(byte[] input) {
		try {
			byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(input);
			RIPEMD160Digest digest = new RIPEMD160Digest();
			digest.update(sha256, 0, sha256.length);
			byte[] out = new byte[20];
			digest.doFinal(out, 0);
			return out;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e); // Cannot happen.
		}
	}
	
	/**
     * 从公钥计算出地址
     * @param pubKeyBytes
     * @return
     */
    public static String getBtcAddress (byte[] pubKeyBytes) {
    	byte[] hash160 = sha256hash160(pubKeyBytes);
    	byte version = 0x00;
    	return Base58Check.encode(version, hash160);
    }
    
    /**
     * 复制指定长度的byte
     * @param in
     * @param length
     * @return
     */
    public static byte[] copyOf(byte[] in, int length) {
        byte[] out = new byte[length];
        System.arraycopy(in, 0, out, 0, Math.min(length, in.length));
        return out;
    }
}
