package fr.vmarchaud.mineweb.common.interactor.requests;

import java.util.UUID;

import lombok.Data;

@Data
public class HandshakeRequest {
	
	private String domain;
	private String secretKey;
	private transient String id = UUID.randomUUID().toString().substring(0, 8);
	
	public boolean isValid() {
		return domain != null && secretKey != null;
	}
}
