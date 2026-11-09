package org.geocyber.repository;

import org.geocyber.domain.ThreatReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreatReportRepository extends JpaRepository<ThreatReport, Long> {
    List<ThreatReport> findByPublisherIgnoreCase(String publisher);
    List<ThreatReport> findTop20ByOrderByPublicationDateDesc();
    List<ThreatReport> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String title, String summary);
}
