package fr.vmarchaud.mineweb.bungee;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonObject;

import fr.vmarchaud.mineweb.common.IBaseMethods;
import fr.vmarchaud.mineweb.common.ICore;
import net.md_5.bungee.api.ProxyServer;

public class BungeeBaseMethods implements IBaseMethods {
	
	private ICore 		api;
	private ProxyServer	server;
	
	public BungeeBaseMethods(ICore api) {
		this.api = api;
		this.server = (ProxyServer) api.getGameServer();
	}

	@Override
	public int getPlayers() {
		return server.getOnlineCount();
	}

	@Override
	public int getMaxPlayers() {
		return server.getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers();
	}

	@Override
	public boolean isConnected(String name) {
		return server.getPlayer(name) == null ? false : true;
	}

	@Override
	public String getPluginType() {
		return api.getType().toString();
	}

	@Override
	public String getVersion() {
		return server.getVersion();
	}
	
	@Override
	public Set<String> getPlayerList() {
		return api.getPlayers();
	}

	@Override
	public String getMOTD() {
		return server.getConfigurationAdapter().getListeners().iterator().next().getMotd();
	}

	@Override
	public Set<String> getWhitelist() {
		return new HashSet<String>();
	}

	@Override
	public Set<JsonObject> getBannedlist() {
		return new HashSet<JsonObject>();
	}

}
