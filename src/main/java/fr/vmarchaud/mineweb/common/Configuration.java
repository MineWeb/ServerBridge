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
package fr.vmarchaud.mineweb.common;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import fr.vmarchaud.mineweb.utils.ConfigurationUtils;
import lombok.Data;

@Data
public class Configuration {
	
	public static transient File		CONFIGURATION_PATH;
	
	public String		logLevel = "FINE";
	public String		secretkey;
	public String		activationKey;
	
	/**
	 * Load the configuration from the file
	 * @param File object representing the path of the file
	 * @param ICore interface for logging and use gson instance
	 * @return Configuration instance
	 */
	public static Configuration load(ICore api) {
		if (CONFIGURATION_PATH.exists()) {
			try {
				return api.gson().fromJson(new FileReader(CONFIGURATION_PATH), Configuration.class);
			} catch (Exception e) {
				api.logger().warning("Config file is invalid, replacing with a new one");
				ConfigurationUtils.createDefault(CONFIGURATION_PATH, api);
				return new Configuration();
			}
		} else {
			api.logger().warning("Cant find a config file, creating it");
			ConfigurationUtils.createDefault(CONFIGURATION_PATH, api);
			return new Configuration();
		}
	}
	
	/**
	 * Save the configuration to the file
	 * @param File object representing the path of the file
	 * @param ICore interface for logging and use gson instance
	 */
	public void save(ICore api) {
		try {
			String config = api.gson().toJson(this);
			FileWriter writer = new FileWriter(CONFIGURATION_PATH);
			writer.write(config);
			writer.close();
		} catch (IOException e) {
			api.logger().severe("Cant save the config file " + e.getMessage());
		}
	}
}
