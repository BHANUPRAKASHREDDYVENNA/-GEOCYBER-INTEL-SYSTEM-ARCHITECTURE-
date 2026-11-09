package org.geocyber.ingestion;

import org.geocyber.domain.GdeltEvent;
import org.geocyber.repository.GdeltEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class GdeltIngestionService {

    private static final Logger log = LoggerFactory.getLogger(GdeltIngestionService.class);

    private final GdeltEventRepository gdeltRepository;

    public GdeltIngestionService(GdeltEventRepository gdeltRepository) {
        this.gdeltRepository = gdeltRepository;
    }

    public List<GdeltEvent> getRecentEvents() {
        return gdeltRepository.findTop20ByOrderByEventDateDesc();
    }

    public List<GdeltEvent> getBilateralEvents(String country1, String country2) {
        return gdeltRepository.findBilateralEvents(country1.toUpperCase(), country2.toUpperCase());
    }

    /**
     * Calculates the Geopolitical Tension Index between two nations over a temporal window.
     * Normalized between 0.0 (calm/peaceful) and 1.0 (imminent conflict/active hostilities).
     */
    public double calculateBilateralTension(String country1, String country2, LocalDate start, LocalDate end) {
        List<GdeltEvent> events = gdeltRepository.findBilateralEvents(country1.toUpperCase(), country2.toUpperCase());
        if (events.isEmpty()) {
            return 0.15; // baseline peaceful default
        }

        double conflictSum = 0.0;
        double totalMentions = 0.0;

        for (GdeltEvent e : events) {
            if (e.getEventDate() != null &&
                    (e.getEventDate().isEqual(start) || e.getEventDate().isAfter(start)) &&
                    (e.getEventDate().isEqual(end) || e.getEventDate().isBefore(end))) {

                double goldstein = e.getGoldsteinScale() != null ? e.getGoldsteinScale() : 0.0;
                double mentions = e.getNumMentions() != null ? Math.log1p(e.getNumMentions()) : 1.0;

                // Goldstein < 0 represents conflict events (e.g. sanctions, military posturing, expulsions, threats)
                if (goldstein < 0) {
                    conflictSum += Math.abs(goldstein) * mentions;
                }
                totalMentions += mentions;
            }
        }

        if (totalMentions == 0.0) return 0.20;
        double rawIndex = conflictSum / (totalMentions * 10.0);
        return Math.min(1.0, Math.max(0.0, rawIndex));
    }

    /**
     * Extracts a numerical feature vector representing geopolitical conflict state for AI model ingestion.
     * Dimensions = 6:
     * [0] Normalized Goldstein Scale (-10..+10 -> 0..1)
     * [1] Average Tone (-10..+10 -> 0..1)
     * [2] Log Number of Mentions
     * [3] Material Conflict Flag (CAMEO 180-200)
     * [4] Diplomatic Sanction / Expulsion Flag (CAMEO 110-160)
     * [5] Bilateral Tension Score
     */
    public double[] extractGdeltFeatures(String sourceCountry, String targetCountry, LocalDate incidentDate) {
        double[] features = new double[6];
        LocalDate start = incidentDate.minusDays(14);
        LocalDate end = incidentDate.plusDays(7);

        List<GdeltEvent> events = gdeltRepository.findBilateralEvents(sourceCountry, targetCountry);
        if (events.isEmpty()) {
            events = gdeltRepository.findByActionGeoCountryOrderByEventDateDesc(targetCountry);
        }

        if (events.isEmpty()) {
            features[0] = 0.5; // neutral
            features[1] = 0.5; // neutral
            features[2] = 0.1;
            features[3] = 0.0;
            features[4] = 0.0;
            features[5] = 0.2;
            return features;
        }

        double sumGoldstein = 0.0;
        double sumTone = 0.0;
        double sumMentions = 0.0;
        boolean hasMaterialConflict = false;
        boolean hasSanctionOrExpulsion = false;
        int count = 0;

        for (GdeltEvent ev : events) {
            count++;
            sumGoldstein += (ev.getGoldsteinScale() != null ? ev.getGoldsteinScale() : 0.0);
            sumTone += (ev.getAvgTone() != null ? ev.getAvgTone() : 0.0);
            sumMentions += (ev.getNumMentions() != null ? ev.getNumMentions() : 1);

            String code = ev.getEventCode() != null ? ev.getEventCode() : "";
            if (code.startsWith("18") || code.startsWith("19") || code.startsWith("20")) {
                hasMaterialConflict = true;
            }
            if (code.startsWith("11") || code.startsWith("12") || code.startsWith("16")) {
                hasSanctionOrExpulsion = true;
            }
        }

        double avgGoldstein = count > 0 ? (sumGoldstein / count) : 0.0;
        double avgTone = count > 0 ? (sumTone / count) : 0.0;

        features[0] = (avgGoldstein + 10.0) / 20.0; // scale -10..+10 to 0..1
        features[1] = (avgTone + 10.0) / 20.0;       // scale -10..+10 to 0..1
        features[2] = Math.min(1.0, Math.log1p(sumMentions) / 10.0);
        features[3] = hasMaterialConflict ? 1.0 : 0.0;
        features[4] = hasSanctionOrExpulsion ? 1.0 : 0.0;
        features[5] = calculateBilateralTension(sourceCountry, targetCountry, start, end);

        return features;
    }

    @Transactional
    public GdeltEvent save(GdeltEvent event) {
        return gdeltRepository.save(event);
    }
}
