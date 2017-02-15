package fr.vmarchaud.mineweb.bungee.methods;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.IMethod;
import fr.vmarchaud.mineweb.common.MethodHandler;
import net.md_5.bungee.api.ProxyServer;

@MethodHandler(inputs = 1, types = {String.class})
public class BungeeRunCommand implements IMethod {
	
	@Override
	public Object execute(ICore instance, Object... inputs) {
		ProxyServer bungee =  ((ProxyServer)instance.getGameServer());
		String command = (String) inputs[0];
		bungee.getPluginManager().dispatchCommand(bungee.getConsole(), command);
		return true;
	}
}
