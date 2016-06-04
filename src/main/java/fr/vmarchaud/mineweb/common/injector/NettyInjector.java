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
    // The temporary player factory
	protected List<VolatileField> bootstrapFields = Lists.newArrayList();
   
    // List of network managers
	protected volatile List<Object> networkManagers;
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
            
            if (list.size() == 0 || list.get(0) instanceof ChannelFuture) {
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
