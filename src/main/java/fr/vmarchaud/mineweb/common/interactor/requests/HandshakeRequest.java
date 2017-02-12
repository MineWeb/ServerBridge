package fr.vmarchaud.mineweb.common.interactor.requests;

import java.util.UUID;

import lombok.Data;

@Data
public class HandshakeRequest {
	
	private String domain;
	private String licenseId;
	private String licenseKey;
	
	private transient String id = UUID.randomUUID().toString().substring(0, 8);
	
	public boolean isValid() {
		if (domain == null || licenseId == null || licenseKey == null)
			return false;
		else
			return true;
	}
}
