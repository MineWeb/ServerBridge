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
