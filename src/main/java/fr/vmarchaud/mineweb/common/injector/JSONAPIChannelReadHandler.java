package fr.vmarchaud.mineweb.common.injector;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import fr.vmarchaud.mineweb.common.ICore;

@Sharable
public class JSONAPIChannelReadHandler extends ChannelInboundHandlerAdapter {
	List<Entry<String, ChannelHandler>>	handlers	= new ArrayList<Entry<String, ChannelHandler>>();
	NioEventLoopGroup					eventGroup;
	ICore								api;

	public JSONAPIChannelReadHandler(ICore api, final NioEventLoopGroup eventGroup) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		this.eventGroup = eventGroup;
		this.api = api;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		Channel child = (Channel) msg;

		child.pipeline().addFirst(new JSONAPIChannelDecoder(api));
		this.eventGroup.register(child);

		ctx.fireChannelRead(msg);
	}

	public class HTTPRequest extends ByteBufInputStream {

		public HTTPRequest(ByteBuf buf) {
			super(buf);
		}
	}
}
