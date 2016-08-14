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

import javax.crypto.spec.SecretKeySpec;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import fr.vmarchaud.mineweb.utils.CryptoUtils;
import fr.vmarchaud.mineweb.utils.http.HttpResponseBuilder;
import fr.vmarchaud.mineweb.utils.http.RoutedHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class RequestHandler {
	
	private ICore			api;
	private SecretKeySpec	key;
	
	/**
	 * Construct an instance of the RequestHandler that will validate and respond to inbound request
	 * @param api: instance of the API
	 */
	public RequestHandler(ICore api) {
		this.api = api;
		if (api.config().getSecretkey() != null)
			key = new SecretKeySpec(api.config().getSecretkey().getBytes(), "AES");
	}
	
	/**
	 * Refresh the secretkey used to cipher and decipher request
	 * @param secretKey: String containing the 32 bytes key
	 */
	public void	refreshKey(String secretKey) {
		key = new SecretKeySpec(secretKey.getBytes(), "AES");
	}
	
	public FullHttpResponse handle(RoutedHttpRequest httpRequest) {
		if (api.config().getSecretkey() == null) {
			api.logger().severe("Secret key isnt defined, please setup like wrote in the mineweb documentation.");
			return new HttpResponseBuilder().code(HttpResponseStatus.NOT_IMPLEMENTED).build();
		}

		JsonObject 	response = new JsonObject();
		
		Type token = new TypeToken<Map<String, Object[]>>(){}.getType();
		Map<String, Object[]> 	requests = null;
		try {
			// decipher request with the key
			String content = CryptoUtils.decrypt(httpRequest.getRequest().content().array(), key);
			// parse json to map of all request
			requests = api.gson().fromJson(content, token);
		} catch (Exception e) {
			api.logger().severe(String.format("Cant decipher/parse a request : %s", e.getMessage()));
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
		
		api.logger().fine(String.format("request %s : %s", httpRequest.hashCode(), requests));
		api.logger().fine(String.format("response %s : %s", httpRequest.hashCode() , response));
		
		try {
			// try to cipher the data and send it
			byte[] contentResp = CryptoUtils.encrypt(api.gson().toJson(response), key);
			return new HttpResponseBuilder().raw(contentResp).code(HttpResponseStatus.OK).build();
		} catch (Exception e) {
			api.logger().severe(String.format("Cant cipher/serialize a request : %s", e.getMessage()));
			return HttpResponseBuilder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
