package org.geocyber.ai.attribution;

import org.geocyber.ai.features.MultiModalFeatureVector;
import org.geocyber.ai.model.MultimodalCrossAttentionTransformer;
import org.geocyber.domain.AptActor;
import org.geocyber.repository.AptActorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ProbabilisticAttributionEngineTest {

    private AptActorRepository actorRepository;
    private MultimodalCrossAttentionTransformer transformer;
    private ProbabilisticAttributionEngine engine;

    @BeforeEach
    void setUp() {
        actorRepository = Mockito.mock(AptActorRepository.class);
        transformer = new MultimodalCrossAttentionTransformer();

        AptActor sandworm = new AptActor("Sandworm", "RU", "Russia", "BlackEnergy",
                "Sabotage", "Energy", "UA", "T1486, T1562.001", "Industroyer2", 0.98,
                "2009", "2026", true, "GRU Unit 74455", "Mandiant");

        when(actorRepository.findAll()).thenReturn(List.of(sandworm));

        engine = new ProbabilisticAttributionEngine(transformer, actorRepository);
    }

    @Test
    void testPerformAttributionGeneratesValidReport() {
        double[] news = new double[64];
        double[] gdelt = new double[6];
        double[] ttps = new double[30];
        double[] iocs = new double[16];
        double[] temp = new double[8];

        MultiModalFeatureVector fv = new MultiModalFeatureVector(news, gdelt, ttps, iocs, temp, "Sandworm", "CAMP-SCADA");

        AttributionReport report = engine.performAttribution(
                fv, "T1486, T1562.001", "194.26.29.112", "UA", "Energy"
        );

        assertThat(report).isNotNull();
        assertThat(report.getReportId()).startsWith("ATTR-");
        assertThat(report.getRankedHypotheses()).isNotEmpty();
        assertThat(report.getSupportingEvidence()).isNotEmpty();
        assertThat(report.getProvenanceTrail()).isNotEmpty();
    }
}
