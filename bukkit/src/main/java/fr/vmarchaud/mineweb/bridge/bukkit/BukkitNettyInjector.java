package fr.vmarchaud.mineweb.bridge.bukkit;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.api.NettyInjector;
import fr.vmarchaud.mineweb.bridge.http.JSONAPIChannelDecoder;
import fr.vmarchaud.mineweb.bridge.util.BootstrapList;
import fr.vmarchaud.mineweb.bridge.util.VolatileField;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BukkitNettyInjector implements NettyInjector {

    private final ICore api;
    private final List<VolatileField> bootstrapFields = new ArrayList<>();
    private volatile Collection<Object> networkManagers;
    private boolean injected;
    private boolean closed;

    public BukkitNettyInjector(ICore api) {
        this.api = api;
    }

    @Override
    public synchronized void inject() {
        if (injected) {
            throw new IllegalStateException("Cannot inject twice");
        }
        try {
            FuzzyReflection fuzzyServer = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass());
            Method serverConnectionMethod = fuzzyServer.getMethodByReturnTypeAndParameters("getServerConnection", MinecraftReflection.getServerConnectionClass());
            Object server = fuzzyServer.getSingleton();
            Object serverConnection = serverConnectionMethod.invoke(server);

            final ChannelInboundHandler endInitProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) {
                    try {
                        synchronized (networkManagers) {
                            injectChannel(channel);
                        }
                    } catch (Exception e) {
                        api.logger().severe("Unable to inject channel " + channel);
                        e.printStackTrace();
                    }
                }
            };

            final ChannelInboundHandler beginInitProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) {
                    channel.pipeline().addLast(endInitProtocol);
                }
            };

            final ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof Channel) {
                        Channel channel = (Channel) msg;
                        channel.pipeline().addFirst(beginInitProtocol);
                    }
                    super.channelRead(ctx, msg);
                }
            };

            this.networkManagers = getNetworkManagers(serverConnection);
            this.bootstrapFields.addAll(getBootstrapFields(serverConnection, connectionHandler));
            injected = true;
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject channel futures", e);
        }
    }

    private void injectChannel(Channel channel) {
        channel.pipeline().addFirst(new JSONAPIChannelDecoder(api));
    }

    @SuppressWarnings("unchecked")
    private List<Object> getNetworkManagers(Object serverConnection) throws IllegalAccessException {
        Field networkManagersField = getFirstFieldWithListOfNetworkManagers(serverConnection);
        List<Object> result = new ArrayList<>();
        if (networkManagersField != null) {
            result = (List<Object>) networkManagersField.get(serverConnection);
        }
        return result;
    }

    private Field getFirstFieldWithListOfNetworkManagers(Object serverConnection) {
        Field networkManagersField = null;
        try {
            Class<?> networkManagerClass = Class.forName(MinecraftReflection.getNetworkManagerClass().getCanonicalName());
            for (Field declaredField : serverConnection.getClass().getDeclaredFields()) {
                boolean fieldIsAList = declaredField.getType() == List.class;
                if (!fieldIsAList) {
                    continue;
                }
                Type typeOfFirstListElement = ((ParameterizedType) declaredField.getGenericType()).getActualTypeArguments()[0];
                if (typeOfFirstListElement == networkManagerClass) {
                    networkManagersField = declaredField;
                    networkManagersField.setAccessible(true);
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return networkManagersField;
    }

    @SuppressWarnings("unchecked")
    private List<VolatileField> getBootstrapFields(Object serverConnection, ChannelHandler connectionHandler) {
        List<VolatileField> result = new ArrayList<>();
        for (Field field : FuzzyReflection.fromObject(serverConnection, true).getFieldListByType(List.class)) {
            VolatileField volatileField = new VolatileField(field, serverConnection, true).toSynchronized();
            List<Object> list = (List<Object>) volatileField.getValue();
            if (list.isEmpty() || list.get(0) instanceof ChannelFuture) {
                if (list == this.networkManagers) {
                    continue;
                }
                volatileField.setValue(new BootstrapList(list, connectionHandler));
                result.add(volatileField);
            }
        }
        return result;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (VolatileField field : bootstrapFields) {
            Object value = field.getValue();
            if (value instanceof BootstrapList) {
                ((BootstrapList) value).close();
            }
            field.revertValue();
        }
        bootstrapFields.clear();
    }
}
