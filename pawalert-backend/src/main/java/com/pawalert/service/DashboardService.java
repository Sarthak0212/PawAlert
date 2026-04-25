package com.pawalert.service;

import com.pawalert.dto.ReporterDashboardDTO;
import com.pawalert.dto.VolunteerDashboardDTO;
import com.pawalert.dto.UserProfileDTO;
import com.pawalert.model.AnimalReport;
import com.pawalert.model.RescueDetail;
import com.pawalert.model.User;
import com.pawalert.repository.AnimalReportRepository;
import com.pawalert.repository.RescueDetailRepository;
import com.pawalert.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnimalReportRepository reportRepository;

    @Autowired
    private RescueDetailRepository rescueRepository;

    public ReporterDashboardDTO getReporterDashboard(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<AnimalReport> reports = reportRepository.findByReporter(user);

        ReporterDashboardDTO dto = new ReporterDashboardDTO();
        dto.setProfile(user);
        dto.setReports(reports.stream().map(report -> {
            ReporterDashboardDTO.ReportWithRescueInfo item = new ReporterDashboardDTO.ReportWithRescueInfo();
            item.setReport(report);
            
            rescueRepository.findByReport(report).ifPresent(rescue -> {
                ReporterDashboardDTO.RescueInfo rescueInfo = new ReporterDashboardDTO.RescueInfo();
                rescueInfo.setVolunteerName(rescue.getVolunteer().getName());
                rescueInfo.setRescueDate(rescue.getRescueDate().toString());
                rescueInfo.setStatusUpdates(rescue.getStatusUpdates());
                rescueInfo.setOutcome(rescue.getOutcome());
                rescueInfo.setNotes(rescue.getNotes());
                item.setRescueDetails(rescueInfo);
            });
            
            return item;
        }).collect(Collectors.toList()));

        return dto;
    }

    public VolunteerDashboardDTO getVolunteerDashboard(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<RescueDetail> tasks = rescueRepository.findByVolunteer(user);

        VolunteerDashboardDTO dto = new VolunteerDashboardDTO();
        dto.setProfile(user);
        
        VolunteerDashboardDTO.Stats stats = new VolunteerDashboardDTO.Stats();
        stats.setTotalRescues(rescueRepository.countByVolunteer(user));
        stats.setActiveRescues(rescueRepository.countByVolunteerAndReportStatus(user, AnimalReport.Status.IN_PROGRESS));
        stats.setCompletedRescues(rescueRepository.countByVolunteerAndReportStatus(user, AnimalReport.Status.RESCUED));
        dto.setStats(stats);
        
        dto.setRescueTasks(tasks);

        return dto;
    }

    public UserProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        UserProfileDTO dto = new UserProfileDTO();
        dto.setProfile(user);

        UserProfileDTO.Stats stats = new UserProfileDTO.Stats();
        if (user.getRole() == User.Role.REPORTER) {
            List<AnimalReport> reports = reportRepository.findByReporter(user);
            dto.setReports(reports);
            stats.setTotal(reports.size());
            stats.setActive(reports.stream().filter(r -> r.getStatus() != AnimalReport.Status.RESCUED).count());
            stats.setCompleted(reports.stream().filter(r -> r.getStatus() == AnimalReport.Status.RESCUED).count());
        } else {
            List<RescueDetail> rescues = rescueRepository.findByVolunteer(user);
            dto.setRescues(rescues);
            stats.setTotal(rescueRepository.countByVolunteer(user));
            stats.setActive(rescueRepository.countByVolunteerAndReportStatus(user, AnimalReport.Status.IN_PROGRESS));
            stats.setCompleted(rescueRepository.countByVolunteerAndReportStatus(user, AnimalReport.Status.RESCUED));
        }
        dto.setStats(stats);
        return dto;
    }
}
