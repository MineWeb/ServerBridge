package fr.vmarchaud.mineweb.bridge.methods.common;

import com.google.gson.JsonElement;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.MethodHandler;

@MethodHandler
public class CommonIsConnected implements IMethod {
    @Override
    public Object execute(ICore core, Object... inputs) {
        if (inputs.length == 0) {
            return false;
        }
        JsonElement el = (JsonElement) inputs[0];
        String name = el.isJsonPrimitive() ? el.getAsString() : null;
        if (name == null) {
            return false;
        }
        return core.getPlayers().contains(name);
    }
}
