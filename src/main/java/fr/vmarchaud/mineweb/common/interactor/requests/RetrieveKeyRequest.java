package fr.vmarchaud.mineweb.common.interactor.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class RetrieveKeyRequest {

	private String signed;
	
	@Data @AllArgsConstructor
	public static class SignedData {
		private String id;
		private String key;
		private String domain;
	}
}
