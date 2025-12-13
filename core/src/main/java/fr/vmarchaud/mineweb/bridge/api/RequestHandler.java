package fr.vmarchaud.mineweb.bridge.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.vmarchaud.mineweb.bridge.config.PluginConfiguration;
import fr.vmarchaud.mineweb.bridge.interactor.requests.AskRequest;
import fr.vmarchaud.mineweb.bridge.interactor.requests.HandshakeRequest;
import fr.vmarchaud.mineweb.bridge.interactor.responses.AskResponse;
import fr.vmarchaud.mineweb.bridge.interactor.responses.HandshakeResponse;
import fr.vmarchaud.mineweb.bridge.http.router.RoutedHttpRequest;
import fr.vmarchaud.mineweb.bridge.util.http.HttpResponseBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

public class RequestHandler {

    private final ICore core;
    private volatile String secretKey;

    public RequestHandler(ICore core) {
        this.core = core;
        PluginConfiguration cfg = core.getConfiguration();
        if (cfg != null) {
            this.secretKey = cfg.getSecretKey();
        }
    }

    public synchronized void resetKey() {
        this.secretKey = null;
    }

    public FullHttpResponse handleHandshake(RoutedHttpRequest event) {
        try {
            FullHttpRequest req = event.getRequest();
            String remote = String.valueOf(event.getCtx().channel().remoteAddress());
            ByteBuf content = req.content();
            String body = content.toString(CharsetUtil.UTF_8);
            String contentType = req.headers().get(HttpHeaderNames.CONTENT_TYPE);
            HandshakeRequest hr;
            if (contentType != null && contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
                String[] parts = body.split("&");
                String domain = null;
                String key = null;
                for (String part : parts) {
                    int idx = part.indexOf('=');
                    if (idx <= 0) {
                        continue;
                    }
                    String k = URLDecoder.decode(part.substring(0, idx), StandardCharsets.UTF_8.name());
                    String v = URLDecoder.decode(part.substring(idx + 1), StandardCharsets.UTF_8.name());
                    if ("domain".equals(k)) {
                        domain = v;
                    } else if ("secretKey".equals(k)) {
                        key = v;
                    }
                }
                hr = new HandshakeRequest();
                hr.setDomain(domain);
                hr.setSecretKey(key);
            } else {
                hr = core.getGson().fromJson(body, HandshakeRequest.class);
            }

            String domain = hr != null ? hr.getDomain() : null;
            core.logger().info("[HANDSHAKE] incoming from " + remote + " domain=" + domain);

            return doHandshake(hr, remote);
        } catch (Exception e) {
            core.logger().severe("Error in /handshake " + e.getMessage());
            HandshakeResponse resp = new HandshakeResponse();
            resp.setStatus(false);
            resp.setMsg("internal error");
            String json = core.getGson().toJson(resp);
            return new HttpResponseBuilder()
                    .code(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    .json(json)
                    .build();
        }
    }

    private FullHttpResponse doHandshake(HandshakeRequest req, String remote) {
        String domain = req != null ? req.getDomain() : null;

        if (req == null || !req.isValid()) {
            core.logger().warning("[HANDSHAKE] invalid params from " + remote + " domain=" + domain);
            HandshakeResponse resp = new HandshakeResponse();
            resp.setStatus(false);
            resp.setMsg("invalid params");
            String json = core.getGson().toJson(resp);
            return new HttpResponseBuilder()
                    .code(HttpResponseStatus.BAD_REQUEST)
                    .json(json)
                    .build();
        }

        PluginConfiguration cfg = core.getConfiguration();
        synchronized (this) {
            if (this.secretKey != null) {
                core.logger().warning("[HANDSHAKE] already linked, reject from " + remote + " domain=" + domain);
                HandshakeResponse resp = new HandshakeResponse();
                resp.setStatus(false);
                resp.setMsg("already linked");
                String json = core.getGson().toJson(resp);
                return new HttpResponseBuilder()
                        .code(HttpResponseStatus.FORBIDDEN)
                        .json(json)
                        .build();
            }
            String incomingKey = req.getSecretKey();
            this.secretKey = incomingKey;
            if (cfg != null) {
                cfg.setSecretKey(incomingKey);
                cfg.save(core.getGson(), core);
            }
        }

        core.logger().info("[HANDSHAKE] success from " + remote + " domain=" + domain);

        HandshakeResponse resp = new HandshakeResponse();
        resp.setStatus(true);
        resp.setMsg("ok");
        String json = core.getGson().toJson(resp);
        return new HttpResponseBuilder()
                .code(HttpResponseStatus.OK)
                .json(json)
                .build();
    }

    public FullHttpResponse handleAsk(RoutedHttpRequest event) {
        if (secretKey == null || secretKey.isEmpty()) {
            return new HttpResponseBuilder()
                    .code(HttpResponseStatus.FORBIDDEN)
                    .text("handshake required")
                    .build();
        }
        try {
            FullHttpRequest req = event.getRequest();
            ByteBuf content = req.content();
            String body = content.toString(CharsetUtil.UTF_8);
            AskRequest ask = core.getGson().fromJson(body, AskRequest.class);
            if (ask == null || ask.getSigned() == null || ask.getIv() == null) {
                return new HttpResponseBuilder()
                        .code(HttpResponseStatus.BAD_REQUEST)
                        .text("invalid request")
                        .build();
            }

            String decrypted;
            try {
                decrypted = decryptSigned(ask.getSigned(), ask.getIv());
            } catch (Exception e) {
                core.logger().warning("Failed to decrypt signed payload " + e.getMessage());
                return new HttpResponseBuilder()
                        .code(HttpResponseStatus.BAD_REQUEST)
                        .text("invalid payload")
                        .build();
            }

            JsonArray calls;
            try {
                JsonElement root = core.getGson().fromJson(decrypted, JsonElement.class);
                if (root != null && root.isJsonArray()) {
                    calls = root.getAsJsonArray();
                } else if (root != null && root.isJsonPrimitive() && root.getAsJsonPrimitive().isString()) {
                    String inner = root.getAsString();
                    calls = core.getGson().fromJson(inner, JsonArray.class);
                } else {
                    return new HttpResponseBuilder()
                            .code(HttpResponseStatus.BAD_REQUEST)
                            .text("invalid payload")
                            .build();
                }
            } catch (Exception parseEx) {
                core.logger().warning("Failed to parse decrypted payload " + parseEx.getMessage());
                return new HttpResponseBuilder()
                        .code(HttpResponseStatus.BAD_REQUEST)
                        .text("invalid payload")
                        .build();
            }

            JsonArray results = new JsonArray();

            for (JsonElement element : calls) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject call = element.getAsJsonObject();
                String name = call.has("name") ? call.get("name").getAsString() : null;
                JsonArray argsArray = call.has("args") && call.get("args").isJsonArray() ? call.getAsJsonArray("args") : new JsonArray();
                JsonObject resObj = new JsonObject();
                resObj.addProperty("name", name);
                if (name == null) {
                    resObj.addProperty("error", "missing name");
                    results.add(resObj);
                    continue;
                }
                Map<String, IMethod> methods = core.getMethods();
                IMethod method = methods.get(name);
                if (method == null) {
                    resObj.addProperty("error", "unknown method");
                    results.add(resObj);
                    continue;
                }
                Object[] args = new Object[argsArray.size()];
                for (int i = 0; i < argsArray.size(); i++) {
                    args[i] = argsArray.get(i);
                }
                try {
                    Object value = method.execute(core, args);
                    resObj.add("response", core.getGson().toJsonTree(value));
                } catch (Exception ex) {
                    resObj.addProperty("error", ex.getMessage());
                }
                results.add(resObj);
            }

            String resultsJson = core.getGson().toJson(results);
            CryptoResult enc = encryptSigned(resultsJson);

            AskResponse response = new AskResponse();
            response.setIv(enc.iv);
            response.setSigned(enc.signed);

            String json = core.getGson().toJson(response);
            return new HttpResponseBuilder().json(json).build();

        } catch (Exception e) {
            core.logger().severe("Error in /ask " + e.getMessage());
            return new HttpResponseBuilder()
                    .code(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    .text("internal error")
                    .build();
        }
    }

    private byte[] buildKeyBytes() {
        byte[] raw = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[16];
        int len = Math.min(raw.length, 16);
        System.arraycopy(raw, 0, keyBytes, 0, len);
        return keyBytes;
    }

    private String decryptSigned(String signed, String iv) throws Exception {
        byte[] keyBytes = buildKeyBytes();
        byte[] ivBytes = Base64.getDecoder().decode(iv);
        byte[] cipherBytes = Base64.getDecoder().decode(signed);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(cipherBytes);
        int pad = decrypted[decrypted.length - 1] & 0xFF;
        int len = decrypted.length;
        if (pad > 0 && pad <= 16 && pad <= decrypted.length) {
            len = decrypted.length - pad;
        }
        return new String(decrypted, 0, len, StandardCharsets.UTF_8);
    }

    private CryptoResult encryptSigned(String plaintext) throws Exception {
        byte[] keyBytes = buildKeyBytes();
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);
        byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        int blockSize = 16;
        int pad = blockSize - (plainBytes.length % blockSize);
        byte[] padded = new byte[plainBytes.length + pad];
        System.arraycopy(plainBytes, 0, padded, 0, plainBytes.length);
        for (int i = plainBytes.length; i < padded.length; i++) {
            padded[i] = (byte) pad;
        }
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] cipherBytes = cipher.doFinal(padded);
        CryptoResult result = new CryptoResult();
        result.signed = Base64.getEncoder().encodeToString(cipherBytes);
        result.iv = Base64.getEncoder().encodeToString(ivBytes);
        return result;
    }

    private static final class CryptoResult {
        String signed;
        String iv;
    }
}
