package com.pawalert.dto;

import com.pawalert.model.User;
import com.pawalert.model.AnimalReport;
import java.util.List;

public class ReporterDashboardDTO {
    private User profile;
    private List<ReportWithRescueInfo> reports;

    public ReporterDashboardDTO() {}

    public User getProfile() { return profile; }
    public void setProfile(User profile) { this.profile = profile; }
    public List<ReportWithRescueInfo> getReports() { return reports; }
    public void setReports(List<ReportWithRescueInfo> reports) { this.reports = reports; }

    public static class ReportWithRescueInfo {
        private AnimalReport report;
        private RescueInfo rescueDetails;

        public ReportWithRescueInfo() {}

        public AnimalReport getReport() { return report; }
        public void setReport(AnimalReport report) { this.report = report; }
        public RescueInfo getRescueDetails() { return rescueDetails; }
        public void setRescueDetails(RescueInfo rescueDetails) { this.rescueDetails = rescueDetails; }
    }

    public static class RescueInfo {
        private String volunteerName;
        private String rescueDate;
        private String statusUpdates;
        private String outcome;
        private String notes;

        public RescueInfo() {}

        public String getVolunteerName() { return volunteerName; }
        public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }
        public String getRescueDate() { return rescueDate; }
        public void setRescueDate(String rescueDate) { this.rescueDate = rescueDate; }
        public String getStatusUpdates() { return statusUpdates; }
        public void setStatusUpdates(String statusUpdates) { this.statusUpdates = statusUpdates; }
        public String getOutcome() { return outcome; }
        public void setOutcome(String outcome) { this.outcome = outcome; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
