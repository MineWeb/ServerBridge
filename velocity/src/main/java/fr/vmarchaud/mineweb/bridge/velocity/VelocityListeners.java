package fr.vmarchaud.mineweb.bridge.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import net.kyori.adventure.text.Component;

public class VelocityListeners {

    private final ICore core;

    public VelocityListeners(ICore core) {
        this.core = core;
    }

    @Subscribe
    public void onLogin(PostLoginEvent event) {
        core.getPlayers().add(event.getPlayer().getUsername());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        core.getPlayers().remove(event.getPlayer().getUsername());
    }

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        String motd = core.getConfiguration().getMotd();
        if (motd == null || motd.isEmpty()) {
            return;
        }
        String replaced = motd.replace("&", "§").replace("{PLAYERS}", String.valueOf(core.getPlayers().size()));
        ServerPing original = event.getPing();
        ServerPing.Builder builder = original.asBuilder();
        builder.description(Component.text(replaced));
        event.setPing(builder.build());
    }
}
