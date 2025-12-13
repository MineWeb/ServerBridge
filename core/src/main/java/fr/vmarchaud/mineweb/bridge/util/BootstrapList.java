package fr.vmarchaud.mineweb.bridge.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BootstrapList extends ArrayList<Object> {

    private final ChannelHandler handler;
    private boolean closed;

    public BootstrapList(List<Object> original, ChannelHandler handler) {
        super(original.size());
        this.handler = handler;
        for (Object element : original) {
            inject(element);
            super.add(element);
        }
    }

    @Override
    public boolean add(Object element) {
        inject(element);
        return super.add(element);
    }

    @Override
    public void add(int index, Object element) {
        inject(element);
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<?> c) {
        for (Object element : c) {
            inject(element);
        }
        return super.addAll(c);
    }

    private void inject(Object obj) {
        if (closed) {
            return;
        }
        if (!(obj instanceof ChannelFuture)) {
            return;
        }
        ChannelFuture future = (ChannelFuture) obj;
        future.addListener((GenericFutureListener<ChannelFuture>) f -> {
            try {
                Channel ch = f.channel();
                if (ch.pipeline().get("mineweb_jsonapi") == null) {
                    ch.pipeline().addFirst("mineweb_jsonapi", handler);
                }
            } catch (Exception ignored) {
            }
        });
    }

    public void close() {
        closed = true;
        clear();
    }
}
