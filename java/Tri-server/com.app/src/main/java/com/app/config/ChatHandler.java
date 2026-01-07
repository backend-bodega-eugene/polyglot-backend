package com.app.config;

import com.app.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatHandler extends TextWebSocketHandler {
    @Autowired
    private AiService aiService;

    public ChatHandler() {
        System.out.println("ChatHandler loaded");
    }

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String raw = session.getUri().getQuery().split("=")[1];
        String name = URLDecoder.decode(raw, StandardCharsets.UTF_8);

        if (!Set.of("老胡", "雪梅").contains(name)) {
            session.close();
            return;
        }

        sessions.add(session);
        System.out.println("Connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        for (WebSocketSession s : sessions) {
            s.sendMessage(message);
        }
        // AI 插嘴（现在先简单点：每条都回）
        String aiReply = aiService.ask(message.getPayload());

        TextMessage aiMsg = new TextMessage("AI: " + aiReply);

        for (WebSocketSession s : sessions) {
            s.sendMessage(aiMsg);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("Closed: " + session.getId());
    }
}
