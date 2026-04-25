package com.example.pawalert.models;

/**
 * Notification — POJO representing a notification sent to a user.
 * Collection: "notifications"
 */
public class Notification {

    private String notificationId;
    private String userId;      // Recipient UID
    private String title;
    private String message;
    private String type;        // e.g., "RESOLUTION_APPROVED", "RESOLUTION_REJECTED"
    private String incidentId;  // Related Report ID
    private long   timestamp;   // System.currentTimeMillis()
    private boolean isRead;     // Default false

    // Constants for Types
    public static final String TYPE_RES_APPROVED = "RESOLUTION_APPROVED";
    public static final String TYPE_RES_REJECTED = "RESOLUTION_REJECTED";
    public static final String TYPE_NEW_REPORT   = "NEW_REPORT_ALert";

    public Notification() {
        this.isRead = false;
        this.timestamp = System.currentTimeMillis();
    }

    public Notification(String userId, String title, String message, String type, String incidentId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.incidentId = incidentId;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getNotificationId() { return notificationId; }
    public String getUserId()         { return userId; }
    public String getTitle()          { return title; }
    public String getMessage()        { return message; }
    public String getType()           { return type; }
    public String getIncidentId()     { return incidentId; }
    public long   getTimestamp()      { return timestamp; }
    public boolean isRead()           { return isRead; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public void setUserId(String userId)                 { this.userId = userId; }
    public void setTitle(String title)                   { this.title = title; }
    public void setMessage(String message)               { this.message = message; }
    public void setType(String type)                     { this.type = type; }
    public void setIncidentId(String incidentId)         { this.incidentId = incidentId; }
    public void setTimestamp(long timestamp)             { this.timestamp = timestamp; }
    public void setRead(boolean read)                    { isRead = read; }
}
