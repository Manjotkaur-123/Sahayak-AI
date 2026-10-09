package com.sahayakai.app.models;

import java.util.List;

/**
 * ChatSession model — represents an entire conversation (multiple messages).
 * Stored in Firebase under: chats/{UID}/{sessionId}
 */
public class ChatSession {

    private String sessionId;
    private String title;        // Auto-generated from first user message
    private long createdAt;
    private List<ChatMessage> messages;

    // Required no-argument constructor for Firebase
    public ChatSession() {}

    public ChatSession(String sessionId, String title, long createdAt) {
        this.sessionId = sessionId;
        this.title = title;
        this.createdAt = createdAt;
    }

    // ---- Getters and Setters ----

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
}
