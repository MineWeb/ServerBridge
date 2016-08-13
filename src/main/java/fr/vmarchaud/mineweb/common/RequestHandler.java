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
package fr.vmarchaud.mineweb.common;


import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import fr.vmarchaud.mineweb.utils.ByteUtils;
import fr.vmarchaud.mineweb.utils.http.HttpResponseBuilder;
import fr.vmarchaud.mineweb.utils.http.RoutedHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class RequestHandler {
	
	private ICore		api;
	
	public RequestHandler(ICore api) {
		this.api = api;
	}
	
	public FullHttpResponse handle(RoutedHttpRequest httpRequest) {
		JsonObject 	response = new JsonObject();
		
		Type token = new TypeToken<Map<String, Object[]>>(){}.getType();
		Map<String, Object[]> 	requests = null;
		try {
			requests = api.gson().fromJson(ByteUtils.bytebufToString(httpRequest.getRequest().content()), token);
		} catch (Exception e) {
			return HttpResponseBuilder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
		
		for(Entry<String, Object[]> entry : requests.entrySet()) {
			IMethod 	method = api.getMethods().get(entry.getKey());
			Object[] 	inputs = entry.getValue();
			
			// if we didnt found the method just continue
			if (method == null) {
				response.addProperty(entry.getKey(), "NOT_FOUND");
				continue ;
			}
			
			// verify if the params size are same as requested by the method handler
			MethodHandler annot = method.getClass().getDeclaredAnnotation(MethodHandler.class);
			if (annot == null) {
				response.addProperty(entry.getKey(), "INVALID_METHOD");
				continue ;
			}
			
			if (annot.inputs() != inputs.length) {
				response.addProperty(entry.getKey(), "BAD_REQUEST");
				continue ;
			}
			
			// verify class type of input
			if (annot.inputs() > 0) {
				boolean valid = true;
				for(int i = 0; i < annot.types().length; i++) {
					if (!inputs[i].getClass().getName().equals(annot.types()[i].getName())) {
						valid = false;
						break ;
					}
				}
				if (!valid) {
					response.addProperty(entry.getKey(), "BAD_REQUEST");
					continue; 
				}
			}
			// execute the method and put the result into the response
			Object output = method.execute(api, inputs);
			response.add(entry.getKey(), api.gson().toJsonTree(output));
		}
		
		return new HttpResponseBuilder().json(api.gson().toJson(response)).code(HttpResponseStatus.OK).build();
	}
}
