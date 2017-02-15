package fr.vmarchaud.mineweb.bukkit.methods;

import org.bukkit.Server;
import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.IMethod;
import fr.vmarchaud.mineweb.common.MethodHandler;

@MethodHandler(inputs = 1, types = {String.class})
public class BukkitRunCommand implements IMethod {
	
	@Override
	public Object execute(ICore instance, Object... inputs) {
		Server bukkit = ((Server)instance.getGameServer());
		String command = (String) inputs[0];
		bukkit.dispatchCommand(bukkit.getConsoleSender(), command);
		return true;
	}
}
