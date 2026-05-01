package com.tutorlink.web.websocket;

import com.tutorlink.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if ("token".equals(kv[0]) && kv.length == 2) {
                    String token = kv[1];
                    try {
                        if (!jwtUtil.isTokenExpired(token)) {
                            Long userId = jwtUtil.getUserIdFromToken(token);
                            attributes.put("userId", userId);
                            attributes.put("principal", (Principal) () -> String.valueOf(userId));
                            return true;
                        }
                    } catch (Exception e) {
                        log.warn("WebSocket handshake token invalid");
                    }
                }
            }
        }
        log.warn("WebSocket handshake rejected: no valid token");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
    }
}
