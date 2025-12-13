package fr.vmarchaud.mineweb.bridge.methods.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.MethodHandler;
import fr.vmarchaud.mineweb.bridge.scheduler.CommandScheduler;
import fr.vmarchaud.mineweb.bridge.scheduler.ScheduledCommand;

@MethodHandler
public class CommonScheduledCommand implements IMethod {
    @Override
    public Object execute(ICore core, Object... inputs) {
        if (inputs.length == 0) {
            return false;
        }
        JsonElement el = (JsonElement) inputs[0];
        if (!el.isJsonObject()) {
            return false;
        }
        JsonObject obj = el.getAsJsonObject();
        String command = obj.has("command") ? obj.get("command").getAsString() : null;
        String player = obj.has("player") ? obj.get("player").getAsString() : null;
        long timestamp = obj.has("timestamp") ? obj.get("timestamp").getAsLong() : 0L;
        if (command == null || timestamp <= 0L) {
            return false;
        }
        CommandScheduler scheduler = core.getCommandScheduler();
        scheduler.getQueue().add(new ScheduledCommand(command, player, timestamp));
        return true;
    }
}
