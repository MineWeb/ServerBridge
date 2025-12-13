package fr.vmarchaud.mineweb.bridge.interactor.responses;

import lombok.Data;

@Data
public class HandshakeResponse {
    private boolean status;
    private String msg;
}
