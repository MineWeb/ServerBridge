package fr.vmarchaud.mineweb.bridge.methods.bukkit;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.MethodHandler;
import org.bukkit.Server;

@MethodHandler
public class BukkitGetVersion implements IMethod {
    @Override
    public Object execute(ICore core, Object... inputs) {
        Server server = (Server) core.getGameServer();
        return server.getVersion();
    }
}
