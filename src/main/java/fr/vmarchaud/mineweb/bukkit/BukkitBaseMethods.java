package fr.vmarchaud.mineweb.bukkit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.mysql.fabric.xmlrpc.base.Array;

import fr.vmarchaud.mineweb.common.IBaseMethods;
import fr.vmarchaud.mineweb.common.ICore;

public class BukkitBaseMethods implements IBaseMethods {
	
	private ICore 	api;
	private Server	server;
	
	public BukkitBaseMethods(ICore api) {
		this.api = api;
		this.server = (Server) api.getGameServer();
	}

	@Override
	public int getPlayers() {
		return getPlayerList().size();
	}

	@Override
	public int getMaxPlayers() {
		return server.getMaxPlayers();
	}

	@Override
	public boolean isConnected(String name) {
		for(Player player : getPlayerList()) {
			if (player.getName().equals(name))
				return true;
		}
		return false;
	}

	@Override
	public String getPluginType() {
		return api.getType().toString();
	}

	@Override
	public String getVersion() {
		return server.getBukkitVersion();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Player> getPlayerList() {
		List<Player>	players = new ArrayList<Player>();
		try {
			Method getCount = server.getClass().getMethod("getOnlinePlayers");
			
			// add all player into our list
			if (getCount.getReturnType() == Array.class) 
				players.addAll(Arrays.asList((Player[]) getCount.invoke(server)));
			else
				players.addAll((Collection<? extends Player>) getCount.invoke(server));
			
		// silent cause we are sure that at least one exist
		} catch (Exception e) {} 
		
		return players;
	}

}
