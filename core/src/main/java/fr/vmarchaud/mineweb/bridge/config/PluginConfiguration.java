package fr.vmarchaud.mineweb.bridge.config;

import com.google.gson.Gson;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import lombok.Data;

import java.io.*;

@Data
public class PluginConfiguration {

    private transient File path;

    private String logLevel = "INFO";
    private String secretKey;
    private String motd;
    private String domain;
    private Integer port;

    public PluginConfiguration(File path) {
        this.path = path;
    }

    public static PluginConfiguration load(File path, Gson gson, ICore loggerCore) {
        if (path.exists()) {
            FileReader reader = null;
            try {
                reader = new FileReader(path);
                PluginConfiguration conf = gson.fromJson(reader, PluginConfiguration.class);
                conf.path = path;
                return conf;
            } catch (Exception e) {
                loggerCore.logger().warning("Config file invalid, creating new one " + e.getMessage());
                return new PluginConfiguration(path);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        } else {
            loggerCore.logger().warning("Config file not found, creating it");
            PluginConfiguration config = new PluginConfiguration(path);
            config.save(gson, loggerCore);
            return config;
        }
    }

    public void save(Gson gson, ICore core) {
        try {
            File parent = path.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            if (!path.exists()) {
                path.createNewFile();
            }
            String json = gson.toJson(this);
            FileWriter writer = new FileWriter(path);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            core.logger().severe("Cannot save config file " + e.getMessage());
        }
    }
}
