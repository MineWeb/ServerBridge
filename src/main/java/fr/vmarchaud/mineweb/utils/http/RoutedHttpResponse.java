package fr.vmarchaud.mineweb.utils.http;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class RoutedHttpResponse {

	FullHttpRequest		request;
	FullHttpResponse	res;
	
}
