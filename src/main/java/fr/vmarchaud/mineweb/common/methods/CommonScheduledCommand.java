package fr.vmarchaud.mineweb.common.methods;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.IMethod;
import fr.vmarchaud.mineweb.common.MethodHandler;
import fr.vmarchaud.mineweb.common.ScheduledCommand;

@MethodHandler(inputs = 3, types = { String.class, String.class, Double.class })
public class CommonScheduledCommand implements IMethod {
	
	@Override
	public Object execute(ICore instance, Object... inputs) {
		String command = (String) inputs[0];
		String player = (String) inputs[1];
		Long time = ((Double)inputs[2]).longValue();
		ScheduledCommand scheduled = new ScheduledCommand(command, player, time);
		instance.getCommandScheduler().getQueue().offer(scheduled);
		return true;
	}
}

