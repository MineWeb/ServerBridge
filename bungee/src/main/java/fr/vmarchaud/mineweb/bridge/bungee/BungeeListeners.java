package fr.vmarchaud.mineweb.bridge.bungee;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListeners implements Listener {

    private final ICore core;

    public BungeeListeners(ICore core) {
        this.core = core;
        ProxyServer proxy = (ProxyServer) core.getGameServer();
        for (ProxiedPlayer p : proxy.getPlayers()) {
            core.getPlayers().add(p.getName());
        }
    }

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        core.getPlayers().add(e.getPlayer().getName());
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        core.getPlayers().remove(e.getPlayer().getName());
    }

    @EventHandler
    public void onPing(ProxyPingEvent e) {
        String motd = core.getConfiguration().getMotd();
        if (motd == null || motd.isEmpty()) {
            return;
        }
        motd = motd.replace("&", "§").replace("{PLAYERS}", String.valueOf(core.getPlayers().size()));
        ServerPing ping = e.getResponse();
        ping.setDescriptionComponent(new TextComponent(motd));
        e.setResponse(ping);
    }
}
