package info.block123.btc.core;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.math.ec.ECPoint;

import info.block123.btc.kit.Base58;
import info.block123.btc.kit.BtcKit;

/**
 * 椭圆曲线算法
 * @author v2future
 *
 */
public class ECKey {
	
	private static final ECDomainParameters ecParams;
    private static final SecureRandom secureRandom;
    
	private BigInteger priv;
	//公钥
    private byte[] pub;
    
    static {
        // All clients must agree on the curve to use by agreement. Bitcoin uses secp256k1.
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        ecParams = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        secureRandom = new SecureRandom();
    }
    
	public ECKey () {
		ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(ecParams, secureRandom);
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
        priv = privParams.getD();
        // Unfortunately Bouncy Castle does not let us explicitly change a point to be compressed, even though it
        // could easily do so. We must re-build it here so the ECPoints withCompression flag can be set to true.
        ECPoint uncompressed = pubParams.getQ();
        ECPoint compressed = compressPoint(uncompressed);
        pub = compressed.getEncoded();
        //creationTimeSeconds = Utils.now().getTime() / 1000;
	}
	
	private static ECPoint compressPoint(ECPoint uncompressed) {
		return new ECPoint.Fp(ecParams.getCurve(), uncompressed.getX(), uncompressed.getY(), true);
	}
	
	/**
	 * 
	 * @return 返回公钥
	 */
	public byte[] getPubKey() {
        return pub;
    }
	
	/**
	 * 
	 * @return 返回私钥
	 */
    public byte[] getPrivKeyBytes() {
    	return BtcKit.bigIntegerToBytes(priv, 32);
    }
    
    /**
     * 根据私钥返回公钥，椭圆曲线相乘得到公钥
     * @param privKey
     * @param compressed
     * @return
     */
    public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
        ECPoint point = ecParams.getG().multiply(privKey);
        if (compressed)
            point = compressPoint(point);
        return point.getEncoded();
    }
    
}
