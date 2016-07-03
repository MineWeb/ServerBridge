package fr.vmarchaud.mineweb.bungee;


import fr.vmarchaud.mineweb.common.ICore;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListeners implements Listener {
	
	private ICore api;
	
	public BungeeListeners(ICore api) {
		this.api = api;
		
		// In case of the plugin has been reloaded, get already connected players
		ProxyServer game = (ProxyServer) api.getGameServer();
		for(ProxiedPlayer player : game.getPlayers()) {
			api.getPlayers().add(player.getName());
		}
	}
	
	@EventHandler
	public void onJoin(PostLoginEvent e) {
		// update our cached player list
		api.getPlayers().add(e.getPlayer().getName());
	}
	
	@EventHandler
	public void onQuit(PlayerDisconnectEvent e) {
		// update our cached player list
		api.getPlayers().remove(e.getPlayer().getName());
	}
}
