package fr.vmarchaud.mineweb.common.interactor.responses;

import lombok.Data;

@Data
public class RetrieveKeyResponse {

	private boolean status;
	private String msg;
	
	// only where if the response is successful
	private String secret_key;
}
