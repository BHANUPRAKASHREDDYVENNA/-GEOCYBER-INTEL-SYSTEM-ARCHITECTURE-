package org.geocyber.ai.attribution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AttributionReport {

    public static class Hypothesis {
        private final String actorName;
        private final String nationState;
        private final double posteriorProbability;
        private final double priorProbability;
        private final String motivation;
        private final List<String> matchingTtps;
        private final List<String> matchingMalware;

        public Hypothesis(String actorName, String nationState, double posteriorProbability,
                          double priorProbability, String motivation,
                          List<String> matchingTtps, List<String> matchingMalware) {
            this.actorName = actorName;
            this.nationState = nationState;
            this.posteriorProbability = posteriorProbability;
            this.priorProbability = priorProbability;
            this.motivation = motivation;
            this.matchingTtps = matchingTtps;
            this.matchingMalware = matchingMalware;
        }

        public String getActorName() { return actorName; }
        public String getNationState() { return nationState; }
        public double getPosteriorProbability() { return posteriorProbability; }
        public double getPriorProbability() { return priorProbability; }
        public String getMotivation() { return motivation; }
        public List<String> getMatchingTtps() { return matchingTtps; }
        public List<String> getMatchingMalware() { return matchingMalware; }
    }

    private String reportId;
    private LocalDateTime generatedAt;
    private String topAttributedActor;
    private String topAttributedNation;
    private double calibratedConfidence;
    private boolean unknownActorDetected;
    private double unknownActorScore;
    private List<Hypothesis> rankedHypotheses;
    private Map<String, Double> modalityImportance;
    private double[][] crossAttentionMatrix;
    private List<String> supportingEvidence;
    private List<String> contradictingEvidence;
    private List<String> alternativeExplanations;
    private String correlationVsAttributionNotes;
    private List<String> provenanceTrail;

    public AttributionReport() {}

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getTopAttributedActor() { return topAttributedActor; }
    public void setTopAttributedActor(String topAttributedActor) { this.topAttributedActor = topAttributedActor; }

    public String getTopAttributedNation() { return topAttributedNation; }
    public void setTopAttributedNation(String topAttributedNation) { this.topAttributedNation = topAttributedNation; }

    public double getCalibratedConfidence() { return calibratedConfidence; }
    public void setCalibratedConfidence(double calibratedConfidence) { this.calibratedConfidence = calibratedConfidence; }

    public boolean isUnknownActorDetected() { return unknownActorDetected; }
    public void setUnknownActorDetected(boolean unknownActorDetected) { this.unknownActorDetected = unknownActorDetected; }

    public double getUnknownActorScore() { return unknownActorScore; }
    public void setUnknownActorScore(double unknownActorScore) { this.unknownActorScore = unknownActorScore; }

    public List<Hypothesis> getRankedHypotheses() { return rankedHypotheses; }
    public void setRankedHypotheses(List<Hypothesis> rankedHypotheses) { this.rankedHypotheses = rankedHypotheses; }

    public Map<String, Double> getModalityImportance() { return modalityImportance; }
    public void setModalityImportance(Map<String, Double> modalityImportance) { this.modalityImportance = modalityImportance; }

    public double[][] getCrossAttentionMatrix() { return crossAttentionMatrix; }
    public void setCrossAttentionMatrix(double[][] crossAttentionMatrix) { this.crossAttentionMatrix = crossAttentionMatrix; }

    public List<String> getSupportingEvidence() { return supportingEvidence; }
    public void setSupportingEvidence(List<String> supportingEvidence) { this.supportingEvidence = supportingEvidence; }

    public List<String> getContradictingEvidence() { return contradictingEvidence; }
    public void setContradictingEvidence(List<String> contradictingEvidence) { this.contradictingEvidence = contradictingEvidence; }

    public List<String> getAlternativeExplanations() { return alternativeExplanations; }
    public void setAlternativeExplanations(List<String> alternativeExplanations) { this.alternativeExplanations = alternativeExplanations; }

    public String getCorrelationVsAttributionNotes() { return correlationVsAttributionNotes; }
    public void setCorrelationVsAttributionNotes(String correlationVsAttributionNotes) { this.correlationVsAttributionNotes = correlationVsAttributionNotes; }

    public List<String> getProvenanceTrail() { return provenanceTrail; }
    public void setProvenanceTrail(List<String> provenanceTrail) { this.provenanceTrail = provenanceTrail; }
}
