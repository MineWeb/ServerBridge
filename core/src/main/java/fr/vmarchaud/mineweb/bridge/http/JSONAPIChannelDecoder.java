package fr.vmarchaud.mineweb.bridge.http;

import fr.vmarchaud.mineweb.bridge.api.ICore;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.List;
import java.util.NoSuchElementException;

public class JSONAPIChannelDecoder extends ByteToMessageDecoder {

    private final ICore api;

    public JSONAPIChannelDecoder(ICore api) {
        this.api = api;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if (buf.readableBytes() < 4) {
            return;
        }

        buf.retain(2);

        int magic1 = buf.getUnsignedByte(buf.readerIndex());
        int magic2 = buf.getUnsignedByte(buf.readerIndex() + 1);
        int magic3 = buf.getUnsignedByte(buf.readerIndex() + 2);
        int magic4 = buf.getUnsignedByte(buf.readerIndex() + 3);

        ChannelPipeline p = ctx.channel().pipeline();

        if (isHttp(magic1, magic2, magic3, magic4)) {
            ByteBuf copy = buf.copy();

            try {
                while (p.removeLast() != null) {
                }
            } catch (NoSuchElementException ignored) {
            }

            p.addLast("codec-http", new HttpServerCodec());
            p.addLast("aggregator", new HttpObjectAggregator(65536));
            p.addLast("handler", new HttpRequestHandler(api, api.getHttpRouter()));

            p.fireChannelRead(copy);
        } else {
            try {
                p.remove(this);
            } catch (NoSuchElementException ignored) {
            }
        }
        buf.release();
        buf.release();
    }

    private boolean isHttp(int magic1, int magic2, int magic3, int magic4) {
        return magic1 == 'G' && magic2 == 'E' && magic3 == 'T' && magic4 == ' '
                || magic1 == 'P' && magic2 == 'O' && magic3 == 'S' && magic4 == 'T'
                || magic1 == 'P' && magic2 == 'U' && magic3 == 'T' && magic4 == ' '
                || magic1 == 'H' && magic2 == 'E' && magic3 == 'A' && magic4 == 'D'
                || magic1 == 'O' && magic2 == 'P' && magic3 == 'T' && magic4 == 'I'
                || magic1 == 'P' && magic2 == 'A' && magic3 == 'T' && magic4 == 'C'
                || magic1 == 'D' && magic2 == 'E' && magic3 == 'L' && magic4 == 'E'
                || magic1 == 'T' && magic2 == 'R' && magic3 == 'A' && magic4 == 'C'
                || magic1 == 'C' && magic2 == 'O' && magic3 == 'N' && magic4 == 'N';
    }
}
