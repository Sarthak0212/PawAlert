package com.pawalert.repository;

import com.pawalert.model.RescueDetail;
import com.pawalert.model.User;
import com.pawalert.model.AnimalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RescueDetailRepository extends JpaRepository<RescueDetail, Long> {
    List<RescueDetail> findByVolunteer(User volunteer);
    Optional<RescueDetail> findByReport(AnimalReport report);
    long countByVolunteer(User volunteer);
    long countByVolunteerAndReportStatus(User volunteer, AnimalReport.Status status);
}
