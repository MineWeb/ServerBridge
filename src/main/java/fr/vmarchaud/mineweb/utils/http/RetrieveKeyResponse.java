package fr.vmarchaud.mineweb.utils.http;

import lombok.Data;

@Data
public class RetrieveKeyResponse {
	
	private String key;
	private KeyResult result;
	
	
	public enum KeyResult {
		VALID, DISABLED, BANNED, NOT_FOUND
	}
}
