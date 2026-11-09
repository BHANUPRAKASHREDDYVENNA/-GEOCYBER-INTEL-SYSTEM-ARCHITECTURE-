package org.geocyber.investigation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geocyber.ai.attribution.AttributionReport;
import org.geocyber.ai.attribution.ProbabilisticAttributionEngine;
import org.geocyber.ai.features.FeatureExtractor;
import org.geocyber.ai.features.MultiModalFeatureVector;
import org.geocyber.domain.AttributionCase;
import org.geocyber.repository.AttributionCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class InvestigationCaseService {

    private static final Logger log = LoggerFactory.getLogger(InvestigationCaseService.class);

    private final AttributionCaseRepository caseRepository;
    private final FeatureExtractor featureExtractor;
    private final ProbabilisticAttributionEngine attributionEngine;
    private final ObjectMapper objectMapper;

    public InvestigationCaseService(AttributionCaseRepository caseRepository,
                                  FeatureExtractor featureExtractor,
                                  ProbabilisticAttributionEngine attributionEngine,
                                  ObjectMapper objectMapper) {
        this.caseRepository = caseRepository;
        this.featureExtractor = featureExtractor;
        this.attributionEngine = attributionEngine;
        this.objectMapper = objectMapper;
    }

    public List<AttributionCase> getAllCases() {
        return caseRepository.findTop20ByOrderByCreatedAtDesc();
    }

    public Optional<AttributionCase> getCaseByNumber(String caseNumber) {
        return caseRepository.findByCaseNumber(caseNumber);
    }

    @Transactional
    public AttributionCase createCase(String title, String analyst, LocalDate incidentDate,
                                      String targetCountry, String targetSector,
                                      String observedTtps, String observedIocs,
                                      String geopoliticalContext, String notes) {
        String caseNumber = "CASE-" + LocalDate.now().getYear() + "-" + String.format("%04d", (caseRepository.count() + 1));

        AttributionCase newCase = new AttributionCase(
                caseNumber, title, analyst, "OPEN", incidentDate,
                targetCountry, targetSector, observedTtps, observedIocs,
                geopoliticalContext, "PENDING_ANALYSIS", 0.0, false, notes, ""
        );

        return caseRepository.save(newCase);
    }

    @Transactional
    public AttributionReport runAttributionOnCase(Long caseId) {
        AttributionCase c = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found with ID: " + caseId));

        log.info("Running automated attribution for case {}", c.getCaseNumber());

        // Extract 5-modality feature vector
        MultiModalFeatureVector fv = featureExtractor.extract(
                c.getGeopoliticalContext(),
                "RU", // Default suspected sphere or extracted from context
                c.getIncidentTargetCountry(),
                c.getIncidentDate(),
                c.getIncidentDate() != null ? c.getIncidentDate().atTime(10, 0) : LocalDateTime.now(),
                c.getObservedTtps(),
                c.getObservedIocs(),
                null,
                c.getCaseNumber()
        );

        AttributionReport report = attributionEngine.performAttribution(
                fv, c.getObservedTtps(), c.getObservedIocs(),
                c.getIncidentTargetCountry(), c.getIncidentTargetSector()
        );

        // Update case state
        c.setAttributedActor(report.getTopAttributedActor());
        c.setCalibratedConfidence(report.getCalibratedConfidence());
        c.setIsUnknownActor(report.isUnknownActorDetected());
        c.setStatus("ATTRIBUTED");
        c.setUpdatedAt(LocalDateTime.now());

        try {
            c.setEvidenceBreakdown(objectMapper.writeValueAsString(report));
        } catch (Exception e) {
            log.error("Failed to serialize attribution report JSON", e);
        }

        caseRepository.save(c);
        return report;
    }

    /**
     * Exports a comprehensive intelligence brief in Markdown / STIX-compatible format.
     */
    public String exportIntelligenceReportMarkdown(Long caseId) {
        AttributionCase c = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));

        StringBuilder sb = new StringBuilder();
        sb.append("# CTI ATTRIBUTION INTELLIGENCE REPORT\n");
        sb.append("------------------------------------------------------------------------\n");
        sb.append("**Case Reference:** ").append(c.getCaseNumber()).append("\n");
        sb.append("**Title:** ").append(c.getTitle()).append("\n");
        sb.append("**Lead Analyst:** ").append(c.getLeadAnalyst()).append("\n");
        sb.append("**Status:** ").append(c.getStatus()).append("\n");
        sb.append("**Incident Date:** ").append(c.getIncidentDate()).append("\n");
        sb.append("**Target Nation:** ").append(c.getIncidentTargetCountry()).append("\n");
        sb.append("**Target Sector:** ").append(c.getIncidentTargetSector()).append("\n\n");

        sb.append("## ATTRIBUTION VERDICT\n");
        sb.append("- **Suspected Threat Actor:** ").append(c.getAttributedActor()).append("\n");
        sb.append("- **Calibrated Confidence:** ").append(String.format("%.2f%%", (c.getCalibratedConfidence() != null ? c.getCalibratedConfidence() * 100.0 : 0.0))).append("\n");
        sb.append("- **Novel Unknown Cluster:** ").append(Boolean.TRUE.equals(c.getIsUnknownActor()) ? "YES" : "NO").append("\n\n");

        sb.append("## OBSERVED MITRE ATT&CK TACTICS & TECHNIQUES\n");
        sb.append(c.getObservedTtps() != null ? c.getObservedTtps() : "None logged").append("\n\n");

        sb.append("## FORENSIC INDICATORS OF COMPROMISE\n");
        sb.append(c.getObservedIocs() != null ? c.getObservedIocs() : "None logged").append("\n\n");

        sb.append("## GEOPOLITICAL CONTEXT & CASE NOTES\n");
        sb.append(c.getNotes() != null ? c.getNotes() : "N/A").append("\n\n");

        sb.append("------------------------------------------------------------------------\n");
        sb.append("*Generated by GEOCYBER INTEL Platform - Strict Research CTI Attribution Engine*\n");

        return sb.toString();
    }
}
