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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Lists;

import fr.vmarchaud.mineweb.bukkit.dump.VolatileField;
import fr.vmarchaud.mineweb.common.ICore;
import fr.vmarchaud.mineweb.common.injector.JSONAPIChannelDecoder;
import fr.vmarchaud.mineweb.common.injector.NettyInjector;
import fr.vmarchaud.mineweb.utils.BootstrapList;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;

public class BukkitNettyInjector extends NettyInjector {

    // The temporary player factory
	protected List<VolatileField> bootstrapFields = Lists.newArrayList();
   
    // List of network managers
	protected volatile Collection<Object> networkManagers;
	
	private ICore			api;
	
	public BukkitNettyInjector(ICore api) {
		this.api = api;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void inject() {
        if (injected)
            throw new IllegalStateException("Cannot inject twice.");
        try {
            FuzzyReflection fuzzyServer = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass());
            Method serverConnectionMethod = fuzzyServer.getMethodByReturnTypeAndParameters("getServerConnection", MinecraftReflection.getServerConnectionClass());
            
            // Get the server connection
            Object server = fuzzyServer.getSingleton();
            Object serverConnection = serverConnectionMethod.invoke(server);
            
            // Handle connected channels
            final ChannelInboundHandler endInitProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    try {
                        // This can take a while, so we need to stop the main thread from interfering
                        synchronized (networkManagers) {
                            injectChannel(channel);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            
            // This is executed before Minecraft's channel handler
            final ChannelInboundHandler beginInitProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    // Our only job is to add init protocol
                    channel.pipeline().addLast(endInitProtocol);
                }
            };
            
            // Add our handler to newly created channels
            final ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    Channel channel = (Channel) msg;

                    // Prepare to initialize ths channel
                    channel.pipeline().addFirst(beginInitProtocol);
                    ctx.fireChannelRead(msg);
                }
            };
            
            // Get the current NetworkMananger list
            
            this.networkManagers = getNetworkManagers(serverConnection);;
            
            // Insert ProtocolLib's connection interceptor
            this.bootstrapFields = getBootstrapFields(serverConnection);
            
            for (VolatileField field : bootstrapFields) {
                final List<Object> list = (List<Object>) field.getValue();
     
                // We don't have to override this list
                if (list == this.networkManagers) {
                    continue;
                }
                
                // Synchronize with each list before we attempt to replace them.
                field.setValue(new BootstrapList(list, connectionHandler));
            }

            injected = true;
            
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject channel futures.", e);
        }
    }
	
	@SuppressWarnings("unchecked")
	private List<Object> getNetworkManagers(Object serverConnection) throws IllegalAccessException {
        Field networkManagersField = getFirstFieldWithListOfNetworkManagers(serverConnection);
        List<Object> networkManagers =  new ArrayList<Object>();

        if ( networkManagersField != null ){
            networkManagers = (List<Object>) networkManagersField.get(serverConnection);
        }
        return networkManagers;
    }


    private Field getFirstFieldWithListOfNetworkManagers(Object serverConnection) {
        Field networkManagersField = null;
        Class<?> networkManagerClass;
        try {
            networkManagerClass = Class.forName(MinecraftReflection.getNetworkManagerClass().getCanonicalName());
            for( Field declaredField: serverConnection.getClass().getDeclaredFields() ){
                boolean fieldIsAList = declaredField.getType() == List.class;
                if( fieldIsAList ){
                    Type typeOfFirstListElement = ((ParameterizedType) declaredField.getGenericType()).getActualTypeArguments()[0];
                    if( typeOfFirstListElement == networkManagerClass ) {
                        networkManagersField = declaredField;
                        networkManagersField.setAccessible(true);
                        break;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return networkManagersField;

    }

	@Override
	protected void injectChannel(Channel channel) {
		channel.pipeline().addFirst(new JSONAPIChannelDecoder(api));
	}
	
	/**
     * Retrieve a list of every field with a list of channel futures.
     * @param serverConnection - the connection.
     * @return List of fields.
     */
    protected List<VolatileField> getBootstrapFields(Object serverConnection) {
        List<VolatileField> result = Lists.newArrayList();
        
        // Find and (possibly) proxy every list
        for (Field field : FuzzyReflection.fromObject(serverConnection, true).getFieldListByType(List.class)) {
            VolatileField volatileField = new VolatileField(field, serverConnection, true).toSynchronized();
            
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) volatileField.getValue();
            
            if (list.isEmpty() || list.get(0) instanceof ChannelFuture) {
                result.add(volatileField);
            }
        }
        return result;
    }
    
    /**
     * Clean up any remaning injections.
     */
    public synchronized void close() {
        if (!closed) {
            closed = true;

            for (VolatileField field : bootstrapFields) {
                Object value = field.getValue();

                // Undo the processed channels, if any 
                if (value instanceof BootstrapList) {
                    ((BootstrapList) value).close();
                }
                field.revertValue();
            }
        }
    }

}
