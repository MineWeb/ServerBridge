/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2016 Valentin 'ThisIsMac' Marchaud
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package fr.vmarchaud.mineweb.bukkit;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

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
	
	@EventHandler
    public void onPing(ServerListPingEvent e) {
        String motd = api.config().motd;
        if(motd == null || motd.length() == 0)
        	return;
        motd = motd.replace("&", "§").replace("{PLAYERS}", String.valueOf(api.getPlayers().size()));
        e.setMotd(motd);
    }
}
