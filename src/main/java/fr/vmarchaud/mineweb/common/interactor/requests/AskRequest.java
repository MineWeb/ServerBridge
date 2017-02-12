package fr.vmarchaud.mineweb.common.interactor.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class AskRequest {
	
	private String signed;
	private String iv;
}
