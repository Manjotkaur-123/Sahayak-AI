package com.sahayakai.app.models;

/**
 * NotificationModel — represents a notification for the user.
 * Stored in Firebase under: notifications/{UID}/{notifId}
 */
public class NotificationModel {

    public static final String TYPE_NEW_SCHEME = "new_scheme";
    public static final String TYPE_DEADLINE = "deadline_reminder";
    public static final String TYPE_UPDATE = "scheme_update";
    public static final String TYPE_DOCUMENT = "missing_document";
    public static final String TYPE_APPLICATION = "application_update";

    private String notifId;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private long timestamp;
    private String schemeId;   // Optional: links to a specific scheme

    // Required no-argument constructor for Firebase
    public NotificationModel() {}

    public NotificationModel(String title, String message, String type, long timestamp) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    // ---- Getters and Setters ----

    public String getNotifId() { return notifId; }
    public void setNotifId(String notifId) { this.notifId = notifId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getSchemeId() { return schemeId; }
    public void setSchemeId(String schemeId) { this.schemeId = schemeId; }
}
