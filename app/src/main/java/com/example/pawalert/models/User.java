package com.example.pawalert.models;

/**
 * Represents a registered user of PawAlert.
 * This model maps to a Firestore document in the "users" collection.
 */
public class User {

    private String userId;
    private String name;
    private String email;
    private String phone;
    private String role;        // "REPORTER", "VOLUNTEER", "ADMIN"
    private String fcmToken;    // Firebase Cloud Messaging token for push notifications
    private long createdAt;

    // Required empty constructor for Firestore deserialization
    public User() {}

    public User(String userId, String name, String email,
                String phone, String role, String fcmToken, long createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.fcmToken = fcmToken;
        this.createdAt = createdAt;
    }

    // Getters
    public String getUserId()   { return userId; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getPhone()    { return phone; }
    public String getRole()     { return role; }
    public String getFcmToken() { return fcmToken; }
    public long getCreatedAt()  { return createdAt; }

    // Setters
    public void setUserId(String userId)     { this.userId = userId; }
    public void setName(String name)         { this.name = name; }
    public void setEmail(String email)       { this.email = email; }
    public void setPhone(String phone)       { this.phone = phone; }
    public void setRole(String role)         { this.role = role; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
