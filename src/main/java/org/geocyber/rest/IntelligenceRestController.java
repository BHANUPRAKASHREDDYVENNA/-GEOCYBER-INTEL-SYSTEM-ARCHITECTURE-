package org.geocyber.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.geocyber.ai.anomaly.AnomalyDetectionService;
import org.geocyber.domain.*;
import org.geocyber.graph.ThreatGraphService;
import org.geocyber.ingestion.AptActorService;
import org.geocyber.ingestion.GdeltIngestionService;
import org.geocyber.ingestion.IocNormalizationService;
import org.geocyber.ingestion.MitreAttackService;
import org.geocyber.repository.IocRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/intelligence")
@Tag(name = "Threat Intelligence & Anomaly Operations", description = "Endpoints for CTI observables, GDELT events, MITRE ATT&CK, threat graphs, and anomaly detection")
public class IntelligenceRestController {

    private final AptActorService actorService;
    private final GdeltIngestionService gdeltService;
    private final MitreAttackService mitreService;
    private final IocNormalizationService iocService;
    private final IocRepository iocRepository;
    private final ThreatGraphService threatGraphService;
    private final AnomalyDetectionService anomalyDetectionService;

    public IntelligenceRestController(AptActorService actorService,
                                      GdeltIngestionService gdeltService,
                                      MitreAttackService mitreService,
                                      IocNormalizationService iocService,
                                      IocRepository iocRepository,
                                      ThreatGraphService threatGraphService,
                                      AnomalyDetectionService anomalyDetectionService) {
        this.actorService = actorService;
        this.gdeltService = gdeltService;
        this.mitreService = mitreService;
        this.iocService = iocService;
        this.iocRepository = iocRepository;
        this.threatGraphService = threatGraphService;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @Operation(summary = "List all canonical APT threat actors and state sponsorships")
    @GetMapping("/actors")
    public List<AptActor> getAllActors() {
        return actorService.getAllActors();
    }

    @Operation(summary = "Resolve known APT alias to canonical threat actor name")
    @GetMapping("/actors/resolve")
    public ResponseEntity<?> resolveActorAlias(@RequestParam String alias) {
        String canonical = actorService.resolveAlias(alias);
        return ResponseEntity.ok(Map.of("queryAlias", alias, "canonicalActor", canonical));
    }

    @Operation(summary = "Retrieve recent GDELT 2.0 geopolitical events")
    @GetMapping("/gdelt/events")
    public List<GdeltEvent> getRecentGdeltEvents() {
        return gdeltService.getRecentEvents();
    }

    @Operation(summary = "Calculate bilateral international tension index between two nation states")
    @GetMapping("/gdelt/tension")
    public ResponseEntity<?> getBilateralTension(@RequestParam String country1, @RequestParam String country2) {
        double tension = gdeltService.calculateBilateralTension(country1, country2,
                java.time.LocalDate.now().minusDays(30), java.time.LocalDate.now());
        return ResponseEntity.ok(Map.of("country1", country1, "country2", country2, "tensionIndex", tension));
    }

    @Operation(summary = "List all MITRE ATT&CK Enterprise techniques")
    @GetMapping("/mitre/techniques")
    public List<MitreAttackTechnique> getAllTechniques() {
        return mitreService.getAllTechniques();
    }

    @Operation(summary = "Get recent Indicators of Compromise (IOCs)")
    @GetMapping("/iocs")
    public List<IndicatorOfCompromise> getRecentIocs() {
        return iocRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @Operation(summary = "Defang malicious IP, URL, or domain to prevent accidental execution")
    @PostMapping("/iocs/defang")
    public ResponseEntity<?> defangIndicator(@RequestBody Map<String, String> body) {
        String raw = body.getOrDefault("indicator", "");
        return ResponseEntity.ok(Map.of(
                "raw", raw,
                "defanged", iocService.defang(raw),
                "normalized", iocService.normalize(raw)
        ));
    }

    @Operation(summary = "Retrieve threat knowledge graph nodes, links, and centrality metrics")
    @GetMapping("/graph")
    public ThreatGraphService.ThreatGraphModel getThreatGraph() {
        return threatGraphService.getVisualThreatGraph();
    }

    @Operation(summary = "Retrieve active threat anomaly alerts (Module 6)")
    @GetMapping("/anomalies")
    public List<AnomalyAlert> getAnomalyAlerts() {
        return anomalyDetectionService.getAllAlerts();
    }

    @Operation(summary = "Calculate Shannon entropy on domain or payload string")
    @PostMapping("/anomalies/detect-entropy")
    public ResponseEntity<?> detectEntropy(@RequestBody Map<String, String> body) {
        String input = body.getOrDefault("input", "");
        double entropy = anomalyDetectionService.calculateShannonEntropy(input);
        boolean isHighEntropy = entropy > 3.85;
        return ResponseEntity.ok(Map.of(
                "input", input,
                "shannonEntropy", entropy,
                "isHighEntropyDga", isHighEntropy,
                "evaluation", isHighEntropy ? "Anomalous high entropy characteristic of DGA rendezvous" : "Standard nominal linguistic entropy"
        ));
    }
}
