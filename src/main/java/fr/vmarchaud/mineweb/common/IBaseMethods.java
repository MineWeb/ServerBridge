package fr.vmarchaud.mineweb.common;

import java.util.List;

import org.bukkit.entity.Player;

public interface IBaseMethods {
	
	public int				getPlayers();
	public int				getMaxPlayers();
	public List<Player>		getPlayerList();
	public boolean			isConnected(String name);
	public String			getPluginType();
	public String			getVersion();
	
	
	
}
