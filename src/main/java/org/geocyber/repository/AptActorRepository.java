package org.geocyber.repository;

import org.geocyber.domain.AptActor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AptActorRepository extends JpaRepository<AptActor, Long> {
    Optional<AptActor> findByNameIgnoreCase(String name);
    List<AptActor> findByCountry(String country);
    List<AptActor> findByActiveTrue();

    @Query("SELECT a FROM AptActor a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.aliases) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<AptActor> searchByNameOrAlias(String query);
}
