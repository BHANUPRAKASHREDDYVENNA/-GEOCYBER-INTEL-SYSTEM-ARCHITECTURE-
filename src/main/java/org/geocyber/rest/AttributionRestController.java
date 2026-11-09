package org.geocyber.rest;

import org.geocyber.ai.attribution.AttributionReport;
import org.geocyber.ai.attribution.ProbabilisticAttributionEngine;
import org.geocyber.ai.features.FeatureExtractor;
import org.geocyber.ai.features.MultiModalFeatureVector;
import org.geocyber.domain.AttributionCase;
import org.geocyber.investigation.InvestigationCaseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/attribution")
public class AttributionRestController {

    private final ProbabilisticAttributionEngine attributionEngine;
    private final FeatureExtractor featureExtractor;
    private final InvestigationCaseService caseService;

    public AttributionRestController(ProbabilisticAttributionEngine attributionEngine,
                                     FeatureExtractor featureExtractor,
                                     InvestigationCaseService caseService) {
        this.attributionEngine = attributionEngine;
        this.featureExtractor = featureExtractor;
        this.caseService = caseService;
    }

    public record AttributionRequest(
            String geopoliticalContext,
            String sourceCountry,
            String targetCountry,
            String targetSector,
            String observedTtps,
            String rawIocs,
            String incidentDate
    ) {}

    @PostMapping("/predict")
    public AttributionReport predictAttribution(@RequestBody AttributionRequest req) {
        LocalDate incDate = req.incidentDate() != null ? LocalDate.parse(req.incidentDate()) : LocalDate.now();

        MultiModalFeatureVector fv = featureExtractor.extract(
                req.geopoliticalContext(),
                req.sourceCountry() != null ? req.sourceCountry() : "RU",
                req.targetCountry() != null ? req.targetCountry() : "UA",
                incDate,
                incDate.atTime(10, 0),
                req.observedTtps(),
                req.rawIocs(),
                null,
                "API-ON-DEMAND"
        );

        return attributionEngine.performAttribution(
                fv, req.observedTtps(), req.rawIocs(), req.targetCountry(), req.targetSector()
        );
    }

    @GetMapping("/cases")
    public List<AttributionCase> listCases() {
        return caseService.getAllCases();
    }

    @PostMapping("/cases")
    public AttributionCase createCase(@RequestBody AttributionCase c) {
        return caseService.createCase(
                c.getTitle(), c.getLeadAnalyst(), c.getIncidentDate(),
                c.getIncidentTargetCountry(), c.getIncidentTargetSector(),
                c.getObservedTtps(), c.getObservedIocs(),
                c.getGeopoliticalContext(), c.getNotes()
        );
    }

    @PostMapping("/cases/{id}/execute")
    public AttributionReport runCaseAttribution(@PathVariable Long id) {
        return caseService.runAttributionOnCase(id);
    }

    @GetMapping(value = "/cases/{id}/report", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> exportReport(@PathVariable Long id) {
        String report = caseService.exportIntelligenceReportMarkdown(id);
        return ResponseEntity.ok(report);
    }
}
