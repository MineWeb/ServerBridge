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
package fr.vmarchaud.mineweb.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.injector.NettyInjector;
import fr.vmarchaud.mineweb.common.injector.router.RouteMatcher;
import fr.vmarchaud.mineweb.utils.Handler;
import fr.vmarchaud.mineweb.utils.http.HttpResponseBuilder;
import fr.vmarchaud.mineweb.utils.http.RoutedHttpRequest;
import fr.vmarchaud.mineweb.utils.http.RoutedHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

public class BukkitCore extends JavaPlugin implements ICore {
	
	RouteMatcher			httpRouter;
	NettyInjector			injector;
	
	@Override
	public void onEnable() {
		// Init
		injector = new BukkitNettyInjector(this);
		injector.inject();
		httpRouter = new RouteMatcher();
		registerRoutes();
	}

	public void registerRoutes() {
		httpRouter.everyMatch(new Handler<Void, RoutedHttpResponse>() {
			
			@Override
			public Void handle(RoutedHttpResponse event) {
				System.out.println("[HTTP] " + event.getRes().getStatus().code() + " " + event.getRequest().getMethod().toString() + " " + event.getRequest().getUri());
				return null;
			}
		});
		
		httpRouter.get("/", new Handler<FullHttpResponse, RoutedHttpRequest>() {
            @Override
            public FullHttpResponse handle(RoutedHttpRequest event) {
                return new HttpResponseBuilder().text("hello world").build();
            }
        });
	}

	@Override
	public RouteMatcher getHTTPRouter() {
		return httpRouter;
	}

	@Override
	public Object getPlugin() {
		return this;
	}

	@Override
	public EnumPluginType getType() {
		return EnumPluginType.BUKKIT;
	}

	@Override
	public Object getGameServer() {
		return this.getServer();
	}

}
