package fr.vmarchaud.mineweb.bridge.interactor.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AskRequest {
    private String signed;
    private String iv;
}
