package fr.vmarchaud.mineweb.bridge.methods.bukkit;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.MethodHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@MethodHandler
public class BukkitGetWhitelistedPlayers implements IMethod {
    @Override
    public Object execute(ICore core, Object... inputs) {
        Server server = (Server) core.getGameServer();
        Set<OfflinePlayer> set = server.getWhitelistedPlayers();
        return set.stream().map(OfflinePlayer::getName).collect(Collectors.toList());
    }
}
