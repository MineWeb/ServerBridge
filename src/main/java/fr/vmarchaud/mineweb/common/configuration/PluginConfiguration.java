package fr.vmarchaud.mineweb.common.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import fr.vmarchaud.mineweb.common.ICore;
import lombok.Data;

@Data
public class PluginConfiguration {
	
	public transient File path;

	public String logLevel = "FINE";
	public String secretkey;
	public String licenseId;
	public String licenseKey;
	public String domain;
	
	public PluginConfiguration(File path) {
		this.path = path;
	}
	
	/**
	 * Load the configuration from the file
	 * @param File object representing the path of the file
	 * @param ICore interface for logging and use gson instance
	 * 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
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
				api.logger().warning("Config file is invalid, replacing with a new one");
				return new PluginConfiguration(path);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {}
				}
			}
		} else {
			api.logger().warning("Cant find a config file, creating it");
			return new PluginConfiguration(path);
		}
	}
	
	/**
	 * Save the configuration to the file
	 * @param ICore interface for logging and use gson instance
	 */
	public void save(ICore api) {
		try {
			String config = api.gson().toJson(this);
			FileWriter writer = new FileWriter(path);
			writer.write(config);
			writer.close();
		} catch (IOException e) {
			api.logger().severe("Cant save the config file " + e.getMessage());
		}
	}

	/**
	 * Reset the configuration to the file
	 */
	public void reset() {
		try {
			String config = "{\"logLevel\":\"FINE\",\"secretkey\":null,\"licenseId\":null,\"licenseKey\":null,\"domain\":null}";
			FileWriter writer = new FileWriter(path);
			writer.write(config);
			writer.close();
		} catch (IOException e) {
			System.out.println("Cant save the config file " + e.getMessage());
		}
	}
}
