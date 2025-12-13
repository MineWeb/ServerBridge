package fr.vmarchaud.mineweb.bridge.http.router;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.Getter;

@Getter
public class RoutedHttpResponse {
    private final FullHttpRequest request;
    private final FullHttpResponse res;

    public RoutedHttpResponse(FullHttpRequest request, FullHttpResponse res) {
        this.request = request;
        this.res = res;
    }
}
