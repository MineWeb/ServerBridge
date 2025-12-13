package fr.vmarchaud.mineweb.bridge.methods.common;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.MethodHandler;

@MethodHandler
public class CommonGetTimestamp implements IMethod {
    @Override
    public Object execute(ICore core, Object... inputs) {
        return System.currentTimeMillis();
    }
}
