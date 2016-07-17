package fr.vmarchaud.mineweb.common;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonSyntaxException;

import fr.vmarchaud.mineweb.utils.ConfigurationUtils;
import lombok.Data;

@Data
public class Configuration {
	
	public String		logLevel = "FINE";
	public Set<String>	tokens = new HashSet<String>();
	
	/**
	 * Load the configuration from the file
	 * @param File object representing the path of the file
	 * @param ICore interface for logging and use gson instance
	 * @return Configuration instance
	 */
	public static Configuration load(File path, ICore api) {
		if (path.exists()) {
			try {
				return api.gson().fromJson(ConfigurationUtils.readConfig(path), Configuration.class);
			} catch (JsonSyntaxException e) {
				System.out.println(e);
				api.logger().warning("Config file is invalid, replacing with a new one");
				ConfigurationUtils.createDefault(path, api);
				return new Configuration();
			}
		} else {
			api.logger().warning("Cant find a config file, creating it");
			ConfigurationUtils.createDefault(path, api);
			return new Configuration();
		}
	}
}
