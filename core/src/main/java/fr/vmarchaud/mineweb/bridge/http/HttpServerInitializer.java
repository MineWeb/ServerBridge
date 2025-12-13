package fr.vmarchaud.mineweb.bridge.http;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ICore core;

    public HttpServerInitializer(ICore core) {
        this.core = core;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast("codec", new HttpServerCodec());
        p.addLast("aggregator", new HttpObjectAggregator(65536));
        p.addLast("handler", new HttpRequestHandler(core, core.getHttpRouter()));
    }
}
