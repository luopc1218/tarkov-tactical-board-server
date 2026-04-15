package com.tarkov.board.config;

import com.tarkov.board.websocket.ChatWebSocketHandler;
import com.tarkov.board.websocket.WhiteboardWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String[] ALLOWED_ORIGIN_PATTERNS = {
            "*"
    };

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WhiteboardWebSocketHandler whiteboardWebSocketHandler;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                           WhiteboardWebSocketHandler whiteboardWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.whiteboardWebSocketHandler = whiteboardWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat", "/ws/chat/**")
                .setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS);
        registry.addHandler(whiteboardWebSocketHandler, "/ws/whiteboard/**")
                .setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS);
    }
}
