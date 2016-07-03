package fr.vmarchaud.mineweb.common;

import java.util.Set;

import com.google.gson.JsonObject;

public interface IBaseMethods {
	
	// get
	public int				getPlayers();
	public int				getMaxPlayers();
	public Set<String>		getPlayerList();
	public boolean			isConnected(String name);
	public String			getPluginType();
	public String			getVersion();
	public String			getMOTD();
	public Set<String>		getWhitelist();
	public Set<JsonObject>	getBannedlist();
	
	
	// 
	
	
	
}
