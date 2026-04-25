package com.example.pawalert.models;

/**
 * Report — POJO representing a single animal distress report stored in Firestore.
 */
public class Report {

    private String reportId;
    private String imageBase64;
    private double lat;
    private double lng;
    private String animalType;
    private String priority;
    private String status;
    private String createdBy;
    private String volunteerId;
    private String resolvedProofImage;
    private String rescueDescription; // New field for volunteer actions
    private String rejectionReason;   // Reason for resolution rejection
    private long   timestamp;          // Epoch milliseconds

    // Status Constants
    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_APPROVED         = "APPROVED";
    public static final String STATUS_IN_PROGRESS       = "IN_PROGRESS";
    public static final String STATUS_RESOLVED_PENDING = "RESOLVED_PENDING_APPROVAL";
    public static final String STATUS_RESOLVED         = "RESOLVED";
    public static final String STATUS_REJECTED         = "REJECTED";
    public static final String STATUS_OPEN             = "OPEN";
    public static final String STATUS_ACCEPTED         = "ACCEPTED";
    public static final String STATUS_COMPLETED        = "COMPLETED";

    // Required no-arg constructor for Firestore deserialization
    public Report() {}

    public Report(String reportId, String imageBase64, double lat, double lng,
                  String animalType, String priority, String status, String createdBy) {
        this.reportId    = reportId;
        this.imageBase64 = imageBase64;
        this.lat         = lat;
        this.lng         = lng;
        this.animalType  = animalType;
        this.priority    = priority;
        this.status      = status;
        this.createdBy   = createdBy;
        this.timestamp   = System.currentTimeMillis();
    }

    // Comprehensive constructor
    public Report(String reportId, String imageBase64, double lat, double lng,
                  String animalType, String priority, String status, String createdBy,
                  String volunteerId, String resolvedProofImage, String rescueDescription, 
                  String rejectionReason, long timestamp) {
        this.reportId = reportId;
        this.imageBase64 = imageBase64;
        this.lat = lat;
        this.lng = lng;
        this.animalType = animalType;
        this.priority = priority;
        this.status = status;
        this.createdBy = createdBy;
        this.volunteerId = volunteerId;
        this.resolvedProofImage = resolvedProofImage;
        this.rescueDescription = rescueDescription;
        this.rejectionReason = rejectionReason;
        this.timestamp = timestamp;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getReportId()    { return reportId; }
    public String getImageBase64() { return imageBase64; }
    public double getLat()         { return lat; }
    public double getLng()         { return lng; }
    public String getAnimalType()  { return animalType; }
    public String getPriority()    { return priority; }
    public String getStatus()      { return status; }
    public String getCreatedBy()   { return createdBy; }
    public String getVolunteerId() { return volunteerId; }
    public String getResolvedProofImage() { return resolvedProofImage; }
    public String getRescueDescription()  { return rescueDescription; }
    public String getRejectionReason()    { return rejectionReason; }
    public long   getTimestamp()          { return timestamp; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setReportId(String reportId)       { this.reportId = reportId; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public void setLat(double lat)                 { this.lat = lat; }
    public void setLng(double lng)                 { this.lng = lng; }
    public void setAnimalType(String animalType)   { this.animalType = animalType; }
    public void setPriority(String priority)       { this.priority = priority; }
    public void setStatus(String status)           { this.status = status; }
    public void setCreatedBy(String createdBy)     { this.createdBy = createdBy; }
    public void setVolunteerId(String volunteerId) { this.volunteerId = volunteerId; }
    public void setResolvedProofImage(String resolvedProofImage) { this.resolvedProofImage = resolvedProofImage; }
    public void setRescueDescription(String rescueDescription)   { this.rescueDescription = rescueDescription; }
    public void setRejectionReason(String rejectionReason)       { this.rejectionReason = rejectionReason; }
    public void setTimestamp(long timestamp)       { this.timestamp = timestamp; }
}
