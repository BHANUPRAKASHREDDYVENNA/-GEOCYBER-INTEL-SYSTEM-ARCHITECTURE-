package org.geocyber.repository;

import org.geocyber.domain.AnomalyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnomalyAlertRepository extends JpaRepository<AnomalyAlert, Long> {
    List<AnomalyAlert> findByOrderByTimestampDesc();
    List<AnomalyAlert> findBySeverity(String severity);
    List<AnomalyAlert> findByStatus(String status);
}
