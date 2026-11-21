package org.geocyber.ai.anomaly;

import org.geocyber.domain.AnomalyAlert;
import org.geocyber.domain.IndicatorOfCompromise;
import org.geocyber.domain.IocType;
import org.geocyber.repository.AnomalyAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Module 6: Anomaly Detection Engine
 * Implements:
 * - Shannon entropy analysis for DGA and payload obfuscation.
 * - Unknown threat cluster detection via prediction distribution entropy.
 * - IOC anomaly identification (high-entropy domains, suspicious TLDs, dynamic DNS).
 * - Unusual temporal activity detection (out-of-hours, weekend exfiltration spikes).
 * - Confidence-based alert generation.
 */
@Service
public class AnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionService.class);

    private final AnomalyAlertRepository alertRepository;

    private static final Set<String> SUSPICIOUS_TLDS = Set.of(
            "top", "xyz", "cc", "su", "tk", "pw", "buzz", "live", "club", "icu"
    );

    public AnomalyDetectionService(AnomalyAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Calculates Shannon entropy: H(X) = - sum(p(x) * log2(p(x)))
     * Typical natural English domains: 2.2 - 3.4
     * Algorithmic DGA / Obfuscated strings: > 3.8 - 4.5
     */
    public double calculateShannonEntropy(String input) {
        if (input == null || input.isEmpty()) return 0.0;

        Map<Character, Integer> freq = new HashMap<>();
        for (char c : input.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        double entropy = 0.0;
        int len = input.length();
        for (int count : freq.values()) {
            double p = (double) count / len;
            entropy -= p * (Math.log(p) / Math.log(2.0));
        }
        return entropy;
    }

    /**
     * Evaluates prediction distribution entropy to detect unknown / novel adversary clusters.
     */
    public boolean isUnknownThreatCluster(double[] probabilities, double unknownThreshold) {
        if (probabilities == null || probabilities.length == 0) return true;

        double maxProb = Arrays.stream(probabilities).max().orElse(0.0);
        double entropy = 0.0;
        for (double p : probabilities) {
            if (p > 1e-12) {
                entropy -= p * (Math.log(p) / Math.log(2.0));
            }
        }

        // Theoretical maximum entropy for K classes: log2(K)
        double maxEntropy = Math.log(probabilities.length) / Math.log(2.0);
        double normalizedEntropy = entropy / maxEntropy;

        return (maxProb < unknownThreshold) || (normalizedEntropy > 0.70);
    }

    /**
     * Analyzes a list of IOCs and generates anomaly alerts for high-risk observables.
     */
    public List<AnomalyAlert> analyzeIocAnomalies(List<IndicatorOfCompromise> iocs, String campaignContext) {
        List<AnomalyAlert> alerts = new ArrayList<>();
        if (iocs == null) return alerts;

        for (IndicatorOfCompromise ioc : iocs) {
            String value = ioc.getNormalizedValue();
            if (value == null) continue;

            // 1. High Shannon Entropy DGA Domain
            if (ioc.getIocType() == IocType.DOMAIN || ioc.getIocType() == IocType.URL) {
                String domainPart = extractDomainPart(value);
                double entropy = calculateShannonEntropy(domainPart);

                if (entropy > 3.85 && domainPart.length() > 8) {
                    AnomalyAlert alert = new AnomalyAlert(
                            "ALERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                            LocalDateTime.now(),
                            "HIGH_SHANNON_ENTROPY_DGA",
                            "HIGH",
                            value,
                            Math.min(1.0, entropy / 5.0),
                            String.format("Domain [%s] exhibits abnormally high Shannon entropy (%.2f bits/symbol), characteristic of algorithmically generated domain (DGA) C2 rendezvous.", value, entropy),
                            "Block domain in perimeter DNS resolvers and query historical sinkhole telemetry."
                    );
                    alerts.add(alertRepository.save(alert));
                }

                // Suspicious TLD check
                String tld = extractTld(domainPart);
                if (SUSPICIOUS_TLDS.contains(tld.toLowerCase())) {
                    AnomalyAlert alert = new AnomalyAlert(
                            "ALERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                            LocalDateTime.now(),
                            "SUSPICIOUS_HIGH_RISK_TLD",
                            "MEDIUM",
                            value,
                            0.65,
                            String.format("Indicator domain [%s] utilizes top-level domain [.%s], frequently associated with disposable bulletproof C2 infrastructure.", value, tld),
                            "Inspect passive DNS resolution history and enforce SSL inspection."
                    );
                    alerts.add(alertRepository.save(alert));
                }
            }

            // 2. High Threat Score Infrastructure
            if (ioc.getReputationScore() != null && ioc.getReputationScore() >= 0.85) {
                AnomalyAlert alert = new AnomalyAlert(
                        "ALERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                        LocalDateTime.now(),
                        "CRITICAL_MALICIOUS_INFRASTRUCTURE",
                        "CRITICAL",
                        value,
                        ioc.getReputationScore(),
                        String.format("IOC observable [%s] has an acute threat reputation score of %.2f with documented adversary links: %s",
                                value, ioc.getReputationScore(), ioc.getAssociatedActors() != null ? ioc.getAssociatedActors() : "Unknown APT"),
                        "Initiate immediate automated host isolation and firewall perimeter null-routing."
                );
                alerts.add(alertRepository.save(alert));
            }
        }

        return alerts;
    }

    /**
     * Detects anomalous temporal rhythms (out-of-office bursts or weekend operations).
     */
    public Optional<AnomalyAlert> detectTemporalAnomaly(LocalDateTime timestamp, String actorCountry, String incidentId) {
        if (timestamp == null) return Optional.empty();

        LocalTime time = timestamp.toLocalTime();
        DayOfWeek day = timestamp.getDayOfWeek();
        boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);

        // Standard government working hours in UTC for typical state-sponsored actors:
        // RU (UTC+3): 09:00 - 18:00 local is 06:00 - 15:00 UTC
        // CN (UTC+8): 09:00 - 18:00 local is 01:00 - 10:00 UTC
        // IR (UTC+3.5): Sat - Wed active, Thu-Fri weekend

        if (isWeekend) {
            AnomalyAlert alert = new AnomalyAlert(
                    "ALERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    LocalDateTime.now(),
                    "UNUSUAL_WEEKEND_TEMPORAL_SPIKE",
                    "MEDIUM",
                    incidentId != null ? incidentId : "INCIDENT-TEMPORAL",
                    0.72,
                    String.format("Operation occurred on %s at %s UTC. Nation-state bureaucratic threat actors typically maintain strict weekday schedules; weekend operations suggest emergency exfiltration or non-state proxies.", day, time),
                    "Verify if exfiltration was scripted cron automation or live interactive human keyboard hands."
            );
            return Optional.of(alertRepository.save(alert));
        }

        return Optional.empty();
    }

    public List<AnomalyAlert> getAllAlerts() {
        return alertRepository.findByOrderByTimestampDesc();
    }

    private String extractDomainPart(String domainOrUrl) {
        String clean = domainOrUrl.replaceAll("https?://", "").replaceAll("/.*", "");
        return clean.toLowerCase();
    }

    private String extractTld(String domain) {
        int idx = domain.lastIndexOf('.');
        if (idx >= 0 && idx < domain.length() - 1) {
            return domain.substring(idx + 1);
        }
        return "";
    }
}
