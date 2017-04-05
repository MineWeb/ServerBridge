package fr.vmarchaud.mineweb.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ScheduledCommand {

	private String command;
	private String player;
	private Long timestamp;
}
