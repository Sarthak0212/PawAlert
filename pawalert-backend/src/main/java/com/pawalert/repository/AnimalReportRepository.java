package com.pawalert.repository;

import com.pawalert.model.AnimalReport;
import com.pawalert.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnimalReportRepository extends JpaRepository<AnimalReport, Long> {
    List<AnimalReport> findByReporter(User reporter);
}
