package fr.vmarchaud.mineweb.utils.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.annimon.stream.Stream;
import com.google.gson.JsonObject;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.utils.CryptoUtils;

public class MinewebInteractor {

	private static SecretKeySpec	MINEWEB_PUBLIC_KEY;
	private static String 			MINEWEB_PUBLIC_RAW = 
			"-----BEGIN RSA PUBLIC KEY----- " +
			"MIIBCgKCAQEAnXpip4jop98LGTqCD/pFjwle3ykHl7vj90gNRni/qtN5LERm0HRv" +
			"uKZ1WL/bJbG3VFZxL1Cp168dkK9lxM1tQ7QEqbyPFstsMBaKVlxxkp5qJC/33l0y" +
			"RkTGWfKblur/VCyWfhCzL7KfGXvy0/MFaZYfrfLBa9I3oG4asmQ4THL72A70jQsD" +
			"8khqfD83MJWejr3EsLKCCO7iav5CbynavKfoYQGQfJegB/QwCVR5cpXgDJNlytn1" +
			"XvzpvasjixslGe9wtAEIoxWAWbEeGqllT8s2pBSa9NoXJlEJMKN5BSG7YTaSHbF6" +
			"QqyuoDyDH3OreZ6CHLWUijGPQFosUKj0ewIDAQAB" +
			"-----END RSA PUBLIC KEY-----";
	
	static {
		MINEWEB_PUBLIC_KEY = new SecretKeySpec(MINEWEB_PUBLIC_RAW.getBytes(), "AES");
	}
	
	/**
	 * Retrieve a the secret key from the activation key
	 * @param api : API instance to deserialize the reponse
	 * @param licenseId : the activation id of the license
	 * @param licenseKey : the licenseKey of the license
	 * @param domain : the domain of the license
	 * 
	 * @return RetrieveKeyResponse: the response
	 * 
	 * @throws Exception
	 */
	public static RetrieveKeyResponse retrieveKey(ICore api, String licenseId, String licenseKey, String domain) throws Exception {
		JsonObject data = new JsonObject();
		data.addProperty("id", licenseId);
		data.addProperty("key", licenseKey);
		data.addProperty("domain", domain);

		// cipher data using mineweb api key
		byte[] ciphered = CryptoUtils.encrypt(api.gson().toJson(data), MINEWEB_PUBLIC_KEY);		
		final URL endpoint = new URL("https://api.mineweb.org/api/v2/key") ;
		
		HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
		conn.setReadTimeout(1000);
		conn.setConnectTimeout(1000);
		
		// write ciphered into out stream
		conn.getOutputStream().write(ciphered);
		conn.getOutputStream().close();
		
		// read result from input stream
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = conn.getInputStream().read(buffer)) != -1) {
		    result.write(buffer, 0, length);
		}
		if (conn.getResponseCode() != 200)
			throw new RuntimeException("MinewebAPI sent invalid response code " + conn.getResponseCode());
		
		// decrypt the response
		String raw_result = CryptoUtils.decrypt(result.toByteArray(), MINEWEB_PUBLIC_KEY);
		conn.disconnect();
		
		return api.gson().fromJson(raw_result, RetrieveKeyResponse.class);
	}
}
