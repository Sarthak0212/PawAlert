package com.example.pawalert.models;

/**
 * Represents a stray animal alert/report submitted by a user.
 * This model maps directly to a Firestore document in the "alerts" collection.
 */
public class Alert {

    private String alertId;
    private String userId;
    private String animalType;      // e.g., "Dog", "Cat", "Cow"
    private String description;
    private String status;          // "OPEN", "IN_PROGRESS", "RESOLVED"
    private double latitude;
    private double longitude;
    private String imageUrl;
    private long timestamp;

    // Required empty constructor for Firestore deserialization
    public Alert() {}

    public Alert(String alertId, String userId, String animalType,
                 String description, String status,
                 double latitude, double longitude,
                 String imageUrl, long timestamp) {
        this.alertId = alertId;
        this.userId = userId;
        this.animalType = animalType;
        this.description = description;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    // Getters
    public String getAlertId()      { return alertId; }
    public String getUserId()       { return userId; }
    public String getAnimalType()   { return animalType; }
    public String getDescription()  { return description; }
    public String getStatus()       { return status; }
    public double getLatitude()     { return latitude; }
    public double getLongitude()    { return longitude; }
    public String getImageUrl()     { return imageUrl; }
    public long getTimestamp()      { return timestamp; }

    // Setters
    public void setAlertId(String alertId)          { this.alertId = alertId; }
    public void setUserId(String userId)            { this.userId = userId; }
    public void setAnimalType(String animalType)    { this.animalType = animalType; }
    public void setDescription(String description)  { this.description = description; }
    public void setStatus(String status)            { this.status = status; }
    public void setLatitude(double latitude)        { this.latitude = latitude; }
    public void setLongitude(double longitude)      { this.longitude = longitude; }
    public void setImageUrl(String imageUrl)        { this.imageUrl = imageUrl; }
    public void setTimestamp(long timestamp)        { this.timestamp = timestamp; }
}
