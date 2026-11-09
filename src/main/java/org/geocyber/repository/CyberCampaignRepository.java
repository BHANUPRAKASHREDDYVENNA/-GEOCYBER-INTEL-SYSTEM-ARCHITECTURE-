package org.geocyber.repository;

import org.geocyber.domain.CyberCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CyberCampaignRepository extends JpaRepository<CyberCampaign, Long> {
    Optional<CyberCampaign> findByNameIgnoreCase(String name);
    List<CyberCampaign> findByAttributedActorNameIgnoreCase(String actor);
    List<CyberCampaign> findTop20ByOrderByStartDateDesc();
}
