package info.block123.btc.kit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * base58check 编码
 * @author 160516a
 *
 */
public class Base58Check {

	/**
	 * base58check编码
	 * 第一位 version
	 * 中间32位：数据
	 * 后四位 ： 校验码
	 * @param version
	 * @param input
	 * @return
	 */
	public static String encode (byte version, byte[] input) {
    	byte[] versionInput = new byte[ 1 + input.length];
    	//版本
    	versionInput[0] = version;
    	System.arraycopy(input, 0, versionInput, 1, input.length);
    	byte[] checkByte = doubleSha256(versionInput);
    	byte[] checkInputData = new byte[ 1 + input.length + 4];
    	System.arraycopy(versionInput, 0, checkInputData, 0, versionInput.length);
    	//检验字节
        System.arraycopy(checkByte, 0, checkInputData, versionInput.length, 4);
		return Base58.encode(checkInputData);
	}
	
	/**
	 * base58check解码
	 * @param input
	 * @return
	 */
	public static byte[] decode (String input) {
		byte[] rawByte = Base58.decode(input);
		byte[] result = new byte[ rawByte.length -1 -4];
		System.arraycopy(rawByte, 1, result, 0, result.length);
		return result;
	}
	/**
	 * 执行2次sha256加密算法，截取结果的前4个字节作为checknum，加到btc地址中
	 * @param input
	 * @return
	 */
	private static byte[] doubleSha256 (byte[] input) {
		try {
			byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(input);
			return MessageDigest.getInstance("SHA-256").digest(sha256);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e); // Cannot happen.
		}
	}
}
