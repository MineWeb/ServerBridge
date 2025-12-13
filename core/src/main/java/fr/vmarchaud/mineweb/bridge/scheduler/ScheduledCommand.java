package fr.vmarchaud.mineweb.bridge.scheduler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScheduledCommand {
    private String command;
    private String player;
    private long timestamp;
}
