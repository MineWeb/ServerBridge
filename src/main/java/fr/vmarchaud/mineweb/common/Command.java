package fr.vmarchaud.mineweb.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class Command {
	private String name;
	private Object[] args;
}
