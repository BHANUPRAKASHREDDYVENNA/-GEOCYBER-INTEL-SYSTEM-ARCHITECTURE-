package org.geocyber.graph;

import org.geocyber.domain.AptActor;
import org.geocyber.domain.CyberCampaign;
import org.geocyber.repository.AptActorRepository;
import org.geocyber.repository.CyberCampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ThreatGraphServiceTest {

    private AptActorRepository actorRepository;
    private CyberCampaignRepository campaignRepository;
    private ThreatGraphService graphService;

    @BeforeEach
    void setUp() {
        actorRepository = Mockito.mock(AptActorRepository.class);
        campaignRepository = Mockito.mock(CyberCampaignRepository.class);

        AptActor sandworm = new AptActor("Sandworm", "RU", "Russia", "BlackEnergy",
                "Sabotage", "Energy", "UA", "T1486", "Industroyer2", 0.98,
                "2009", "2026", true, "GRU", "ESET");

        CyberCampaign camp = new CyberCampaign("HermeticWiper Substation Assault", "Sandworm",
                null, null, "UA", "Energy", "Blackout", "T1486", "Industroyer2", "194.26.29.112", 0.95);

        when(actorRepository.findAll()).thenReturn(List.of(sandworm));
        when(campaignRepository.findAll()).thenReturn(List.of(camp));

        graphService = new ThreatGraphService(actorRepository, campaignRepository);
    }

    @Test
    void testComputeCentrality() {
        Map<String, Double> scores = graphService.computeCentralityScores();
        assertThat(scores).isNotEmpty();
        assertThat(scores).containsKey("country:RU");
    }

    @Test
    void testFindAttributionPath() {
        List<String> path = graphService.findAttributionPath("actor:Sandworm", "RU");
        assertThat(path).isNotEmpty();
        assertThat(path).containsExactly("actor:Sandworm", "country:RU");
    }
}
