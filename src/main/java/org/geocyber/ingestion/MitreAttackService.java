package org.geocyber.ingestion;

import org.geocyber.domain.MitreAttackTechnique;
import org.geocyber.repository.MitreTechniqueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MitreAttackService {

    private static final Logger log = LoggerFactory.getLogger(MitreAttackService.class);

    private final MitreTechniqueRepository techniqueRepository;

    // Ordered list of top enterprise techniques for vector representation (Multi-hot encoding)
    public static final List<String> CORE_TECHNIQUES = List.of(
            "T1566.001", "T1566.002", "T1190", "T1078", "T1059.001", "T1059.003", "T1059.005",
            "T1053.005", "T1547.001", "T1055", "T1027", "T1140", "T1070.004", "T1562.001",
            "T1003.001", "T1555", "T1082", "T1016", "T1049", "T1057", "T1021.001", "T1021.002",
            "T1071.001", "T1071.004", "T1573.001", "T1090", "T1041", "T1486", "T1490", "T1565.001"
    );

    public MitreAttackService(MitreTechniqueRepository techniqueRepository) {
        this.techniqueRepository = techniqueRepository;
    }

    public List<MitreAttackTechnique> getAllTechniques() {
        return techniqueRepository.findAll();
    }

    public Optional<MitreAttackTechnique> getByTechniqueId(String id) {
        return techniqueRepository.findByTechniqueId(id.trim());
    }

    public List<MitreAttackTechnique> getByTactic(String tactic) {
        return techniqueRepository.findByTacticIgnoreCase(tactic.trim());
    }

    /**
     * Converts a comma-separated string of observed TTP IDs into a normalized binary multi-hot feature vector.
     * Dimensions = CORE_TECHNIQUES.size()
     */
    public double[] encodeTtpVector(String observedTtps) {
        double[] vector = new double[CORE_TECHNIQUES.size()];
        if (observedTtps == null || observedTtps.isBlank()) {
            return vector;
        }

        String lower = observedTtps.toUpperCase();
        for (int i = 0; i < CORE_TECHNIQUES.size(); i++) {
            String techId = CORE_TECHNIQUES.get(i);
            if (lower.contains(techId)) {
                vector[i] = 1.0;
            }
        }
        return vector;
    }

    @Transactional
    public MitreAttackTechnique save(MitreAttackTechnique tech) {
        return techniqueRepository.save(tech);
    }
}
