package org.geocyber.ai.anomaly;

import org.geocyber.domain.AnomalyAlert;
import org.geocyber.domain.IndicatorOfCompromise;
import org.geocyber.domain.IocType;
import org.geocyber.repository.AnomalyAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AnomalyDetectionServiceTest {

    private AnomalyAlertRepository alertRepository;
    private AnomalyDetectionService anomalyService;

    @BeforeEach
    void setUp() {
        alertRepository = Mockito.mock(AnomalyAlertRepository.class);
        when(alertRepository.save(any(AnomalyAlert.class))).thenAnswer(inv -> inv.getArgument(0));
        anomalyService = new AnomalyDetectionService(alertRepository);
    }

    @Test
    @DisplayName("Should calculate higher Shannon entropy for DGA domain than natural domain")
    void testShannonEntropy() {
        String naturalDomain = "defense-ministry-portal.gov";
        String dgaDomain = "k9x7qm2v0b8p1lz4w.top";

        double naturalEntropy = anomalyService.calculateShannonEntropy(naturalDomain);
        double dgaEntropy = anomalyService.calculateShannonEntropy(dgaDomain);

        assertTrue(dgaEntropy > naturalEntropy, "DGA domain must have higher Shannon entropy than natural text");
        assertTrue(dgaEntropy > 3.8, "DGA domain entropy should exceed typical algorithmic threshold");
    }

    @Test
    @DisplayName("Should detect unknown threat cluster when prediction probabilities have high entropy or low max prob")
    void testUnknownThreatClusterDetection() {
        // Uniform distribution (high entropy) across 10 classes
        double[] uniformProbs = new double[]{0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10, 0.10};
        boolean isUnknown = anomalyService.isUnknownThreatCluster(uniformProbs, 0.35);
        assertTrue(isUnknown, "Uniform prediction distribution must be detected as an unknown threat cluster");

        // High confidence prediction (low entropy)
        double[] confidentProbs = new double[]{0.92, 0.02, 0.01, 0.01, 0.01, 0.01, 0.01, 0.005, 0.005, 0.005};
        boolean isKnown = anomalyService.isUnknownThreatCluster(confidentProbs, 0.35);
        assertFalse(isKnown, "High-confidence attribution must not be flagged as unknown cluster");
    }

    @Test
    @DisplayName("Should generate alerts for high-risk IOC observables and suspicious TLDs")
    void testIocAnomalies() {
        IndicatorOfCompromise dgaIoc = new IndicatorOfCompromise(
                IocType.DOMAIN, "http://a8x29m31qp7.xyz", "a8x29m31qp7.xyz", "a8x29m31qp7[.]xyz",
                0.92, "HIGH", "AlienVault", "Unknown", "Baltic Probe"
        );

        List<AnomalyAlert> alerts = anomalyService.analyzeIocAnomalies(List.of(dgaIoc), "Baltic Probe");
        assertFalse(alerts.isEmpty(), "Should generate alerts for high risk and high entropy domain with .xyz TLD");
    }

    @Test
    @DisplayName("Should detect weekend operations as temporal anomaly for state bureaucratic actors")
    void testTemporalAnomaly() {
        // Sunday timestamp
        LocalDateTime sunday = LocalDateTime.of(2026, 11, 15, 2, 30);
        Optional<AnomalyAlert> alert = anomalyService.detectTemporalAnomaly(sunday, "RU", "INCIDENT-001");

        assertTrue(alert.isPresent(), "Weekend activity must trigger temporal anomaly alert");
        assertEquals("UNUSUAL_WEEKEND_TEMPORAL_SPIKE", alert.get().getAnomalyType());
    }
}
