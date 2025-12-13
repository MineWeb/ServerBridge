package fr.vmarchaud.mineweb.bridge.velocity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.vmarchaud.mineweb.bridge.api.EnumPluginType;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.IMethod;
import fr.vmarchaud.mineweb.bridge.api.RequestHandler;
import fr.vmarchaud.mineweb.bridge.config.PluginConfiguration;
import fr.vmarchaud.mineweb.bridge.config.ScheduledStorage;
import fr.vmarchaud.mineweb.bridge.http.HttpServer;
import fr.vmarchaud.mineweb.bridge.http.router.RouteMatcher;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonGetPlayerCount;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonGetPlayerList;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonGetSystemStats;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonGetTimestamp;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonIsConnected;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonPluginType;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonRunCommand;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonScheduledCommand;
import fr.vmarchaud.mineweb.bridge.methods.common.CommonSetMotd;
import fr.vmarchaud.mineweb.bridge.methods.velocity.VelocityGetMOTD;
import fr.vmarchaud.mineweb.bridge.methods.velocity.VelocityGetMaxPlayers;
import fr.vmarchaud.mineweb.bridge.methods.velocity.VelocityGetVersion;
import fr.vmarchaud.mineweb.bridge.scheduler.CommandScheduler;
import fr.vmarchaud.mineweb.bridge.util.CustomLogFormatter;
import net.kyori.adventure.text.Component;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


@Plugin(id = "minewebbridge", name = "MineWebBridge", version = "4.0.0")
public class VelocityBridgePlugin implements ICore {

    final ProxyServer proxy;
    final Path dataDirectory;

    private final Set<String> players = new HashSet<>();
    private final Map<String, IMethod> methods = new HashMap<>();

    private PluginConfiguration config;
    private CommandScheduler scheduler;
    private RouteMatcher router;
    private HttpServer httpServer;
    private VelocityNettyInjector injector;
    private Gson gson;
    private Logger logger;
    private FileHandler fileHandler;
    private RequestHandler requestHandler;

    @Inject
    public VelocityBridgePlugin(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        File folder = dataDirectory.toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        this.gson = new GsonBuilder().serializeNulls().create();
        this.logger = Logger.getLogger("MineWebBridge");
        this.config = PluginConfiguration.load(new File(folder, "config.json"), gson, this);
        ScheduledStorage storage = ScheduledStorage.load(new File(folder, "commands.json"), gson, this);
        setupLogger(folder);
        initPlayersCache();
        this.router = new RouteMatcher();
        this.requestHandler = new RequestHandler(this);
        registerRoutes();
        registerMethods();
        this.scheduler = new CommandScheduler(this, storage);
        proxy.getScheduler().buildTask(this, scheduler).repeat(5, TimeUnit.SECONDS).schedule();
        startHttpOrInject();
        proxy.getEventManager().register(this, new VelocityListeners(this));
        proxy.getCommandManager().register(
                proxy.getCommandManager().metaBuilder("mineweb").plugin(this).build(),
                new MinewebCommand(this)
        );
        logger.info("MineWebBridge Velocity ready");
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
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
        logger.info("MineWebBridge Velocity stopped");
    }

    private void setupLogger(File folder) {
        try {
            logger.setLevel(Level.parse(config.getLogLevel()));
            logger.setUseParentHandlers(false);
            folder.mkdirs();
            fileHandler = new FileHandler(new File(folder, "mineweb.log").getPath(), true);
            fileHandler.setFormatter(new CustomLogFormatter());
            logger.addHandler(fileHandler);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to setup logger: " + e.getMessage(), e);
        }
    }

    private void initPlayersCache() {
        proxy.getAllPlayers().forEach(p -> players.add(p.getUsername()));
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
        methods.put("GET_MAX_PLAYERS", new VelocityGetMaxPlayers());
        methods.put("GET_MOTD", new VelocityGetMOTD());
        methods.put("GET_VERSION", new VelocityGetVersion());
    }

    private void startHttpOrInject() {
        Integer port = config.getPort();
        if (port == null) {
            injector = new VelocityNettyInjector(this, proxy);
            try {
                injector.inject();
                logger.info("MineWebBridge injected HTTP handler into Velocity server channel");
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
        return EnumPluginType.VELOCITY;
    }

    @Override
    public Object getGameServer() {
        return proxy;
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
        proxy.getCommandManager().executeAsync(proxy.getConsoleCommandSource(), command);
    }

    private static final class MinewebCommand implements SimpleCommand {

        private final VelocityBridgePlugin plugin;

        public MinewebCommand(VelocityBridgePlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] args = invocation.arguments();
            if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
                File folder = plugin.dataDirectory.toFile();
                plugin.config = new PluginConfiguration(new File(folder, "config.json"));
                plugin.config.save(plugin.gson, plugin);
                if (plugin.requestHandler != null) {
                    plugin.requestHandler.resetKey();
                }
                source.sendMessage(Component.text("MineWebBridge configuration reset"));
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
                    source.sendMessage(Component.text("MineWebBridge HTTP port set to " + port));
                    plugin.logger.info("MineWebBridge port set to " + port);
                } catch (Exception e) {
                    source.sendMessage(Component.text("Failed to set port " + e.getMessage()));
                }
                return;
            }
            source.sendMessage(Component.text("Usage: /mineweb reset | /mineweb port <port>"));
        }
    }
}
