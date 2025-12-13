package fr.vmarchaud.mineweb.bridge.api;

import com.google.gson.Gson;
import fr.vmarchaud.mineweb.bridge.config.PluginConfiguration;
import fr.vmarchaud.mineweb.bridge.scheduler.CommandScheduler;
import fr.vmarchaud.mineweb.bridge.http.router.RouteMatcher;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public interface ICore {
    EnumPluginType getType();
    Object getGameServer();
    Set<String> getPlayers();
    Map<String, IMethod> getMethods();
    PluginConfiguration getConfiguration();
    CommandScheduler getCommandScheduler();
    RouteMatcher getHttpRouter();
    Gson getGson();
    Logger logger();
    void runCommand(String command);
}
