package fr.vmarchaud.mineweb.bridge.bungee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.vmarchaud.mineweb.bridge.api.EnumPluginType;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.RequestHandler;
import fr.vmarchaud.mineweb.bridge.config.PluginConfiguration;
import fr.vmarchaud.mineweb.bridge.config.ScheduledStorage;
import fr.vmarchaud.mineweb.bridge.http.HttpServer;
import fr.vmarchaud.mineweb.bridge.http.router.RouteMatcher;
import fr.vmarchaud.mineweb.bridge.methods.bungee.BungeeGetMOTD;
import fr.vmarchaud.mineweb.bridge.methods.bungee.BungeeGetMaxPlayers;
import fr.vmarchaud.mineweb.bridge.methods.bungee.BungeeGetVersion;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonGetPlayerCount;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonGetPlayerList;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonGetSystemStats;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonGetTimestamp;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonIsConnected;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonPluginType;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonRunCommand;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonScheduledCommand;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonSetMotd;
import fr.vmarchaud.mineweb.bridge.scheduler.CommandScheduler;
import fr.vmarchaud.mineweb.bridge.util.CustomLogFormatter;
import fr.vmarchaud.mineweb.bridge.util.http.HttpResponseBuilder;
import io.netty.handler.codec.http.FullHttpResponse;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BungeeBridgePlugin extends Plugin implements ICore {

    private final Set<String> players = new HashSet<>();
    private final Map<String, IMethod> methods = new HashMap<>();

    private PluginConfiguration config;
    private CommandScheduler scheduler;
    private RouteMatcher router;
    private HttpServer httpServer;
    private BungeeNettyInjector injector;
    private ScheduledTask task;
    private Gson gson;
    private Logger logger;
    private FileHandler fileHandler;
    private RequestHandler requestHandler;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        this.gson = new GsonBuilder().serializeNulls().create();
        this.logger = Logger.getLogger("MineWebBridge");
        this.config = PluginConfiguration.load(new File(getDataFolder(), "config.json"), gson, this);
        ScheduledStorage storage = ScheduledStorage.load(new File(getDataFolder(), "commands.json"), gson, this);
        setupLogger();
        initPlayersCache();
        this.router = new RouteMatcher();
        this.requestHandler = new RequestHandler(this);
        registerRoutes();
        registerMethods();
        this.scheduler = new CommandScheduler(this, storage);
        this.task = getProxy().getScheduler().schedule(this, scheduler, 5, 5, TimeUnit.SECONDS);
        startHttpOrInject();
        getProxy().getPluginManager().registerListener(this, new BungeeListeners(this));
        getProxy().getPluginManager().registerCommand(this, new MinewebCommand(this));
        logger.info("MineWebBridge Bungee ready");
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
        }
        if (scheduler != null) {
            scheduler.save();
        }
        if (httpServer != null) {
            httpServer.stop();
        }
        if (injector != null) {
            injector.close();
        }
        if (fileHandler != null) {
            fileHandler.close();
        }
        logger.info("MineWebBridge Bungee stopped");
    }

    private void setupLogger() {
        try {
            logger.setLevel(Level.parse(config.getLogLevel()));
            logger.setUseParentHandlers(false);
            File folder = getDataFolder();
            folder.mkdirs();
            fileHandler = new FileHandler(new File(folder, "mineweb.log").getPath(), true);
            fileHandler.setFormatter(new CustomLogFormatter());
            logger.addHandler(fileHandler);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to setup logger: " + e.getMessage(), e);
        }
    }

    private void initPlayersCache() {
        ProxyServer proxy = getProxy();
        proxy.getPlayers().forEach(p -> players.add(p.getName()));
    }

    private void registerRoutes() {
        router.everyMatch(event -> {
            logger.fine("[HTTP] " + event.getRequest().method() + " " + event.getRequest().uri() + " " + event.getRes().status().code());
            return null;
        });
        router.post("/handshake", event -> requestHandler.handleHandshake(event));
        router.post("/ask", event -> requestHandler.handleAsk(event));
    }

    private void registerMethods() {
        methods.put("GET_PLAYER_LIST", new CommonGetPlayerList());
        methods.put("GET_PLAYER_COUNT", new CommonGetPlayerCount());
        methods.put("IS_CONNECTED", new CommonIsConnected());
        methods.put("GET_PLUGIN_TYPE", new CommonPluginType());
        methods.put("GET_SYSTEM_STATS", new CommonGetSystemStats());
        methods.put("RUN_COMMAND", new CommonRunCommand());
        methods.put("RUN_SCHEDULED_COMMAND", new CommonScheduledCommand());
        methods.put("GET_SERVER_TIMESTAMP", new CommonGetTimestamp());
        methods.put("SET_MOTD", new CommonSetMotd());
        methods.put("GET_MAX_PLAYERS", new BungeeGetMaxPlayers());
        methods.put("GET_MOTD", new BungeeGetMOTD());
        methods.put("GET_VERSION", new BungeeGetVersion());
    }

    private void startHttpOrInject() {
        Integer port = config.getPort();
        if (port == null) {
            injector = new BungeeNettyInjector(this);
            try {
                injector.inject();
                logger.info("MineWebBridge injected HTTP handler into Bungee server channel");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to inject Netty HTTP handler " + e.getMessage(), e);
            }
            return;
        }
        try {
            httpServer = new HttpServer(this);
            httpServer.start(port);
            logger.info("HTTP server started on port " + port);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start HTTP server " + e.getMessage(), e);
        }
    }

    @Override
    public EnumPluginType getType() {
        return EnumPluginType.BUNGEE;
    }

    @Override
    public Object getGameServer() {
        return getProxy();
    }

    @Override
    public Set<String> getPlayers() {
        return players;
    }

    @Override
    public Map<String, IMethod> getMethods() {
        return methods;
    }

    @Override
    public PluginConfiguration getConfiguration() {
        return config;
    }

    @Override
    public CommandScheduler getCommandScheduler() {
        return scheduler;
    }

    @Override
    public RouteMatcher getHttpRouter() {
        return router;
    }

    @Override
    public Gson getGson() {
        return gson;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public void runCommand(String command) {
        getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), command);
    }

    private static final class MinewebCommand extends Command {

        private final BungeeBridgePlugin plugin;

        public MinewebCommand(BungeeBridgePlugin plugin) {
            super("mineweb", null);
            this.plugin = plugin;
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
                plugin.config = new PluginConfiguration(new File(plugin.getDataFolder(), "config.json"));
                plugin.config.save(plugin.gson, plugin);
                if (plugin.requestHandler != null) {
                    plugin.requestHandler.resetKey();
                }
                sender.sendMessage(new TextComponent("MineWebBridge configuration reset"));
                plugin.logger.info("MineWebBridge configuration reset");
                return;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("port")) {
                try {
                    int port = Integer.parseInt(args[1]);
                    plugin.config.setPort(port);
                    plugin.config.save(plugin.gson, plugin);
                    if (plugin.injector != null) {
                        plugin.injector.close();
                        plugin.injector = null;
                    }
                    if (plugin.httpServer != null) {
                        plugin.httpServer.stop();
                        plugin.httpServer = null;
                    }
                    plugin.httpServer = new HttpServer(plugin);
                    plugin.httpServer.start(port);
                    sender.sendMessage(new TextComponent("MineWebBridge HTTP port set to " + port));
                    plugin.logger.info("MineWebBridge port set to " + port);
                } catch (Exception e) {
                    sender.sendMessage(new TextComponent("Failed to set port " + e.getMessage()));
                }
                return;
            }
            sender.sendMessage(new TextComponent("Usage: /mineweb reset | /mineweb port <port>"));
        }
    }
}
