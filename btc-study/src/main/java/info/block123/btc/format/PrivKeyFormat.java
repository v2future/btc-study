package info.block123.btc.format;

import info.block123.btc.core.BtcException;
import info.block123.btc.kit.Base58Check;
import info.block123.btc.kit.BtcKit;

/**
 * 私钥格式化
 * Hex 
 * WIF 5开头
 * WIF-COMPRESS K或L开头
 * @author v2future
 *
 */
public class PrivKeyFormat extends KeyFormat{
	
	private String keyType;
	public PrivKeyFormat(String keyType) {
		if ( keyType == null)
			throw new BtcException("密钥格式不可以为空");
		if ( !PRIV_KEY_HEX.equals(keyType) &&
			 !PRIV_KEY_WIF.equals(keyType) &&
			 !PRIV_KEY_WIFC.equals(keyType))
			throw new BtcException("不支持的密钥格式");
		this.keyType = keyType;
	}
	
	/**
	 * 未加工的密钥格式化
	 */
	@Override
	public String format(byte[] keyBytes) {
		if ( PRIV_KEY_HEX.equals(keyType)) {
			return BtcKit.toHexString(keyBytes);
		}
		if ( PRIV_KEY_WIF.equals(keyType)) {
			byte version = (byte)0x80;
			return Base58Check.encode(version, keyBytes);
		}
		if ( PRIV_KEY_WIFC.equals(keyType)) {
			byte[] cBytes = new byte[ keyBytes.length + 1];
			System.arraycopy(keyBytes, 0, cBytes, 0, keyBytes.length);
			cBytes[ cBytes.length - 1] = 0x01;
			byte version = (byte)0x80;
			return Base58Check.encode(version, cBytes);
		}
		return null;
	}

	/**
	 * 格式化的私钥转为私钥byte数组
	 */
	@Override
	public byte[] parse(String src) {
		if ( PRIV_KEY_HEX.equals(keyType)) {
			return BtcKit.hexStringToByte(src);
		}
		if ( PRIV_KEY_WIF.equals(keyType)) {
			return Base58Check.decode(src);
		} 
		if (PRIV_KEY_WIFC.equals(keyType)) {
			byte[] rawBytes = Base58Check.decode(src);
			byte[] result = new byte[rawBytes.length-1];
			System.arraycopy(rawBytes, 0, result, 0, result.length);
			return result;
		}
		return null;
	}

}
