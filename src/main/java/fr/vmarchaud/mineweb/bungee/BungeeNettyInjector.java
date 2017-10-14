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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.injector.JSONAPIChannelDecoder;
import fr.vmarchaud.mineweb.common.injector.NettyInjector;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.KickStringWriter;
import net.md_5.bungee.protocol.LegacyDecoder;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.Protocol;

public class BungeeNettyInjector extends NettyInjector {

	private ICore api;

	public BungeeNettyInjector(ICore api) {
		this.api = api;
	}

	public synchronized void inject() {
		if (injected)
			throw new IllegalStateException("Cannot inject twice.");

		try {
			// get the field that will setup the channel and inject our handler
			Class<PipelineUtils> server = PipelineUtils.class;
			Field field = server.getDeclaredField("SERVER_CHILD");
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			// set the new value
			field.set(null, new ChannelInitializer<Channel>() {
				
				@SuppressWarnings("deprecation")
				protected void initChannel(Channel ch) throws Exception {
					// inject here
					injectChannel(ch);
					
					// and let the original code run
					ListenerInfo listener = ch.attr( PipelineUtils.LISTENER ).get();

					PipelineUtils.BASE.initChannel(ch);
					ch.pipeline().addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, new LegacyDecoder());
					ch.pipeline().addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder(Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion()));
					ch.pipeline().addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder(Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion()));
					ch.pipeline().addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, new KickStringWriter());
					ch.pipeline().get(HandlerBoss.class) .setHandler(new InitialHandler(BungeeCord.getInstance(), listener));

					//if (listener.getClass().getMethod("isProxyProtocol").toString() != null && listener.isProxyProtocol())
					//{
					//	ch.pipeline().addFirst(new io.netty.handler.codec.haproxy.HAProxyMessageDecoder.HAProxyMessageDecoder());
					//}
				}
			});
			injected = true;

		} catch (Exception e) {
			throw new RuntimeException("Unable to inject channel futures.", e);
		}
	}

	@Override
	protected void injectChannel(final Channel channel) {
		channel.pipeline().addFirst(new JSONAPIChannelDecoder(api));
	}
}
