package org.geocyber.rest;

import org.geocyber.domain.AptActor;
import org.geocyber.domain.GdeltEvent;
import org.geocyber.domain.IndicatorOfCompromise;
import org.geocyber.domain.MitreAttackTechnique;
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
public class IntelligenceRestController {

    private final AptActorService actorService;
    private final GdeltIngestionService gdeltService;
    private final MitreAttackService mitreService;
    private final IocNormalizationService iocService;
    private final IocRepository iocRepository;
    private final ThreatGraphService threatGraphService;

    public IntelligenceRestController(AptActorService actorService,
                                     GdeltIngestionService gdeltService,
                                     MitreAttackService mitreService,
                                     IocNormalizationService iocService,
                                     IocRepository iocRepository,
                                     ThreatGraphService threatGraphService) {
        this.actorService = actorService;
        this.gdeltService = gdeltService;
        this.mitreService = mitreService;
        this.iocService = iocService;
        this.iocRepository = iocRepository;
        this.threatGraphService = threatGraphService;
    }

    @GetMapping("/actors")
    public List<AptActor> getAllActors() {
        return actorService.getAllActors();
    }

    @GetMapping("/actors/resolve")
    public ResponseEntity<?> resolveActorAlias(@RequestParam String alias) {
        String canonical = actorService.resolveAlias(alias);
        return ResponseEntity.ok(Map.of("queryAlias", alias, "canonicalActor", canonical));
    }

    @GetMapping("/gdelt/events")
    public List<GdeltEvent> getRecentGdeltEvents() {
        return gdeltService.getRecentEvents();
    }

    @GetMapping("/gdelt/tension")
    public ResponseEntity<?> getBilateralTension(@RequestParam String country1, @RequestParam String country2) {
        double tension = gdeltService.calculateBilateralTension(country1, country2,
                java.time.LocalDate.now().minusDays(30), java.time.LocalDate.now());
        return ResponseEntity.ok(Map.of("country1", country1, "country2", country2, "tensionIndex", tension));
    }

    @GetMapping("/mitre/techniques")
    public List<MitreAttackTechnique> getAllTechniques() {
        return mitreService.getAllTechniques();
    }

    @GetMapping("/iocs")
    public List<IndicatorOfCompromise> getRecentIocs() {
        return iocRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @PostMapping("/iocs/defang")
    public ResponseEntity<?> defangIndicator(@RequestBody Map<String, String> body) {
        String raw = body.getOrDefault("indicator", "");
        return ResponseEntity.ok(Map.of(
                "raw", raw,
                "defanged", iocService.defang(raw),
                "normalized", iocService.normalize(raw)
        ));
    }

    @GetMapping("/graph")
    public ThreatGraphService.ThreatGraphModel getThreatGraph() {
        return threatGraphService.getVisualThreatGraph();
    }
}
