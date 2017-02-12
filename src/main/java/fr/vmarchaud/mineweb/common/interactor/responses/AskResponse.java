package fr.vmarchaud.mineweb.common.interactor.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class AskResponse {
	
	private String signed;
	private String iv;
}
