package org.geocyber.repository;

import org.geocyber.domain.MitreAttackTechnique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MitreTechniqueRepository extends JpaRepository<MitreAttackTechnique, Long> {
    Optional<MitreAttackTechnique> findByTechniqueId(String techniqueId);
    List<MitreAttackTechnique> findByTacticIgnoreCase(String tactic);
    List<MitreAttackTechnique> findByNameContainingIgnoreCase(String name);
}
