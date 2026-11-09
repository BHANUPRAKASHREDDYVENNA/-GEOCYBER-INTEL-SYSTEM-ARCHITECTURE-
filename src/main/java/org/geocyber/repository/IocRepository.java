package org.geocyber.repository;

import org.geocyber.domain.IndicatorOfCompromise;
import org.geocyber.domain.IocType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IocRepository extends JpaRepository<IndicatorOfCompromise, Long> {
    Optional<IndicatorOfCompromise> findByNormalizedValue(String normalizedValue);
    List<IndicatorOfCompromise> findByIocType(IocType iocType);
    List<IndicatorOfCompromise> findByAssociatedActorsContainingIgnoreCase(String actor);
    List<IndicatorOfCompromise> findTop50ByOrderByCreatedAtDesc();
}
