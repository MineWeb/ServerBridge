package fr.vmarchaud.mineweb.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class CryptoUtils {
	
	/**
	 * Encrypt a string using a AES256 key
	 * @param data : string that will be encrypted
	 * @param key : private key used to encrypt the string
	 * @return an array of bytes representing the encrypted string
	 * 
	 * @throws Exception
	 */
	public static byte[] encrypt(String raw, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] ciphered = cipher.doFinal(raw.getBytes("UTF-8"));
        return DatatypeConverter.printBase64Binary(ciphered).getBytes();
    }

	/**
	 * Decrypt a string using a AES256 key
	 * @param data : string that will be decrypted
	 * @param key : private key used to decrypt the string
	 * @return a String representating the decrypted data
	 * 
	 * @throws Exception
	 */
    public static String decrypt(byte[] raw, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        String data = DatatypeConverter.printBase64Binary(raw);
        return new String(cipher.doFinal(data.getBytes()));

    }
}