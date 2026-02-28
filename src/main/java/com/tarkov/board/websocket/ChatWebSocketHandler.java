package com.tarkov.board.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkov.board.whiteboard.WhiteboardChatMessageResponse;
import com.tarkov.board.whiteboard.WhiteboardChatService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String CHAT_PATH_PREFIX = "/ws/chat/";
    private static final String INSTANCE_ID_ATTR = "chatInstanceId";

    private final WhiteboardChatService chatService;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(WhiteboardChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String instanceId = resolveInstanceId(session.getUri());
        if (!StringUtils.hasText(instanceId)) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing chat instanceId"));
            return;
        }

        List<WhiteboardChatMessageResponse> history;
        try {
            history = chatService.listRecentMessages(instanceId);
        } catch (ResponseStatusException ex) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Whiteboard instance not found"));
            return;
        }

        roomSessions.computeIfAbsent(instanceId, key -> ConcurrentHashMap.newKeySet()).add(session);
        session.getAttributes().put(INSTANCE_ID_ATTR, instanceId);
        sendToSession(session, new ChatHistoryEnvelope("chat.history", instanceId, history));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String instanceId = (String) session.getAttributes().get(INSTANCE_ID_ATTR);
        if (!StringUtils.hasText(instanceId)) {
            return;
        }

        IncomingChatMessage incoming = parseIncoming(message.getPayload());
        if (!StringUtils.hasText(incoming.content())) {
            return;
        }

        try {
            WhiteboardChatMessageResponse saved = chatService.appendMessage(
                    instanceId,
                    incoming.senderName(),
                    incoming.content()
            );
            broadcastToRoom(instanceId, new ChatMessageEnvelope("chat.message", instanceId, saved));
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Whiteboard instance expired"));
                return;
            }
            sendToSession(session, new ChatErrorEnvelope("chat.error", ex.getReason()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String instanceId = (String) session.getAttributes().get(INSTANCE_ID_ATTR);
        if (!StringUtils.hasText(instanceId)) {
            return;
        }

        Set<WebSocketSession> sessions = roomSessions.get(instanceId);
        if (sessions == null) {
            return;
        }

        sessions.remove(session);
        if (sessions.isEmpty()) {
            roomSessions.remove(instanceId);
        }
    }

    private IncomingChatMessage parseIncoming(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (root.isObject()) {
                String content = root.path("content").asText(null);
                String senderName = root.path("senderName").asText(null);
                return new IncomingChatMessage(senderName, content);
            }
        } catch (JsonProcessingException ignored) {
        }
        return new IncomingChatMessage(null, payload);
    }

    private void broadcastToRoom(String instanceId, Object payloadObject) {
        Set<WebSocketSession> sessions = roomSessions.get(instanceId);
        if (sessions == null) {
            return;
        }

        for (WebSocketSession targetSession : sessions) {
            if (!targetSession.isOpen()) {
                continue;
            }
            sendToSession(targetSession, payloadObject);
        }
    }

    private void sendToSession(WebSocketSession session, Object payloadObject) {
        if (!session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payloadObject)));
        } catch (IOException ignored) {
        }
    }

    private String resolveInstanceId(URI uri) {
        if (uri == null) {
            return null;
        }

        String instanceIdFromPath = resolveInstanceIdFromPath(uri.getPath());
        if (StringUtils.hasText(instanceIdFromPath)) {
            return instanceIdFromPath;
        }

        return resolveInstanceIdFromQuery(uri.getQuery());
    }

    private String resolveInstanceIdFromPath(String path) {
        if (!StringUtils.hasText(path) || !path.startsWith(CHAT_PATH_PREFIX)) {
            return null;
        }
        String raw = path.substring(CHAT_PATH_PREFIX.length());
        String instanceId = raw.contains("/") ? raw.substring(0, raw.indexOf('/')) : raw;
        return StringUtils.hasText(instanceId) ? instanceId : null;
    }

    private String resolveInstanceIdFromQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return null;
        }
        for (String part : query.split("&")) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2 && "instanceId".equals(keyValue[0])) {
                return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private record IncomingChatMessage(String senderName, String content) {
    }

    private record ChatHistoryEnvelope(String type,
                                       String instanceId,
                                       List<WhiteboardChatMessageResponse> messages) {
    }

    private record ChatMessageEnvelope(String type,
                                       String instanceId,
                                       WhiteboardChatMessageResponse message) {
    }

    private record ChatErrorEnvelope(String type, String message) {
    }
}
