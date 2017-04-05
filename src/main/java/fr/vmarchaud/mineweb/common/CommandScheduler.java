package fr.vmarchaud.mineweb.common;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import fr.vmarchaud.mineweb.common.configuration.ScheduledStorage;
import lombok.Getter;

public class CommandScheduler implements Runnable {
	
	private ICore api;
	private ScheduledStorage storage;
	private Set<ScheduledCommand> commands;
	
	@Getter
	private ConcurrentLinkedQueue<ScheduledCommand> queue = new ConcurrentLinkedQueue<ScheduledCommand>();
	
	/**
	 * ScheduledManager
	 * 
	 * Used to manage the command that are scheduled for the future (check/addition)
	 * 
	 * @param ICore api
	 * @param ScheduledStorage scheduled command storage
	 */
	public CommandScheduler(ICore api, ScheduledStorage storage) {
		this.api = api;
		this.storage = storage;
		this.commands = storage.getCommands();
	}
	
	@Override
	public void run() {
		Iterator<ScheduledCommand> it = commands.iterator();
		Date now = new Date();
		
		while(it.hasNext()) {
			ScheduledCommand command = it.next();
			// if the command is in the future, continue
			if (new Date(command.getTimestamp()).after(now))
				continue;
			// if the command need a player to be online and this player isn't connected, continue
			if (command.getPlayer() != null && !api.getPlayers().contains(command.getPlayer()))
				continue;
			// otherwise run the command and remove it from the list
			api.runCommand(command.getCommand());
			it.remove();
		}
		// add the new command for the next iteration
		ScheduledCommand next = null;
		while((next = queue.poll()) != null) {
			commands.add(next);
		}
		
		// save them on the disk
		save();
	}
	
	/**
	 * Save the cached commands in FS
	 */
	public void save() {
		storage.setCommands(commands);
		storage.save(api);
	}

}
