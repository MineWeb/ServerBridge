package fr.vmarchaud.mineweb.bridge.methods.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.MethodHandler;

@MethodHandler
public class VelocityGetMaxPlayers implements IMethod {
    @Override
    public Object execute(ICore core, Object... inputs) {
        ProxyServer proxy = (ProxyServer) core.getGameServer();
        return proxy.getConfiguration().getShowMaxPlayers();
    }
}
