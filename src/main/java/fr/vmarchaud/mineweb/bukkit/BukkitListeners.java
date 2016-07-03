package fr.vmarchaud.mineweb.bukkit;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.vmarchaud.mineweb.common.ICore;

public class BukkitListeners implements Listener {
	
	private ICore api;
	
	public BukkitListeners(ICore api) {
		this.api = api;
		
		// In case of the plugin has been reloaded, get already connected players
		Server game = (Server) api.getGameServer();
		for(Player player : BukkitUtils.getPlayerList(game)) {
			api.getPlayers().add(player.getName());
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		// update our cached player list
		api.getPlayers().add(e.getPlayer().getName());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		// update our cached player list
		api.getPlayers().remove(e.getPlayer().getName());
	}
}
