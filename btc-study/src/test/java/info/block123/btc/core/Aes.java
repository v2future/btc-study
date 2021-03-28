package info.block123.btc.core;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class AES {
	
	  public static final String DEFAULT_KEY = "test";

    public static String DES = "AES"; // optional value AES/DES/DESede
    public static String CIPHER_ALGORITHM = "AES"; // optional value AES/DES/DESede

    private static Key getKey(String strKey) {
        try {
            if (strKey == null) {
                strKey = "";
            }
            KeyGenerator _generator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(strKey.getBytes());
            _generator.init(128, secureRandom);
            return _generator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(" 初始化密钥出现异常 ");
        }
    }

    public static String encode(String key, String data) {
    	try {
	        SecureRandom sr = new SecureRandom();
	        Key secureKey = getKey(key);
	        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
	        cipher.init(Cipher.ENCRYPT_MODE, secureKey, sr);
	        byte[] bt = cipher.doFinal(data.getBytes());
	        String strS = new BASE64Encoder().encode(bt);
	        return strS;
    	} catch(Exception e) {
    		throw new RuntimeException("AES加密异常");
    	}
    }


    public static String decode(String key, String data) {
    	try {
	        SecureRandom sr = new SecureRandom(); 
	        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
	        Key secureKey = getKey(key);
	        cipher.init(Cipher.DECRYPT_MODE, secureKey, sr);
	        byte[] res = new BASE64Decoder().decodeBuffer(data);
	        res = cipher.doFinal(res);
	        return new String(res);
	    } catch(Exception e) {
			throw new RuntimeException("AES解密异常");
		}
    }
    
    
    public static void main(String[] args) {
    	String target = AES.encode("key", "test");
    	System.out.println( target);
    
    }
}

