package fr.vmarchaud.mineweb.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import fr.vmarchaud.mineweb.common.Configuration;
import fr.vmarchaud.mineweb.common.ICore;

public class ConfigurationUtils {
	
	/**
	 * Read all content of a file 
	 * @param File object that point to the file 
	 * @return String containing all the content
	 */
	public static String readConfig(File path) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		    return "";
		} finally {
		    try {
				br.close();
			} catch (IOException e) { }
		}
	}
	
	/**
	 * Create a default config in the desired path
	 * @param File object representing the path of the file
	 * @param ICore interface for logging
	 */
	public static void	createDefault(File path, ICore api) {
		System.out.println("yo");
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		try {
			gson.toJson(new Configuration(), Configuration.class, new FileWriter(path));
		} catch (JsonIOException e) {
			api.logger().log(Level.SEVERE, "Cant write the configuration", e);
		} catch (IOException e) {
			api.logger().log(Level.SEVERE, "Cant write the configuration", e);
		}
	}
}
