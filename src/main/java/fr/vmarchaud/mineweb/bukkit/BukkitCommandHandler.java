package fr.vmarchaud.mineweb.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.utils.http.MinewebInteractor;
import fr.vmarchaud.mineweb.utils.http.RetrieveKeyResponse;
import fr.vmarchaud.mineweb.utils.http.RetrieveKeyResponse.KeyResult;
import net.md_5.bungee.api.ChatColor;

public class BukkitCommandHandler implements CommandExecutor {
	
	private ICore api;
	
	public BukkitCommandHandler(ICore api) {
		this.api = api;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
		switch(args.length) {
			case 2: {
				// if the user want to setup a new link
				if (args[0].equals("init")) {
					// invalid key
					if (args[1].length() != 24) {
						sender.sendMessage(ChatColor.RED + "bad activation key");
						return true;
					}
					// validate against api
					try {
						RetrieveKeyResponse response = MinewebInteractor.retrieveKey(api, args[1]);
						
						// api send us that the key isnt valid
						if (response.getResult() != KeyResult.VALID) {
							sender.sendMessage(ChatColor.RED + "api returned " + response.getResult());
							sender.sendMessage(ChatColor.RED + "NOT_FOUND = activation key isnt valid | BANNED = your license has been suspended");
							return true;
						}
						
						
						api.config().setSecretkey(response.getKey());
						api.config().setActivationKey(args[1]);
						api.requestHandler().refreshKey(response.getKey());
						sender.sendMessage(ChatColor.GREEN + "Setup finished, you may use the link now.");
						return true;
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED + "cant retrieve the secret key from mineweb.org");
						return true;
					}
				}
			}
			default : {
				sender.sendMessage(ChatColor.GREEN + "mineweb_bridge by ThisIsMac");
				return true;
			}
		}
	}

}
