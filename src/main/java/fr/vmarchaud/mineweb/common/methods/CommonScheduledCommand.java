package fr.vmarchaud.mineweb.common.methods;

import java.sql.Date;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.IMethod;
import fr.vmarchaud.mineweb.common.MethodHandler;
import fr.vmarchaud.mineweb.common.ScheduledCommand;

@MethodHandler(inputs = 3, types = { String.class, String.class, Date.class })
public class CommonScheduledCommand implements IMethod {
	
	@Override
	public Object execute(ICore instance, Object... inputs) {
		String command = (String) inputs[0];
		String player = (String) inputs[1];
		Date date = (Date) inputs[2];
		ScheduledCommand scheduled = new ScheduledCommand(command, player, date);
		instance.getCommandScheduler().getQueue().offer(scheduled);
		return true;
	}
}

