package fr.vmarchaud.mineweb.bridge.bukkit;

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
import fr.vmarchaud.mineweb.bridge.methods.bukkit.BukkitGetBannedPlayers;
import fr.vmarchaud.mineweb.bridge.methods.bukkit.BukkitGetMaxPlayers;
import fr.vmarchaud.mineweb.bridge.methods.bukkit.BukkitGetMOTD;
import fr.vmarchaud.mineweb.bridge.methods.bukkit.BukkitGetVersion;
import fr.vmarchaud.mineweb.bridge.methods.bukkit.BukkitGetWhitelistedPlayers;
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
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BukkitBridgePlugin extends JavaPlugin implements ICore {

    private final Set<String> players = new HashSet<>();
    private final Map<String, IMethod> methods = new HashMap<>();

    private PluginConfiguration config;
    private CommandScheduler scheduler;
    private RouteMatcher router;
    private HttpServer httpServer;
    private BukkitNettyInjector injector;
    private BukkitTask task;
    private Gson gson;
    private Logger logger;
    private FileHandler fileHandler;
    private RequestHandler requestHandler;
    private boolean protocolLibEnabled;

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        this.gson = new GsonBuilder().serializeNulls().create();
        this.logger = Logger.getLogger("MineWebBridge");
        this.config = PluginConfiguration.load(new File(getDataFolder(), "config.json"), gson, this);
        ScheduledStorage storage = ScheduledStorage.load(new File(getDataFolder(), "commands.json"), gson, this);
        this.protocolLibEnabled = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
        setupLogger();
        initPlayersCache();
        this.router = new RouteMatcher();
        this.requestHandler = new RequestHandler(this);
        registerRoutes();
        registerMethods();
        this.scheduler = new CommandScheduler(this, storage);
        this.task = getServer().getScheduler().runTaskTimerAsynchronously(this, scheduler, 0L, 100L);
        startHttpOrInject();
        getServer().getPluginManager().registerEvents(new BukkitListeners(this), this);
        logger.info("MineWebBridge Bukkit ready");
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
        logger.info("MineWebBridge Bukkit stopped");
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
            logger.log(Level.SEVERE, "Failed to setup logger " + e.getMessage(), e);
        }
    }

    private void initPlayersCache() {
        Server server = getServer();
        for (Player p : server.getOnlinePlayers()) {
            players.add(p.getName());
        }
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
        methods.put("GET_BANNED_PLAYERS", new BukkitGetBannedPlayers());
        methods.put("GET_MAX_PLAYERS", new BukkitGetMaxPlayers());
        methods.put("GET_MOTD", new BukkitGetMOTD());
        methods.put("GET_VERSION", new BukkitGetVersion());
        methods.put("GET_WHITELISTED_PLAYERS", new BukkitGetWhitelistedPlayers());
    }

    private void startHttpOrInject() {
        Integer port = config.getPort();
        if (port == null) {
            if (!protocolLibEnabled) {
                logger.warning("No port configured and ProtocolLib is not installed, HTTP bridge is disabled");
                return;
            }
            logger.info("No port configured, injecting HTTP handler on Minecraft server port through ProtocolLib");
            injector = new BukkitNettyInjector(this);
            try {
                injector.inject();
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("mineweb")) {
            return false;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            if (sender instanceof Player && !sender.isOp() && !sender.hasPermission("mineweb.reset")) {
                return false;
            }
            config = new PluginConfiguration(new File(getDataFolder(), "config.json"));
            config.save(gson, this);
            if (requestHandler != null) {
                requestHandler.resetKey();
            }
            sender.sendMessage("MineWebBridge configuration reset");
            logger.info("MineWebBridge configuration reset");
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("port")) {
            if (sender instanceof Player && !sender.isOp() && !sender.hasPermission("mineweb.port")) {
                return false;
            }
            try {
                int port = Integer.parseInt(args[1]);
                config.setPort(port);
                config.save(gson, this);
                if (injector != null) {
                    injector.close();
                    injector = null;
                }
                if (httpServer != null) {
                    httpServer.stop();
                    httpServer = null;
                }
                httpServer = new HttpServer(this);
                httpServer.start(port);
                TimeUnit.MILLISECONDS.sleep(5L);
                sender.sendMessage("MineWebBridge HTTP port set to " + port);
                logger.info("MineWebBridge port set to " + port);
                return true;
            } catch (Exception e) {
                sender.sendMessage("Failed to set port " + e.getMessage());
                return true;
            }
        }
        return false;
    }

    @Override
    public EnumPluginType getType() {
        return EnumPluginType.BUKKIT;
    }

    @Override
    public Object getGameServer() {
        return getServer();
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
        getServer().getScheduler().runTask(this, () -> Bukkit.dispatchCommand(getServer().getConsoleSender(), command));
    }
}
