/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2016 Valentin 'ThisIsMac' Marchaud
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package fr.vmarchaud.mineweb.bungee;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.vmarchaud.mineweb.bungee.methods.BungeeGetMOTD;
import fr.vmarchaud.mineweb.bungee.methods.BungeeGetMaxPlayers;
import fr.vmarchaud.mineweb.bungee.methods.BungeeGetVersion;
import fr.vmarchaud.mineweb.common.Configuration;
import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.IMethod;
import fr.vmarchaud.mineweb.common.RequestHandler;
import fr.vmarchaud.mineweb.common.injector.NettyInjector;
import fr.vmarchaud.mineweb.common.injector.router.RouteMatcher;
import fr.vmarchaud.mineweb.common.methods.CommonGetPlayerCount;
import fr.vmarchaud.mineweb.common.methods.CommonGetPlayerList;
import fr.vmarchaud.mineweb.common.methods.CommonIsConnected;
import fr.vmarchaud.mineweb.common.methods.CommonPluginType;
import fr.vmarchaud.mineweb.utils.CustomLogFormatter;
import fr.vmarchaud.mineweb.utils.http.HttpResponseBuilder;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCore extends Plugin implements ICore {
	
	public static ICore		instance;
	public static ICore get() {
		return instance;
	}

	private RouteMatcher				httpRouter;
	private NettyInjector				injector;
	private HashMap<String, IMethod>	methods;
	private RequestHandler				requestHandler;
	private Configuration				config;
	
	/** Cached player list to not rely on Reflection on every request **/
	private HashSet<String>				players;
	
	private Logger						logger		= Logger.getLogger("Mineweb");
	
	private Gson						gson 		= new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	
	public void onEnable() {
		instance = this;

		// load config
		Configuration.CONFIGURATION_PATH = new File(getDataFolder(), "config.json");
		config = Configuration.load(instance);
		// directly setup logger
		setupLogger();
		
		// Init
		logger.info("Loading ...");
		methods = new HashMap<String, IMethod>();
		players = new HashSet<String>();
		injector = new BungeeNettyInjector(this);
		httpRouter = new RouteMatcher();
		logger.info("Registering route ...");
		registerRoutes();
		getProxy().getPluginManager().registerListener(this, new BungeeListeners(instance));
		
		// inject when we are ready
		logger.info("Injecting http server ...");
		injector.inject();
		logger.info("Registering methods ...");
		requestHandler = new RequestHandler(instance);
		registerMethods();
		logger.info("Ready !");
	}

	public void registerRoutes() {
		httpRouter.everyMatch((event) -> {
			logger.fine(String.format("[HTTP Request] %d %s on %s", event.getRes().getStatus().code(),
					event.getRequest().getMethod().toString(), event.getRequest().getUri()));
			return null;
		});
		
		httpRouter.get("/", (event) -> {
            return HttpResponseBuilder.ok();
        });
	}
	
	public void registerMethods() {
		// common methods
		methods.put("GET_PLAYER_LIST", new CommonGetPlayerList());
		methods.put("GET_PLAYER_COUNT", new CommonGetPlayerCount());
		methods.put("IS_CONNECTED", new CommonIsConnected());
		methods.put("GET_PLUGIN_TYPE", new CommonPluginType());
		
		// bungee methods
		methods.put("GET_MAX_PLAYERS", new BungeeGetMaxPlayers());
		methods.put("GET_MOTD", new BungeeGetMOTD());
		methods.put("GET_VERSION", new BungeeGetVersion());
	}
	
	public void setupLogger() {
		try {
			logger.setLevel(Level.parse(config.getLogLevel()));
			logger.setUseParentHandlers(false);
			new File(getDataFolder() + File.separator).mkdirs();
			FileHandler	fileHandler = new FileHandler(getDataFolder() + File.separator + "mineweb.log", true);
			fileHandler.setFormatter(new CustomLogFormatter());
			logger.addHandler(fileHandler);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public RouteMatcher getHTTPRouter() {
		return httpRouter;
	}

	@Override
	public Object getGameServer() {
		return this.getProxy();
	}

	@Override
	public Object getPlugin() {
		return this;
	}
	
	@Override
	public EnumPluginType getType() {
		return EnumPluginType.BUNGEE;
	}

	@Override
	public HashSet<String> getPlayers() {
		return players;
	}
	
	@Override
	public Logger logger() {
		return logger;
	}

	@Override
	public Gson gson() {
		return gson;
	}
	
	@Override
	public Map<String, IMethod> getMethods() {
		return methods;
	}

	@Override
	public Configuration config() {
		return config;
	}

	@Override
	public RequestHandler requestHandler() {
		return requestHandler;
	}
}
