package fr.vmarchaud.mineweb.bridge.http;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import fr.vmarchaud.mineweb.bridge.http.router.RouteMatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final ICore core;
    private final RouteMatcher router;

    public HttpRequestHandler(ICore core, RouteMatcher router) {
        this.core = core;
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        router.serveRequest(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        core.logger().warning("HTTP error " + cause.getMessage());
        ctx.close();
    }
}
