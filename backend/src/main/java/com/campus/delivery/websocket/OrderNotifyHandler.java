package com.campus.delivery.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单与抢单消息推送处理器（需求文档 5.6.6 难点六）。
 *
 * <p>连接地址：{@code ws://localhost:8080/api/ws/order?token=<jwt>&role=<ROLE>&userId=<id>}
 *
 * <p>推送场景：
 * <ul>
 *   <li>学生：自己订单的状态变更</li>
 *   <li>商户：本店新订单提醒</li>
 *   <li>骑手：订单大厅新单广播</li>
 * </ul>
 *
 * <p>可靠性：关键状态变更采用「WebSocket 推送 + 前端轮询」双通道，
 * 断线后前端调用订单列表接口即可恢复状态。
 */
@Slf4j
@Component
public class OrderNotifyHandler extends TextWebSocketHandler {

    /** userId -> 该用户的全部连接（同一账号可能多端登录） */
    private static final Map<String, Set<WebSocketSession>> USER_SESSIONS = new ConcurrentHashMap<>();

    /** role -> 该角色的全部连接（用于骑手大厅广播） */
    private static final Map<String, Set<WebSocketSession>> ROLE_SESSIONS = new ConcurrentHashMap<>();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = attr(session, "userId");
        String role = attr(session, "role");
        if (userId == null || role == null) {
            closeQuietly(session);
            return;
        }
        USER_SESSIONS.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        ROLE_SESSIONS.computeIfAbsent(role, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("WebSocket 连接建立: userId={}, role={}, 在线连接数={}", userId, role, USER_SESSIONS.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
        log.info("WebSocket 连接关闭: sessionId={}, status={}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端心跳：回复 pong，用于前端探活与后端在线状态维护
        if ("ping".equals(message.getPayload())) {
            sendText(session, "{\"type\":\"PONG\"}");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket 传输异常: sessionId={}, msg={}", session.getId(), exception.getMessage());
        removeSession(session);
    }

    // ------------------------------------------------------------------
    // 对外推送方法：供 order / delivery 模块调用
    // ------------------------------------------------------------------

    /** 推送给指定用户（学生、商户） */
    public void sendToUser(String userId, String type, Object data) {
        Set<WebSocketSession> sessions = USER_SESSIONS.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String payload = buildPayload(type, data);
        sessions.forEach(session -> sendText(session, payload));
    }

    /** 广播给某角色的全部在线连接（骑手订单大厅） */
    public void broadcastToRole(String role, String type, Object data) {
        Set<WebSocketSession> sessions = ROLE_SESSIONS.get(role);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String payload = buildPayload(type, data);
        sessions.forEach(session -> sendText(session, payload));
    }

    /** 当前在线连接数（用于数据看板） */
    public int onlineCount(String role) {
        Set<WebSocketSession> sessions = ROLE_SESSIONS.get(role);
        return sessions == null ? 0 : sessions.size();
    }

    // ------------------------------------------------------------------

    private String buildPayload(String type, Object data) {
        try {
            return MAPPER.writeValueAsString(Map.of("type", type, "data", data));
        } catch (Exception e) {
            log.error("WebSocket 消息序列化失败", e);
            return "{\"type\":\"ERROR\",\"data\":null}";
        }
    }

    private void sendText(WebSocketSession session, String text) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            log.warn("WebSocket 推送失败: sessionId={}, msg={}", session.getId(), e.getMessage());
            removeSession(session);
        }
    }

    private void removeSession(WebSocketSession session) {
        String userId = attr(session, "userId");
        String role = attr(session, "role");
        if (userId != null) {
            Set<WebSocketSession> sessions = USER_SESSIONS.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    USER_SESSIONS.remove(userId);
                }
            }
        }
        if (role != null) {
            Set<WebSocketSession> sessions = ROLE_SESSIONS.get(role);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    ROLE_SESSIONS.remove(role);
                }
            }
        }
    }

    private String attr(WebSocketSession session, String key) {
        Object value = session.getAttributes().get(key);
        return value == null ? null : String.valueOf(value);
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close(CloseStatus.NOT_ACCEPTABLE);
        } catch (IOException ignored) {
            // 关闭失败无需处理
        }
    }
}
