package fr.vmarchaud.mineweb.bridge.bungee;

import fr.vmarchaud.mineweb.bridge.api.NettyInjector;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.http.JSONAPIChannelDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import net.md_5.bungee.netty.PipelineUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BungeeNettyInjector implements NettyInjector {

    private final ICore api;
    private boolean injected;
    private ChannelInitializer<Channel> original;

    public BungeeNettyInjector(ICore api) {
        this.api = api;
    }

    @Override
    public synchronized void inject() throws Exception {
        if (injected) {
            throw new IllegalStateException("Cannot inject twice");
        }

        Field field = PipelineUtils.class.getDeclaredField("SERVER_CHILD");
        Field mod = Field.class.getDeclaredField("modifiers");

        mod.setAccessible(true);
        field.setAccessible(true);
        mod.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        @SuppressWarnings("unchecked")
        ChannelInitializer<Channel> current = (ChannelInitializer<Channel>) field.get(null);
        original = current;
        final ChannelInitializer<Channel> delegate = current;

        ChannelInitializer<Channel> wrapped = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addFirst(new JSONAPIChannelDecoder(api));
                invokeOriginalInit(delegate, ch);
            }
        };

        field.set(null, wrapped);
        injected = true;
    }

    @Override
    public synchronized void close() {
        if (!injected || original == null) {
            return;
        }
        try {
            Field field = PipelineUtils.class.getDeclaredField("SERVER_CHILD");
            Field mod = Field.class.getDeclaredField("modifiers");

            mod.setAccessible(true);
            field.setAccessible(true);
            mod.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, original);
        } catch (Exception ignored) {
        } finally {
            injected = false;
        }
    }

    private static void invokeOriginalInit(ChannelInitializer<Channel> delegate, Channel ch) throws Exception {
        Method m = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
        m.setAccessible(true);
        m.invoke(delegate, ch);
    }
}
