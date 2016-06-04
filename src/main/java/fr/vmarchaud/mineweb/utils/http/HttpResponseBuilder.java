/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2016 Valentin 'ThisIsMac' Marchaud
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package fr.vmarchaud.mineweb.utils.http;

import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.Getter;

public class HttpResponseBuilder {
	
	
	private HttpResponseStatus		status 			= HttpResponseStatus.OK;
	private	String					body			= "";
	private EnumContent				contentType		= EnumContent.JSON;
	
	/**
	 * Build a empty response with code 200
	 * 
	 * @return FullHttpResponse
	 */
	public FullHttpResponse build() {
		ByteBuf buf = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8);
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
		response.headers().set("Content-Length", buf.readableBytes());
		response.headers().set("Content-Type", contentType.getMime());
		return response;
	}
	
	/**
	 * Set the http code that the response will show
	 * 
	 * @param HttpResponseStatus the http code of the response
	 * @return Builder instance
	 */
	public HttpResponseBuilder code(HttpResponseStatus status) {
		this.status = status;
		return this;
	}
	
	/**
	 * Set the text that the response will output
	 * 
	 * @param text The response text
	 * 
	 * @return Builder instance
	 */
	public HttpResponseBuilder text(String text) {
		this.body = text;
		this.contentType = EnumContent.PLAIN;
		return this;
	}
	
	/**
	 * Set the text that the response will output (from json object)
	 * 
	 * @param Object any object that gson can map
	 * 
	 * @return Builder instance
	 */
	public HttpResponseBuilder json(Object json) {
		this.body = new Gson().toJson(json);
		this.contentType = EnumContent.JSON;
		return this;
	}
	
	/**
	 * Build a empty response with code 200
	 * 
	 * @return FullHttpResponse
	 */
	public static FullHttpResponse ok() {
		ByteBuf buf = Unpooled.EMPTY_BUFFER;
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set("Content-Length", buf.readableBytes());
		return response;
	}
	
	/**
	 * Build a empty response with the code specified
	 * 
	 * @param HttpResponseStatus http code returned
	 * 
	 * @return FullHttpResponse
	 */
	public static FullHttpResponse status(HttpResponseStatus status) {
		ByteBuf buf = Unpooled.EMPTY_BUFFER;
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
		response.headers().set("Content-Length", buf.readableBytes());
		return response;
	}
	
	public enum EnumContent	{
		JSON("application/json"),
		PLAIN("text/plain"),
		HTML("text/html");
		
		@Getter String mime;
		
		EnumContent(String mime) {
			this.mime = mime;
		}
	}
}
