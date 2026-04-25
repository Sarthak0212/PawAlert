package com.pawalert.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rescue_details")
public class RescueDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "report_id", nullable = false)
    private AnimalReport report;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    private LocalDateTime rescueDate = LocalDateTime.now();
    private String statusUpdates;
    private String outcome;
    private String notes;

    public RescueDetail() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AnimalReport getReport() { return report; }
    public void setReport(AnimalReport report) { this.report = report; }
    public User getVolunteer() { return volunteer; }
    public void setVolunteer(User volunteer) { this.volunteer = volunteer; }
    public LocalDateTime getRescueDate() { return rescueDate; }
    public void setRescueDate(LocalDateTime rescueDate) { this.rescueDate = rescueDate; }
    public String getStatusUpdates() { return statusUpdates; }
    public void setStatusUpdates(String statusUpdates) { this.statusUpdates = statusUpdates; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
