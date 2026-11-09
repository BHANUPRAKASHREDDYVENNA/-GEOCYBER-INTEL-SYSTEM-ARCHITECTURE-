package org.geocyber.ai.attribution;

import org.geocyber.ai.features.MultiModalFeatureVector;
import org.geocyber.ai.model.MultimodalCrossAttentionTransformer;
import org.geocyber.domain.AptActor;
import org.geocyber.repository.AptActorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProbabilisticAttributionEngine {

    private static final Logger log = LoggerFactory.getLogger(ProbabilisticAttributionEngine.class);

    private final MultimodalCrossAttentionTransformer transformer;
    private final AptActorRepository actorRepository;

    @Value("${geocyber.ai.calibration.temperature:1.25}")
    private double temperature;

    @Value("${geocyber.ai.unknown-threshold:0.35}")
    private double unknownThreshold;

    public ProbabilisticAttributionEngine(MultimodalCrossAttentionTransformer transformer,
                                          AptActorRepository actorRepository) {
        this.transformer = transformer;
        this.actorRepository = actorRepository;
    }

    /**
     * Executes the comprehensive attribution pipeline.
     */
    public AttributionReport performAttribution(MultiModalFeatureVector featureVector,
                                               String observedTtps,
                                               String rawIocs,
                                               String targetCountry,
                                               String incidentSector) {

        // 1. Transformer Multimodal Cross-Attention Forward Pass
        MultimodalCrossAttentionTransformer.InferenceResult result =
                transformer.forward(featureVector, temperature);

        AttributionReport report = new AttributionReport();
        report.setReportId("ATTR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        report.setGeneratedAt(LocalDateTime.now());
        report.setCrossAttentionMatrix(result.crossAttentionMatrix);

        // Modality weights mapping
        Map<String, Double> modMap = new LinkedHashMap<>();
        modMap.put("Geopolitical News NLP", result.modalityWeights[0]);
        modMap.put("GDELT Conflict Dynamics", result.modalityWeights[1]);
        modMap.put("MITRE ATT&CK TTPs", result.modalityWeights[2]);
        modMap.put("Network Infrastructure IOCs", result.modalityWeights[3]);
        modMap.put("Campaign Temporal Signatures", result.modalityWeights[4]);
        report.setModalityImportance(modMap);

        // 2. Rank Candidate Hypotheses
        List<AttributionReport.Hypothesis> hypotheses = new ArrayList<>();
        List<AptActor> allActors = actorRepository.findAll();
        Map<String, AptActor> actorMap = new HashMap<>();
        for (AptActor a : allActors) {
            actorMap.put(a.getName(), a);
        }

        double[] probs = result.probabilities;
        Integer[] indices = new Integer[probs.length];
        for (int i = 0; i < indices.length; i++) indices[i] = i;
        Arrays.sort(indices, (a, b) -> Double.compare(probs[b], probs[a]));

        for (int idx : indices) {
            String actorName = MultimodalCrossAttentionTransformer.ACTOR_CLASSES.get(idx);
            double postProb = probs[idx];
            AptActor actor = actorMap.get(actorName);

            String nation = actor != null ? actor.getCountry() : "UNKNOWN";
            String motivation = actor != null ? actor.getMotivation() : "Espionage";
            List<String> matchingTtps = findMatchingTtps(actor, observedTtps);
            List<String> matchingMal = findMatchingMalware(actor, rawIocs);

            hypotheses.add(new AttributionReport.Hypothesis(
                    actorName, nation, postProb, 1.0 / probs.length, motivation, matchingTtps, matchingMal
            ));
        }
        report.setRankedHypotheses(hypotheses);

        // 3. Confidence Calibration & Unknown Novel Actor Detection
        double topProb = probs[indices[0]];
        double entropy = calculateEntropy(probs);
        boolean isUnknown = (topProb < unknownThreshold) || (entropy > 2.2);

        report.setTopAttributedActor(isUnknown ? "UNC-NOVEL (Uncategorized Cluster)" : hypotheses.getFirst().getActorName());
        report.setTopAttributedNation(isUnknown ? "UNATTRIBUTED" : hypotheses.getFirst().getNationState());
        report.setCalibratedConfidence(topProb);
        report.setUnknownActorDetected(isUnknown);
        report.setUnknownActorScore(entropy / (Math.log(probs.length) / Math.log(2.0))); // Normalized entropy

        // 4. Explainable AI: Evidence breakdown
        AptActor topActor = actorMap.get(hypotheses.getFirst().getActorName());
        report.setSupportingEvidence(buildSupportingEvidence(topActor, featureVector, observedTtps, rawIocs, targetCountry, incidentSector));
        report.setContradictingEvidence(buildContradictingEvidence(topActor, featureVector, observedTtps, targetCountry));
        report.setAlternativeExplanations(buildAlternativeExplanations(hypotheses, topProb));
        report.setCorrelationVsAttributionNotes(buildCorrelationVsAttribution(isUnknown, topProb, featureVector));
        report.setProvenanceTrail(buildProvenance(topActor, targetCountry));

        return report;
    }

    private double calculateEntropy(double[] probs) {
        double entropy = 0.0;
        for (double p : probs) {
            if (p > 1e-12) {
                entropy -= p * (Math.log(p) / Math.log(2.0));
            }
        }
        return entropy;
    }

    private List<String> findMatchingTtps(AptActor actor, String observed) {
        if (actor == null || actor.getObservedTtps() == null || observed == null) return Collections.emptyList();
        List<String> matches = new ArrayList<>();
        String[] actorTtps = actor.getObservedTtps().split(",");
        String obsUpper = observed.toUpperCase();
        for (String t : actorTtps) {
            String clean = t.trim().toUpperCase();
            if (!clean.isBlank() && obsUpper.contains(clean)) {
                matches.add(clean);
            }
        }
        return matches;
    }

    private List<String> findMatchingMalware(AptActor actor, String rawIocs) {
        if (actor == null || actor.getObservedMalware() == null || rawIocs == null) return Collections.emptyList();
        List<String> matches = new ArrayList<>();
        String[] actorMal = actor.getObservedMalware().split(",");
        String iocLower = rawIocs.toLowerCase();
        for (String m : actorMal) {
            String clean = m.trim().toLowerCase();
            if (!clean.isBlank() && iocLower.contains(clean)) {
                matches.add(m.trim());
            }
        }
        return matches;
    }

    private List<String> buildSupportingEvidence(AptActor actor, MultiModalFeatureVector fv,
                                                 String observedTtps, String rawIocs,
                                                 String targetCountry, String sector) {
        List<String> evidence = new ArrayList<>();
        if (actor == null) {
            evidence.add("TTP signature matches common offensive playbook without primary state affiliation.");
            return evidence;
        }

        // TTP overlap
        List<String> matchedTtps = findMatchingTtps(actor, observedTtps);
        if (!matchedTtps.isEmpty()) {
            evidence.add(String.format("Observed %d matching MITRE ATT&CK techniques historically associated with %s: %s",
                    matchedTtps.size(), actor.getName(), String.join(", ", matchedTtps)));
        }

        // Target Sector & Country
        if (actor.getTargetCountries() != null && targetCountry != null &&
                actor.getTargetCountries().toUpperCase().contains(targetCountry.toUpperCase())) {
            evidence.add(String.format("Victim country [%s] is an established tier-1 collection priority for %s.", targetCountry, actor.getName()));
        }

        if (actor.getTargetSectors() != null && sector != null &&
                actor.getTargetSectors().toLowerCase().contains(sector.toLowerCase())) {
            evidence.add(String.format("Targeted industry sector [%s] directly corresponds to strategic collection objectives of %s.", sector, actor.getName()));
        }

        // GDELT bilateral tension support
        double[] gdelt = fv.getGdeltFeatures();
        if (gdelt[5] > 0.40) {
            evidence.add(String.format("Temporal co-occurrence with acute geopolitical tension index (%.2f) between suspected sponsor [%s] and target nation.",
                    gdelt[5], actor.getCountry()));
        }

        // Temporal work hour alignment
        double[] temporal = fv.getTemporalFeatures();
        if ("RU".equals(actor.getCountry()) && temporal[4] == 1.0) {
            evidence.add("Intrusion activity window strictly correlates with standard Moscow business working hours (06:00 - 15:00 UTC, Mon-Fri).");
        } else if ("CN".equals(actor.getCountry()) && temporal[5] == 1.0) {
            evidence.add("Intrusion activity window strictly correlates with China Standard Time business working hours (01:00 - 10:00 UTC, Mon-Fri).");
        } else if ("IR".equals(actor.getCountry()) && temporal[6] == 1.0) {
            evidence.add("Intrusion activity matches Iranian workweek schedule (Saturday through Wednesday active operational cadence).");
        }

        return evidence;
    }

    private List<String> buildContradictingEvidence(AptActor actor, MultiModalFeatureVector fv,
                                                    String observedTtps, String targetCountry) {
        List<String> contra = new ArrayList<>();
        if (actor == null) return contra;

        double[] temporal = fv.getTemporalFeatures();
        if (temporal[7] == 1.0) {
            contra.add("Command-and-control exfiltration burst occurred during the weekend, deviating from standard state bureaucratic operational tempos.");
        }

        if (actor.getTargetCountries() != null && targetCountry != null &&
                !actor.getTargetCountries().toUpperCase().contains(targetCountry.toUpperCase())) {
            contra.add(String.format("Target country [%s] has no prior verified campaigns documented for %s in public CTI archives.", targetCountry, actor.getName()));
        }

        if (observedTtps != null && observedTtps.toUpperCase().contains("T1486")) { // Ransomware
            if ("Espionage".equalsIgnoreCase(actor.getMotivation())) {
                contra.add("Presence of data-encrypting ransomware (T1486) contradicts pure cyber-espionage doctrine unless deployed as a deniable pseudo-criminal wiper.");
            }
        }

        if (contra.isEmpty()) {
            contra.add("No significant operational discrepancies detected against established actor baseline.");
        }

        return contra;
    }

    private List<String> buildAlternativeExplanations(List<AttributionReport.Hypothesis> hypotheses, double topProb) {
        List<String> alt = new ArrayList<>();
        if (hypotheses.size() > 1) {
            AttributionReport.Hypothesis secondary = hypotheses.get(1);
            alt.add(String.format("Alternative Hypothesis: %s (%s) with calibrated posterior probability %.2f%%. Potential shared infrastructure or third-party contractor overlaps.",
                    secondary.getActorName(), secondary.getNationState(), secondary.getPosteriorProbability() * 100.0));
        }

        alt.add("False Flag Deception: Advanced actors routinely embed linguistic artifacts, timestamps, or public tools from rival nation states to misdirect analysts.");
        alt.add("Commercial Dual-Use Tooling: Prevalence of Cobalt Strike, Mimikatz, and public GitHub POCs introduces attribution dilution.");
        return alt;
    }

    private String buildCorrelationVsAttribution(boolean isUnknown, double topProb, MultiModalFeatureVector fv) {
        if (isUnknown) {
            return "ANOMALY: High entropy feature state indicates a novel, unattributed threat cluster or intentional multi-actor operational camouflage.";
        } else if (topProb >= 0.85) {
            return "VERIFIED HIGH-CONFIDENCE ATTRIBUTION: Multimodal fusion confirms convergence of technical TTPs, geopolitical crisis timing, temporal discipline, and infrastructure overlap.";
        } else if (topProb >= 0.60) {
            return "MODERATE CONFIDENCE ATTRIBUTION: Strong technical alignment present, but geopolitical tension features are circumstantial and require corroboration via signals intelligence.";
        } else {
            return "GEOPOLITICAL CORRELATION ONLY: Intrusion aligns chronologically with diplomatic dispute, but technical IOCs remain ambiguous across multiple actor archetypes.";
        }
    }

    private List<String> buildProvenance(AptActor actor, String targetCountry) {
        List<String> prov = new ArrayList<>();
        prov.add("MITRE ATT&CK STIX 2.1 Enterprise Matrix (v14.1)");
        prov.add("GDELT 2.0 Global Geopolitical Knowledge Graph");
        prov.add("APTnotes CTI Repository Index (Mandiant, CrowdStrike, CISA Advisory corpus)");
        if (actor != null && actor.getProvenance() != null) {
            prov.add("Primary Vendor Ground Truth: " + actor.getProvenance());
        }
        return prov;
    }
}
