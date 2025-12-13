package fr.vmarchaud.mineweb.bridge.util.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

public class HttpResponseBuilder {

    private HttpResponseStatus status = HttpResponseStatus.OK;
    private byte[] body = new byte[0];
    private EnumContent contentType = EnumContent.JSON;

    public FullHttpResponse build() {
        ByteBuf buf = Unpooled.wrappedBuffer(body);
        FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
        HttpHeaders headers = res.headers();
        headers.set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        headers.set(HttpHeaderNames.CONTENT_TYPE, contentType.getMime());
        return res;
    }

    public HttpResponseBuilder code(HttpResponseStatus status) {
        this.status = status;
        return this;
    }

    public HttpResponseBuilder text(String text) {
        this.body = text != null ? text.getBytes(StandardCharsets.UTF_8) : new byte[0];
        this.contentType = EnumContent.PLAIN;
        return this;
    }

    public HttpResponseBuilder json(String json) {
        this.body = json != null ? json.getBytes(StandardCharsets.UTF_8) : new byte[0];
        this.contentType = EnumContent.JSON;
        return this;
    }

    public static FullHttpResponse ok() {
        FullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.EMPTY_BUFFER
        );
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return res;
    }

    public static FullHttpResponse status(HttpResponseStatus status) {
        FullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.EMPTY_BUFFER
        );
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return res;
    }

    @Getter
    public enum EnumContent {
        JSON("application/json"),
        PLAIN("text/plain"),
        HTML("text/html"),
        BINARY("application/octet-stream");

        private final String mime;

        EnumContent(String mime) {
            this.mime = mime;
        }
    }
}
