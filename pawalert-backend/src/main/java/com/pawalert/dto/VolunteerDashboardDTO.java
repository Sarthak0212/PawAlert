package com.pawalert.dto;

import com.pawalert.model.User;
import com.pawalert.model.RescueDetail;
import java.util.List;

public class VolunteerDashboardDTO {
    private User profile;
    private Stats stats;
    private List<RescueDetail> rescueTasks;

    public VolunteerDashboardDTO() {}

    public User getProfile() { return profile; }
    public void setProfile(User profile) { this.profile = profile; }
    public Stats getStats() { return stats; }
    public void setStats(Stats stats) { this.stats = stats; }
    public List<RescueDetail> getRescueTasks() { return rescueTasks; }
    public void setRescueTasks(List<RescueDetail> rescueTasks) { this.rescueTasks = rescueTasks; }

    public static class Stats {
        private long totalRescues;
        private long activeRescues;
        private long completedRescues;

        public Stats() {}

        public long getTotalRescues() { return totalRescues; }
        public void setTotalRescues(long totalRescues) { this.totalRescues = totalRescues; }
        public long getActiveRescues() { return activeRescues; }
        public void setActiveRescues(long activeRescues) { this.activeRescues = activeRescues; }
        public long getCompletedRescues() { return completedRescues; }
        public void setCompletedRescues(long completedRescues) { this.completedRescues = completedRescues; }
    }
}
