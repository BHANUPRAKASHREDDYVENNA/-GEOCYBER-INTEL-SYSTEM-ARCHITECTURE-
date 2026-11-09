package org.geocyber.repository;

import org.geocyber.domain.GdeltEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GdeltEventRepository extends JpaRepository<GdeltEvent, Long> {
    Optional<GdeltEvent> findByGlobalEventId(String globalEventId);
    List<GdeltEvent> findByEventDateBetweenOrderByEventDateDesc(LocalDate start, LocalDate end);
    List<GdeltEvent> findByActionGeoCountryOrderByEventDateDesc(String country);

    @Query("SELECT g FROM GdeltEvent g WHERE (g.actor1Country = :country1 AND g.actor2Country = :country2) OR (g.actor1Country = :country2 AND g.actor2Country = :country1) ORDER BY g.eventDate DESC")
    List<GdeltEvent> findBilateralEvents(String country1, String country2);

    List<GdeltEvent> findTop20ByOrderByEventDateDesc();
}
