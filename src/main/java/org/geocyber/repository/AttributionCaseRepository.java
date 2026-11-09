package org.geocyber.repository;

import org.geocyber.domain.AttributionCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributionCaseRepository extends JpaRepository<AttributionCase, Long> {
    Optional<AttributionCase> findByCaseNumber(String caseNumber);
    List<AttributionCase> findByStatus(String status);
    List<AttributionCase> findByAttributedActorIgnoreCase(String actor);
    List<AttributionCase> findTop20ByOrderByCreatedAtDesc();
}
