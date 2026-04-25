package com.pawalert.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "animal_reports")
public class AnimalReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    private String animalName;
    private String location;
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDateTime reportDate = LocalDateTime.now();

    public AnimalReport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }
    public String getAnimalName() { return animalName; }
    public void setAnimalName(String animalName) { this.animalName = animalName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }

    public enum Status {
        PENDING, IN_PROGRESS, RESCUED
    }
}
