package fr.vmarchaud.mineweb.bridge.interactor.requests;

import lombok.Data;

import java.util.UUID;

@Data
public class HandshakeRequest {
    private String domain;
    private String secretKey;
    private transient String id = UUID.randomUUID().toString().substring(0, 8);

    public boolean isValid() {
        return domain != null && secretKey != null;
    }
}
