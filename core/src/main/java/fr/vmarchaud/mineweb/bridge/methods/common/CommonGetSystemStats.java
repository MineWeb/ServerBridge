package fr.vmarchaud.mineweb.bridge.methods.common;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.MethodHandler;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

@MethodHandler
public class CommonGetSystemStats implements IMethod {
    @Override
    public Object execute(ICore core, Object... inputs) {
        Map<String, Object> map = new HashMap<>();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        map.put("osName", os.getName());
        map.put("osArch", os.getArch());
        map.put("availableProcessors", os.getAvailableProcessors());
        map.put("systemLoadAverage", os.getSystemLoadAverage());
        map.put("runtimeMaxMemory", Runtime.getRuntime().maxMemory());
        map.put("runtimeTotalMemory", Runtime.getRuntime().totalMemory());
        map.put("runtimeFreeMemory", Runtime.getRuntime().freeMemory());
        return map;
    }
}
