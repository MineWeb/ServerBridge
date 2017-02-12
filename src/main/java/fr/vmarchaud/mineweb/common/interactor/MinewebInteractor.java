package fr.vmarchaud.mineweb.common.interactor;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.stream.JsonReader;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.interactor.requests.HandshakeRequest;
import fr.vmarchaud.mineweb.common.interactor.requests.RetrieveKeyRequest;
import fr.vmarchaud.mineweb.common.interactor.requests.RetrieveKeyRequest.SignedData;
import fr.vmarchaud.mineweb.common.interactor.responses.RetrieveKeyResponse;
import fr.vmarchaud.mineweb.utils.CryptoUtils;

public class MinewebInteractor {

	public static PublicKey	MINEWEB_PUBLIC_KEY;
	
	static {
		InputStream stream = MinewebInteractor.class.getResourceAsStream("/mineweb.rsa");
		byte[] buffer = new byte[1024];
		StringBuilder data = new StringBuilder();
		try {
			while (stream.read(buffer) != -1)
			    data.append(new String(buffer));
			stream.close();
			
			String key = data.toString().replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");
		    EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(key));
		    MINEWEB_PUBLIC_KEY = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieve a the secret key from the activation key
	 * @param api : API instance to deserialize the reponse
	 * @param HandshakeRequest : the handshake request object provided by the cms
	 * 
	 * @return RetrieveKeyResponse: the response
	 * 
	 * @throws Exception
	 */
	public static RetrieveKeyResponse retrieveKey(ICore api, HandshakeRequest handshake) throws Exception {
		RetrieveKeyRequest request = new RetrieveKeyRequest();
		SignedData signed = new RetrieveKeyRequest.SignedData(handshake.getLicenseId(), handshake.getLicenseKey(), handshake.getDomain());
		request.setSigned(CryptoUtils.encryptRSA(api.gson().toJson(signed), MINEWEB_PUBLIC_KEY));	
		
		// cipher data using mineweb api key	
		final URL endpoint = new URL("https://api.mineweb.org/api/v2/key") ;
		
		
		HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setDoOutput(true);
		conn.setReadTimeout(1000);
		conn.setConnectTimeout(1000);
		
		// write ciphered into out stream
		conn.getOutputStream().write(api.gson().toJson(request).getBytes());
		conn.getOutputStream().close();
		
		api.logger().info(String.format("API send code %s for handshake id: %s", conn.getResponseCode(), handshake.getId()));
		
		// if cloudflare tell us that the server is down
		if (conn.getResponseCode() == 521 || conn.getResponseCode() == 522)
			throw new Exception("Mineweb API is down");

		InputStream stream;
		if (conn.getResponseCode() >= 400)
			stream = conn.getErrorStream();
		else
			stream = conn.getInputStream();
		
		// read result from input stream
		byte[] buffer = new byte[65536];
		StringBuilder data = new StringBuilder();
		while (stream.read(buffer) != -1) {
		    data.append(new String(buffer));
		}
		stream.close();
		conn.disconnect();

		JsonReader reader = new JsonReader(new StringReader(data.toString()));
		reader.setLenient(true);
		
		return api.gson().fromJson(reader, RetrieveKeyResponse.class);
	}
}
