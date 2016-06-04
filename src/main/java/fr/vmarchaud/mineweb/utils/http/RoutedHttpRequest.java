package fr.vmarchaud.mineweb.utils.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class RoutedHttpRequest {
	
	ChannelHandlerContext	ctx;
	FullHttpRequest			request;
	
}
