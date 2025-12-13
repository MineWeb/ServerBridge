package fr.vmarchaud.mineweb.bridge.methods.bungee;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.MethodHandler;
import net.md_5.bungee.api.ProxyServer;

@MethodHandler
public class BungeeGetVersion implements IMethod {
    @Override
    public Object execute(ICore core, Object... inputs) {
        ProxyServer proxy = (ProxyServer) core.getGameServer();
        return proxy.getVersion();
    }
}
