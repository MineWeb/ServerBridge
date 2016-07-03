package fr.vmarchaud.mineweb.bukkit;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.BanEntry;
import org.bukkit.BanList.Type;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import com.google.gson.JsonObject;

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
		return api.getPlayers().size();
	}

	@Override
	public int getMaxPlayers() {
		return server.getMaxPlayers();
	}

	@Override
	public boolean isConnected(String name) {
		for(String player : api.getPlayers()) {
			if (player.equals(name))
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
	
	@Override
	public Set<String> getPlayerList() {
		return api.getPlayers();
	}

	@Override
	public String getMOTD() {
		return server.getMotd();
	}

	@Override
	public Set<String> getWhitelist() {
		Set<String>	whitelisted = new HashSet<String>();
		for(OfflinePlayer player : server.getWhitelistedPlayers())
			whitelisted.add(player.getName());
		return whitelisted;
	}

	@Override
	public Set<JsonObject> getBannedlist() {
		Set<JsonObject>	banned = new HashSet<JsonObject>();
		
		for(BanEntry entry : server.getBanList(Type.NAME).getBanEntries()) {
			JsonObject object = new JsonObject();
			object.addProperty("player", entry.getTarget());
			object.addProperty("by", entry.getSource());
			object.addProperty("for", entry.getReason());
			object.addProperty("at", entry.getCreated().getTime());
			object.addProperty("until", entry.getExpiration().getTime());
			banned.add(object);
		}
		return banned;
	}

}
