package fr.vmarchaud.mineweb.bukkit;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class BukkitUtils {
		
	/**
	 * Get access to the internal player list of bukkit
	 * 
	 * @param {@link org.bukkit.Server} instance of the server
	 * @return internal List<Player> of Bukkit 
	 */
	@SuppressWarnings("unchecked")
	public static List<Player> getPlayerList(Server server) {
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
