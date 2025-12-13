package fr.vmarchaud.mineweb.bridge.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.NettyInjector;
import fr.vmarchaud.mineweb.bridge.http.JSONAPIChannelDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class VelocityNettyInjector implements NettyInjector {

    private final ICore core;
    private final ProxyServer proxy;
    private ChannelInitializer<Channel> original;
    private boolean injected;

    public VelocityNettyInjector(ICore core, ProxyServer proxy) {
        this.core = core;
        this.proxy = proxy;
    }

    @Override
    public synchronized void inject() throws Exception {
        if (injected) {
            throw new IllegalStateException("Cannot inject twice");
        }

        Object cm = getConnectionManager(proxy);
        Object holder = getServerChannelInitializerHolder(cm);

        Supplier<ChannelInitializer<Channel>> supplier = asChannelInitializerSupplier(holder);
        original = supplier.get();

        ChannelInitializer<Channel> wrapped = new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addFirst(new JSONAPIChannelDecoder(core));

                try {
                    Method m = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
                    m.setAccessible(true);
                    m.invoke(original, ch);
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to invoke original ChannelInitializer#initChannel", ex);
                }
            }
        };

        setServerChannelInitializer(holder, wrapped);
        injected = true;
    }

    @Override
    public void close() {
        if (!injected) {
            return;
        }
        try {
            Object cm = getConnectionManager(proxy);
            Object holder = getServerChannelInitializerHolder(cm);
            setServerChannelInitializer(holder, original);
        } catch (Exception ignored) {
        }
    }

    private static Object getConnectionManager(ProxyServer proxy) throws Exception {
        Field cmField = proxy.getClass().getDeclaredField("cm");
        cmField.setAccessible(true);
        return cmField.get(proxy);
    }

    private static Object getServerChannelInitializerHolder(Object connectionManager) throws Exception {
        Field holderField = connectionManager.getClass().getField("serverChannelInitializer");
        return holderField.get(connectionManager);
    }

    @SuppressWarnings("unchecked")
    private static Supplier<ChannelInitializer<Channel>> asChannelInitializerSupplier(Object holder) {
        return (Supplier<ChannelInitializer<Channel>>) holder;
    }

    private static void setServerChannelInitializer(Object holder, ChannelInitializer<Channel> initializer) throws Exception {
        Method set = holder.getClass().getMethod("set", ChannelInitializer.class);
        set.invoke(holder, initializer);
    }
}
