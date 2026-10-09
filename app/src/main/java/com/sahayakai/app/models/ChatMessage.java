package com.sahayakai.app.models;

/**
 * ChatMessage model — represents a single message in a conversation.
 * Stored in Firebase under: chats/{UID}/{sessionId}/messages/{messageId}
 */
public class ChatMessage {

    public static final String SENDER_USER = "user";
    public static final String SENDER_AI = "assistant";

    private String messageId;
    private String sender;      // "user" or "assistant"
    private String message;
    private long timestamp;

    // Required no-argument constructor for Firebase
    public ChatMessage() {}

    public ChatMessage(String sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    // ---- Getters and Setters ----

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isUserMessage() {
        return SENDER_USER.equals(sender);
    }
}
