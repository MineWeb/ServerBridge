package fr.vmarchaud.mineweb.bridge.http.router;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Getter;

@Getter
public class RoutedHttpRequest {
    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;

    public RoutedHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        this.ctx = ctx;
        this.request = request;
    }
}
