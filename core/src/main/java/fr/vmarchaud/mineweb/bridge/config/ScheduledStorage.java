package fr.vmarchaud.mineweb.bridge.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.scheduler.ScheduledCommand;
import lombok.Data;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@Data
public class ScheduledStorage {

    private transient File path;
    private Set<ScheduledCommand> commands = new HashSet<>();

    private static final Type TOKEN = new TypeToken<Set<ScheduledCommand>>() {}.getType();

    public ScheduledStorage(File path) {
        this.path = path;
    }

    public static ScheduledStorage load(File path, Gson gson, ICore core) {
        if (path.exists()) {
            try (FileReader reader = new FileReader(path)) {
                Set<ScheduledCommand> data = gson.fromJson(reader, TOKEN);
                ScheduledStorage storage = new ScheduledStorage(path);
                if (data != null) {
                    storage.commands = data;
                }
                return storage;
            } catch (Exception ignored) {
                return new ScheduledStorage(path);
            }
        } else {
            return new ScheduledStorage(path);
        }
    }

    public void save(Gson gson, ICore core) {
        try {
            File parent = path.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            String json = gson.toJson(commands, TOKEN);
            FileWriter writer = new FileWriter(path);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            core.logger().severe("Cannot save scheduled commands " + e.getMessage());
        }
    }
}
