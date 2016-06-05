package fr.vmarchaud.mineweb.common.injector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.lang.reflect.Field;
import java.util.List;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.google.common.collect.Lists;

import fr.vmarchaud.mineweb.utils.BootstrapList;

public abstract class NettyInjector {   
    protected boolean injected;

    protected boolean closed;

    /**
     * Inject into the connection class.
     */
    public abstract void inject();
    
    
    /**
     * Invoked when a channel is ready to be injected.
     * @param channel - the channel to inject.
     */
    protected abstract void injectChannel(Channel channel);
    
    
}
