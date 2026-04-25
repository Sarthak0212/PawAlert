package com.pawalert.dto;

import com.pawalert.model.User;
import com.pawalert.model.AnimalReport;
import com.pawalert.model.RescueDetail;
import java.util.List;

public class UserProfileDTO {
    private User profile;
    private List<AnimalReport> reports; // For Reporter
    private List<RescueDetail> rescues;   // For Volunteer
    private Stats stats;

    public UserProfileDTO() {}

    public User getProfile() { return profile; }
    public void setProfile(User profile) { this.profile = profile; }
    public List<AnimalReport> getReports() { return reports; }
    public void setReports(List<AnimalReport> reports) { this.reports = reports; }
    public List<RescueDetail> getRescues() { return rescues; }
    public void setRescues(List<RescueDetail> rescues) { this.rescues = rescues; }
    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }

    public static class Stats {
        private long total;
        private long active;
        private long completed;

        public Stats() {}

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getActive() { return active; }
        public void setActive(long active) { this.active = active; }
        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }
    }
}
