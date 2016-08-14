package fr.vmarchaud.mineweb.utils.http;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.vmarchaud.mineweb.common.ICore;

public class MinewebInteractor {
	
	/**
	 * Retrieve a the secret key from the activation key
	 * @param api : API instance to deserialize the reponse
	 * @param activation_key : the activation key input by a user
	 * @return RetrieveKeyResponse: the response
	 * 
	 * @throws Exception
	 */
	public static RetrieveKeyResponse retrieveKey(ICore api, String activation_key) throws Exception {
		final URL endpoint = new URL(String.format("%s%s?key=%s", "http://api.mineweb.org",
				"/interact/retrieve", activation_key)) ;
		
		HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
		conn.setReadTimeout(1000);
		conn.setConnectTimeout(1000);
		
		return api.gson().fromJson(new InputStreamReader(conn.getInputStream()), RetrieveKeyResponse.class);
	}
}
