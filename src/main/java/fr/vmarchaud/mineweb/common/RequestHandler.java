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

import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.spec.SecretKeySpec;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import fr.vmarchaud.mineweb.common.interactor.MinewebInteractor;
import fr.vmarchaud.mineweb.common.interactor.requests.AskRequest;
import fr.vmarchaud.mineweb.common.interactor.requests.HandshakeRequest;
import fr.vmarchaud.mineweb.common.interactor.responses.AskResponse;
import fr.vmarchaud.mineweb.common.interactor.responses.HandshakeResponse;
import fr.vmarchaud.mineweb.common.interactor.responses.RetrieveKeyResponse;
import fr.vmarchaud.mineweb.utils.CryptoUtils;
import fr.vmarchaud.mineweb.utils.http.HttpResponseBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
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
			this.key = new SecretKeySpec(api.config().getSecretkey().getBytes(), "AES");
		
		// register handshake endpoint
		api.getHTTPRouter().post("/handshake", (request) -> this.handleHandshake(request.getRequest()));
		api.getHTTPRouter().post("/ask", (request) -> this.handle(request.getRequest()));
	}
	
	/**
	 * Refresh the secretkey used to cipher and decipher request
	 * @param secretKey: String containing the 32 bytes key
	 */
	public void	refreshKey(String secretKey) {
		this.key = new SecretKeySpec(secretKey.getBytes(), "AES");
	}
	
	/**
	 * Handle special request from CMS (handshake)
	 * Retrieve secret-key -> save it in configuration -> send it back as succesfull handshake.
	 * 
	 * @param request: HttpRequest that contains data required for handshake
	 * @return httpResponse: Response object to send back to the client
	 * @throws Exception 
	 */
	public FullHttpResponse handleHandshake(FullHttpRequest httpRequest) {
		ByteBuf buf = httpRequest.content();
		String content = buf.toString(buf.readerIndex(), buf.readableBytes(), Charset.forName("UTF-8"));
		HandshakeRequest handshake = api.gson().fromJson(content, HandshakeRequest.class);
		
		api.logger().info(String.format("New Handshake id: %s (%s, %s, %s)",
				handshake.getId(), handshake.getLicenseId(), handshake.getLicenseKey(), handshake.getDomain()));
		
		if (!handshake.isValid()) {
			api.logger().info(String.format("Handshake failed id: %s (reason: invalid params)", handshake.getId()));
			return new HttpResponseBuilder().code(HttpResponseStatus.BAD_REQUEST).build();
		}
		
		if (api.config().getSecretkey() != null) {
			api.logger().info(String.format("Handshake failed id: %s (reason: already linked)", handshake.getId()));
			return new HttpResponseBuilder().code(HttpResponseStatus.FORBIDDEN).build();
		}
		try {
			RetrieveKeyResponse keyResponse = MinewebInteractor.retrieveKey(api, handshake);
			HandshakeResponse response = new HandshakeResponse();
			
			// if the key isn't send by the api, the data sent by the cms are invalid
			if (keyResponse.getSecret_key() == null) {
				response.setMsg(keyResponse.getMsg());
				response.setStatus(response.isStatus());
				api.logger().info(String.format("Handshake failed id: %s (reason: %s)", handshake.getId(), keyResponse.getMsg()));
				return new HttpResponseBuilder().code(HttpResponseStatus.BAD_REQUEST).json(api.gson().toJson(response)).build();
			}
			
			String secret = CryptoUtils.decryptRSA(keyResponse.getSecret_key(), MinewebInteractor.MINEWEB_PUBLIC_KEY).substring(0, 16);
			if (secret == null) {
				api.logger().info(String.format("Handshake failed for request %s (reason: Cant decrypt secret)", handshake.getId()));
				response.setMsg("Cant decrypt secret from API");
				response.setStatus(false);
				return new HttpResponseBuilder().code(HttpResponseStatus.INTERNAL_SERVER_ERROR).json(api.gson().toJson(response)).build();
			}
			
			// save all the stuff inside the configuration
			api.config().setSecretkey(secret);
			api.config().setLicenseId(handshake.getLicenseId());
			api.config().setLicenseKey(handshake.getLicenseKey());
			api.config().setDomain(handshake.getDomain());
			api.config().save(api);
			response.setMsg("Successfully retrieved secret key, now ready !");
			response.setStatus(true);
			api.logger().info(String.format("Handshake request %s has been successfully valided (secret: %s)", handshake.getId(), secret));
			
			return new HttpResponseBuilder().code(HttpResponseStatus.OK).json(api.gson().toJson(response)).build();
		} catch (Exception e) {
			e.printStackTrace();
			return new HttpResponseBuilder().code(HttpResponseStatus.INTERNAL_SERVER_ERROR).json(api.gson().toJson(e.getMessage())).build();
		}
	}
	
	/**
	 * Handle general request from CMS
	 * 
	 * @param httpRequest: Request object that contains ciphered CMS's request
	 * @return httpResponse: Response object to send back to the client
	 */
	public FullHttpResponse handle(FullHttpRequest httpRequest) {
		if (api.config().getSecretkey() == null) {
			api.logger().severe("Secret key isnt defined, please setup like wrote in the mineweb documentation.");
			return new HttpResponseBuilder().code(HttpResponseStatus.NOT_IMPLEMENTED).build();
		}
		
		ByteBuf buf = httpRequest.content();
		String content = buf.toString(buf.readerIndex(), buf.readableBytes(), Charset.forName("UTF-8"));
		
		Map<String, Object[]> requests;
		AskRequest request;
		JsonObject response = new JsonObject();
		Type token = new TypeToken<Map<String, Object[]>>(){}.getType();
		
		try {
			// parse json to map
			request = api.gson().fromJson(content, AskRequest.class);
			String tmp = CryptoUtils.decryptAES(request.getSigned(), key, request.getIv());
			JsonReader reader = new JsonReader(new StringReader(tmp));
			reader.setLenient(true);
			requests = api.gson().fromJson(reader, token);
		} catch (Exception e) {
			api.logger().severe(String.format("Cant decipher/parse a request : %s", e.getMessage()));
			e.printStackTrace();
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
		
		api.logger().fine(String.format("request %s : %s", httpRequest.hashCode(), api.gson().toJson(requests)));
		api.logger().fine(String.format("response %s : %s", httpRequest.hashCode() , api.gson().toJson(response)));
		
		try {
			// try to cipher the data and send it
			AskResponse askResponse = new AskResponse();
			askResponse.setSigned(CryptoUtils.encryptAES(api.gson().toJson(response), key, request.getIv()));
			askResponse.setIv(request.getIv());
			return new HttpResponseBuilder().text(api.gson().toJson(askResponse)).code(HttpResponseStatus.OK).build();
		} catch (Exception e) {
			api.logger().severe(String.format("Cant cipher/serialize a response : %s", e.getMessage()));
			return HttpResponseBuilder.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
