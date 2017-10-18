package fr.vmarchaud.mineweb.common.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.reflect.TypeToken;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.ScheduledCommand;
import lombok.Data;

@Data
public class ScheduledStorage{

	public File path;
	private Set<ScheduledCommand>	commands = new HashSet<ScheduledCommand>();
	private final static TypeToken<Set<ScheduledCommand>> token = new TypeToken<Set<ScheduledCommand>>(){};
	
	public ScheduledStorage(File path) {
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
	public static ScheduledStorage load(File path, ICore api) {
		if (path.exists()) {
			FileReader reader = null;
			try {
				reader = new FileReader(path);
				ScheduledStorage conf = api.gson().fromJson(reader, token.getType());
				conf.path = path;
				return conf;
			} catch (Exception e) {
				//api.logger().warning("Config file is invalid, replacing with a new one");
				return new ScheduledStorage(path);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {}
				}
			}
		} else {
			//api.logger().warning("Cant find a config file, creating it");
			return new ScheduledStorage(path);
		}
	}
	
	/**
	 * Save the configuration to the file
	 * @param ICore interface for logging and use gson instance
	 */
	public void save(ICore api) {
		try {
			String config = api.gson().toJson(commands);
			FileWriter writer = new FileWriter(path);
			writer.write(config);
			writer.close();
		} catch (IOException e) {
			api.logger().severe("Cant save the config file " + e.getMessage());
		}
	}
}
