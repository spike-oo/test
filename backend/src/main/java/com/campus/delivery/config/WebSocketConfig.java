package com.campus.delivery.config;

import com.campus.delivery.websocket.OrderNotifyHandler;
import com.campus.delivery.security.JwtUtils;
import com.campus.delivery.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 配置：注册订单推送端点并完成握手期鉴权。
 *
 * <p>端点：{@code /ws/order?token=<jwt>}
 * 握手时解析 JWT，把 userId / role 写入会话属性，供 {@link OrderNotifyHandler} 定向推送。
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrderNotifyHandler orderNotifyHandler;
    private final JwtUtils jwtUtils;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderNotifyHandler, "/ws/order")
                .addInterceptors(new TokenHandshakeInterceptor(jwtUtils))
                .setAllowedOriginPatterns("*");
    }

    /** 握手拦截器：从 query 参数解析 Token，未通过则不建立连接 */
    static class TokenHandshakeInterceptor implements HandshakeInterceptor {

        private final JwtUtils jwtUtils;

        TokenHandshakeInterceptor(JwtUtils jwtUtils) {
            this.jwtUtils = jwtUtils;
        }

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            List<String> tokens = UriComponentsBuilder.fromUri(request.getURI())
                    .build().getQueryParams().get("token");
            if (tokens == null || tokens.isEmpty()) {
                log.warn("WebSocket 握手失败：缺少 token");
                return false;
            }
            LoginUser loginUser = jwtUtils.parseToken(tokens.get(0));
            if (loginUser == null) {
                log.warn("WebSocket 握手失败：token 无效");
                return false;
            }
            attributes.put("userId", loginUser.getUserId());
            attributes.put("role", loginUser.getRole());
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
            // 无需处理
        }
    }
}
