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
package fr.vmarchaud.mineweb.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import com.google.gson.GsonBuilder;
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
		String config = new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(new Configuration());
		try {
			FileWriter writer = new FileWriter(path);
			writer.write(config);
			writer.close();
		} catch (IOException e) {
			api.logger().log(Level.SEVERE, "Cant write the configuration", e);
		}
	}
}
