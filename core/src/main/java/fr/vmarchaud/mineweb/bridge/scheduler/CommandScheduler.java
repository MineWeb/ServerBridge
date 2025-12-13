package fr.vmarchaud.mineweb.bridge.scheduler;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.config.ScheduledStorage;
import lombok.Getter;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandScheduler implements Runnable {

    private final ICore core;
    private final ScheduledStorage storage;
    private final Set<ScheduledCommand> commands;

    @Getter
    private final ConcurrentLinkedQueue<ScheduledCommand> queue = new ConcurrentLinkedQueue<>();

    public CommandScheduler(ICore core, ScheduledStorage storage) {
        this.core = core;
        this.storage = storage;
        this.commands = storage.getCommands();
    }

    @Override
    public void run() {
        Date now = new Date();
        Iterator<ScheduledCommand> it = commands.iterator();
        while (it.hasNext()) {
            ScheduledCommand cmd = it.next();
            if (cmd.getTimestamp() > now.getTime()) {
                continue;
            }
            if (cmd.getPlayer() != null && !core.getPlayers().contains(cmd.getPlayer())) {
                continue;
            }
            core.runCommand(cmd.getCommand());
            it.remove();
        }
        ScheduledCommand next;
        while ((next = queue.poll()) != null) {
            commands.add(next);
        }
        save();
    }

    public void save() {
        storage.setCommands(commands);
        storage.save(core.getGson(), core);
    }
}
