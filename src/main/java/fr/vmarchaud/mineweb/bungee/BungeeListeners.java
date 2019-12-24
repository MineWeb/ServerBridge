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
package fr.vmarchaud.mineweb.bungee;


import fr.vmarchaud.mineweb.common.ICore;
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
	
	
	@EventHandler
    public void onPing(ProxyPingEvent e) {
        String motd = api.config().motd;
        if(motd == null || motd.length() == 0)
        	return;
        ServerPing response = e.getResponse();
        motd = motd.replace("&", "�").replace("{PLAYERS}", String.valueOf(api.getPlayers().size()));
        response.setDescriptionComponent(new TextComponent(motd));
        e.setResponse(response);
    }


}
