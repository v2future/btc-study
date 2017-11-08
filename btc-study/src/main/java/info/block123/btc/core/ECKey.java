package info.block123.btc.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.DERInteger;
import org.spongycastle.asn1.DERSequenceGenerator;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.math.ec.ECPoint;

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
	
	public ECKey(BigInteger privKey) {
		this(privKey, (byte[])null);
	}
	
	private ECKey(BigInteger privKey, byte[] pubKey) {
        this(privKey, pubKey, false);
    }
	
	public ECKey(BigInteger privKey, byte[] pubKey, boolean compressed) {
        this.priv = privKey;
        this.pub = null;
        if (pubKey == null && privKey != null) {
            this.pub = publicKeyFromPrivate(privKey, compressed);
        } else if (pubKey != null) {
            this.pub = pubKey;
        }
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
    
    /**
     * Signs the given hash and returns the R and S components as BigIntegers. In the Bitcoin protocol, they are
     * usually encoded using DER format, so you want {@link com.google.bitcoin.core.ECKey.ECDSASignature#toASN1()}
     * instead. However sometimes the independent components can be useful, for instance, if you're doing to do
     * further EC maths on them.
     * @throws BtcException if this ECKey doesn't have a private part.
     */
    public ECDSASignature sign(byte[] sha256HashByte) throws BtcException {
        return sign(sha256HashByte, null);
    }

    /**
     * @param aesKey 加密的私钥
     * @throws KeyCrypterException if this ECKey doesn't have a private part.
     */
    public ECDSASignature sign(byte[] sha256HashByte, KeyParameter aesKey) throws BtcException {
        // The private key bytes to use for signing.
        ECDSASigner signer = new ECDSASigner();
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(priv, ecParams);
        signer.init(true, privKey);
        BigInteger[] sigs = signer.generateSignature(sha256HashByte);
        return new ECDSASignature(sigs[0], sigs[1]);
    }
    
    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param data      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     * @param pub       The public key bytes to use.
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(ecParams.getCurve().decodePoint(pub), ecParams);
        signer.init(false, params);
        try {
            ASN1InputStream decoder = new ASN1InputStream(signature);
            DLSequence seq = (DLSequence) decoder.readObject();
            DERInteger r = (DERInteger) seq.getObjectAt(0);
            DERInteger s = (DERInteger) seq.getObjectAt(1);
            decoder.close();
            // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
            // Thus, we always use the positive versions.
            // See: http://r6.ca/blog/20111119T211504Z.html
            try {
                return signer.verifySignature(data, r.getPositiveValue(), s.getPositiveValue());
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *	验证签名
     * @param data      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     */
    public boolean verify(byte[] data, byte[] signature) {
        return ECKey.verify(data, signature, pub);
    }
    
    /**
     * 签名函数
     * @author v2future
     *
     */
    public static class ECDSASignature {
        public BigInteger r, s;

        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        /**
         * What we get back from the signer are the two components of a signature, r and s. To get a flat byte stream
         * of the type used by Bitcoin we have to encode them using DER encoding, which is just a way to pack the two
         * components into a structure.
         */
        public byte[] encodeToDER() {
            try {
                // Usually 70-72 bytes.
                ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(72);
                DERSequenceGenerator seq = new DERSequenceGenerator(bos);
                seq.addObject(new DERInteger(r));
                seq.addObject(new DERInteger(s));
                seq.close();
                return bos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);  // Cannot happen.
            }
        }
    }
}
