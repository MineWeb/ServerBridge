package fr.vmarchaud.mineweb.bridge.http.router;

import fr.vmarchaud.mineweb.bridge.util.http.HttpResponseBuilder;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class RouteMatcher {

    private final Map<String, Handler<FullHttpResponse, RoutedHttpRequest>> getRoutes = new LinkedHashMap<>();
    private final Map<String, Handler<FullHttpResponse, RoutedHttpRequest>> postRoutes = new LinkedHashMap<>();
    private Handler<Void, RoutedHttpResponse> everyMatch;

    public RouteMatcher get(String path, Handler<FullHttpResponse, RoutedHttpRequest> handler) {
        getRoutes.put(path, handler);
        return this;
    }

    public void post(String path, Handler<FullHttpResponse, RoutedHttpRequest> handler) {
        postRoutes.put(path, handler);
    }

    public void everyMatch(Handler<Void, RoutedHttpResponse> handler) {
        this.everyMatch = handler;
    }

    public void serveRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        FullHttpResponse res = handleRequest(ctx, req);
        send(ctx, req, res);
    }

    public FullHttpResponse handleRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        if (!req.getDecoderResult().isSuccess()) {
            return HttpResponseBuilder.status(HttpResponseStatus.BAD_REQUEST);
        }

        String uri = req.uri();
        String path;
        int q = uri.indexOf("?");
        if (q >= 0) {
            path = uri.substring(0, q);
        } else {
            path = uri;
        }

        HttpMethod method = req.method();
        Handler<FullHttpResponse, RoutedHttpRequest> handler;

        if (HttpMethod.GET.equals(method)) {
            handler = getRoutes.get(path);
        } else if (HttpMethod.POST.equals(method)) {
            handler = postRoutes.get(path);
        } else {
            handler = null;
        }

        FullHttpResponse res;

        if (handler == null) {
            res = HttpResponseBuilder.status(HttpResponseStatus.NOT_FOUND);
        } else {
            RoutedHttpRequest routed = new RoutedHttpRequest(ctx, req);
            res = handler.handle(routed);
            if (res == null) {
                res = HttpResponseBuilder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (everyMatch != null) {
            everyMatch.handle(new RoutedHttpResponse(req, res));
        }

        return res;
    }

    public void send(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        ChannelFuture f = ctx.writeAndFlush(res);
        boolean keepAlive = HttpUtil.isKeepAlive(req) && res.status().code() == 200;
        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
