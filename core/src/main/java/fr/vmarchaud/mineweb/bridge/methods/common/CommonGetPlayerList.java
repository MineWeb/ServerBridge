package fr.vmarchaud.mineweb.bridge.methods.common;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.MethodHandler;

import java.util.ArrayList;

@MethodHandler
public class CommonGetPlayerList implements IMethod {
    @Override
    public Object execute(ICore core, Object... inputs) {
        return new ArrayList<>(core.getPlayers());
    }
}
