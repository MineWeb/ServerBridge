package fr.vmarchaud.mineweb.common.configuration;

import fr.vmarchaud.mineweb.common.ICore;
import lombok.Data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;

@Data
public class PluginConfiguration {

	public transient File path;

	public String logLevel = "FINE";
	public String secretkey;
	public String motd;
	public String domain;
	public Integer port;

	public PluginConfiguration(File path) {
		this.path = path;
	}

	/**
	 * Load the configuration from the file
	 * 
	 * @param path object representing the path of the file
	 * @param api interface for logging and use gson instance
	 */
	public static PluginConfiguration load(File path, ICore api) {
		if (path.exists()) {
			FileReader reader = null;
			try {
				reader = new FileReader(path);
				PluginConfiguration conf = api.gson().fromJson(reader, PluginConfiguration.class);
				conf.path = path;
				return conf;
			} catch (Exception e) {
				api.logger().warning("Config file is invalid, replacing with a new one (" + e.getMessage() + ")");
				return new PluginConfiguration(path);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			api.logger().warning("Cant find a config file, creating it");
			PluginConfiguration config = new PluginConfiguration(path);
			config.save(api);
			return config;
		}
	}

	/**
	 * Save the configuration to the file
	 * 
	 * @param api interface for logging and use gson instance
	 */
	public void save(ICore api) {
		try {
			// Create the folder
			new File(path.getParent()).mkdirs();
			// Create the file
			path.createNewFile();
			// Write it
			String config = api.gson().toJson(this);
			FileWriter writer = new FileWriter(path);
			writer.write(config);
			writer.close();
		} catch (IOException e) {
			api.logger().severe("Cant save the config file " + e.getMessage());
		}
	}
}
