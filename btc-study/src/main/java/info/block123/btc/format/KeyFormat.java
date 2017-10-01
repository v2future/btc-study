package info.block123.btc.format;

/**
 * 私钥、公钥格式化
 * @author v2future
 *
 */
public abstract class KeyFormat {

	protected KeyFormat () {}
	/**16进制格式**/
	public static final String PRIV_KEY_HEX = "Hex";
	/**WIF格式：Base58Check encoding Base58 with version prefix of 128 and 32-bit checksum **/
	public static final String PRIV_KEY_WIF = "WIF";
	/**WIF-COMPFRESS 压缩格式私钥**/
	public static final String PRIV_KEY_WIFC = "WIF-C";
	
	public abstract String format ( byte[] bytes);
	
	public abstract byte[] parse(String src);
}
