package fr.vmarchaud.mineweb.bridge.methods.bukkit;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.MethodHandler;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@MethodHandler
public class BukkitGetBannedPlayers implements IMethod {
    @Override
    public Object execute(ICore core, Object... inputs) {
        Server server = (Server) core.getGameServer();
        BanList banList = server.getBanList(BanList.Type.NAME);
        List<BanEntry> entries = new ArrayList<>(banList.getBanEntries());
        return entries.stream().map(BanEntry::getTarget).collect(Collectors.toList());
    }
}
