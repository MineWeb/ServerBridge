package fr.vmarchaud.mineweb.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
	
	/**
	 * Encrypt a string using a AES256 key
	 * @param data : string that will be encrypted
	 * @param key : private key used to encrypt the string
	 * @return an array of bytes representing the encrypted string
	 * 
	 * @throws Exception
	 */
	public static byte[] encrypt(String data, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(String.valueOf(data).getBytes("UTF-8"));
    }

	/**
	 * Decrypt a string using a AES256 key
	 * @param data : string that will be decrypted
	 * @param key : private key used to decrypt the string
	 * @return a String representating the decrypted data
	 * 
	 * @throws Exception
	 */
    public static String decrypt(byte[] data, SecretKeySpec key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(data));

    }
}