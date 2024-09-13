/**
 * Author : rasintha_j
 * Date : 9/8/2024
 * Time : 11:13 AM
 * Project Name : remindifyapp
 */

package com.remindifyapp.service;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        sessions.put(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, IOException {
        String userId = (String) session.getAttributes().get("userId");
        String payload = message.getPayload();

        // Broadcast the message to all users in the chat group
        for (WebSocketSession s : sessions.values()) {
            s.sendMessage(new TextMessage(userId + ": " + payload));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        sessions.remove(userId);
    }
}
