package fr.vmarchaud.mineweb.bridge.bukkit;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class BukkitListeners implements Listener {

    private final ICore core;

    public BukkitListeners(ICore core) {
        this.core = core;
        Server server = (Server) core.getGameServer();
        for (Player p : server.getOnlinePlayers()) {
            core.getPlayers().add(p.getName());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        core.getPlayers().add(e.getPlayer().getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        core.getPlayers().remove(e.getPlayer().getName());
    }

    @EventHandler
    public void onPing(ServerListPingEvent e) {
        String motd = core.getConfiguration().getMotd();
        if (motd == null || motd.isEmpty()) {
            return;
        }
        String withPlayers = motd.replace("&", "§").replace("{PLAYERS}", String.valueOf(core.getPlayers().size()));
        e.setMotd(withPlayers);
    }
}
