package fr.vmarchaud.mineweb.utils;

import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class CryptoUtils {
	
	/**
	 * Encrypt a string using AES128
	 * 
	 * @param data string that will be encrypted
	 * @param key shared key used to encrypt the string
	 * @param iv public iv shared with request
	 * 
	 * @return String base64 encoded data after encryption
	 * 
	 * @throws Exception
	 */
	public static String encryptAES(String raw, SecretKeySpec key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(DatatypeConverter.parseBase64Binary(iv), 0, cipher.getBlockSize()));
        byte[] ciphered = cipher.doFinal(raw.getBytes("UTF-8"));
        return DatatypeConverter.printBase64Binary(ciphered);
    }

	/**
	 * Decrypt a string using AES128
	 * 
	 * @param data string that will be decrypted (in base64)
	 * @param key shared key used to decrypt the string
	 * @param iv public iv shared with request
	 * 
	 * @return a String representating the decrypted data
	 * 
	 * @throws Exception
	 */
    public static String decryptAES(String raw, SecretKeySpec key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(DatatypeConverter.parseBase64Binary(iv), 0, cipher.getBlockSize()));
        return new String(cipher.doFinal(DatatypeConverter.parseBase64Binary(raw)), "UTF-8");

    }
    
    /**
	 * Encrypt a string using a RSA public key
	 * 
	 * @param data string that will be encrypted
	 * @param key shared key used to encrypt the string
	 * @param iv public iv shared with request
	 * 
	 * @return String base64 encoded data after encryption
	 * 
	 * @throws Exception
	 */
    public static String encryptRSA(String raw, PublicKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");   
        cipher.init(Cipher.ENCRYPT_MODE, key);  
        byte[] ciphered = cipher.doFinal(raw.getBytes("UTF-8"));
        return DatatypeConverter.printBase64Binary(ciphered);
    }
    
    /**
	 * Decrypt a string using a RSA public key
	 * 
	 * @param data string that will be decrypted (in base64)
	 * @param key shared key used to decrypt the string
	 * @param iv public iv shared with request
	 * 
	 * @return String
	 * 
	 * @throws Exception
	 */
    public static String decryptRSA(String raw, PublicKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");   
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(DatatypeConverter.parseBase64Binary(raw)));
    }
}