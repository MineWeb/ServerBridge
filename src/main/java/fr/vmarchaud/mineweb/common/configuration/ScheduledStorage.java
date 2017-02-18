package fr.vmarchaud.mineweb.common.configuration;

import java.util.Set;

import fr.vmarchaud.mineweb.common.ScheduledCommand;
import lombok.Data;

@Data
public class ScheduledStorage extends Configuration {
	
	private Set<ScheduledCommand>	commands;
}
