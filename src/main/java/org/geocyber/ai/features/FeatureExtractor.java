package org.geocyber.ai.features;

import org.geocyber.domain.IndicatorOfCompromise;
import org.geocyber.ingestion.GdeltIngestionService;
import org.geocyber.ingestion.IocNormalizationService;
import org.geocyber.ingestion.MitreAttackService;
import org.geocyber.nlp.NlpAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeatureExtractor {

    private static final Logger log = LoggerFactory.getLogger(FeatureExtractor.class);

    private final NlpAnalysisService nlpService;
    private final GdeltIngestionService gdeltService;
    private final MitreAttackService mitreService;
    private final IocNormalizationService iocService;

    public FeatureExtractor(NlpAnalysisService nlpService,
                            GdeltIngestionService gdeltService,
                            MitreAttackService mitreService,
                            IocNormalizationService iocService) {
        this.nlpService = nlpService;
        this.gdeltService = gdeltService;
        this.mitreService = mitreService;
        this.iocService = iocService;
    }

    /**
     * Extracts complete multimodal feature vector from raw incident and CTI data.
     */
    public MultiModalFeatureVector extract(String newsText,
                                          String sourceCountry,
                                          String targetCountry,
                                          LocalDate incidentDate,
                                          LocalDateTime incidentTimestamp,
                                          String observedTtps,
                                          String rawIocs,
                                          String groundTruthActor,
                                          String campaignId) {

        // 1. Modality 1: Geopolitical News Embedding (64 dims)
        double[] newsEmbedding = nlpService.generateNewsEmbedding(newsText);

        // 2. Modality 2: GDELT Conflict & Tension Features (6 dims)
        double[] gdeltFeatures = gdeltService.extractGdeltFeatures(
                sourceCountry != null ? sourceCountry : "RU",
                targetCountry != null ? targetCountry : "UA",
                incidentDate != null ? incidentDate : LocalDate.now()
        );

        // 3. Modality 3: MITRE ATT&CK TTP Features (30 dims)
        double[] ttpFeatures = mitreService.encodeTtpVector(observedTtps);

        // 4. Modality 4: Infrastructure & Network IOC Features (16 dims)
        double[] iocFeatures = extractIocFeatures(rawIocs);

        // 5. Modality 5: Campaign Temporal Features (8 dims)
        double[] temporalFeatures = extractTemporalFeatures(incidentTimestamp != null ? incidentTimestamp : LocalDateTime.now());

        return new MultiModalFeatureVector(newsEmbedding, gdeltFeatures, ttpFeatures, iocFeatures, temporalFeatures, groundTruthActor, campaignId);
    }

    /**
     * Extracts a 16-dimensional infrastructure feature vector from IOC strings.
     */
    public double[] extractIocFeatures(String rawIocs) {
        double[] features = new double[16];
        if (rawIocs == null || rawIocs.isBlank()) {
            return features;
        }

        List<IndicatorOfCompromise> parsed = iocService.extractFromText(rawIocs, "INCIDENT_EXTRACTION", "");

        int ipCount = 0;
        int domainCount = 0;
        int hashCount = 0;
        int cveCount = 0;
        double sumReputation = 0.0;
        double domainEntropySum = 0.0;

        for (IndicatorOfCompromise ioc : parsed) {
            sumReputation += ioc.getReputationScore() != null ? ioc.getReputationScore() : 0.5;
            switch (ioc.getIocType()) {
                case IPV4, IPV6 -> ipCount++;
                case DOMAIN, URL -> {
                    domainCount++;
                    domainEntropySum += calculateShannonEntropy(ioc.getNormalizedValue());
                }
                case MD5, SHA1, SHA256 -> hashCount++;
                case CVE -> cveCount++;
                default -> {}
            }
        }

        int total = Math.max(1, parsed.size());

        features[0] = Math.min(1.0, ipCount / 10.0);
        features[1] = Math.min(1.0, domainCount / 10.0);
        features[2] = Math.min(1.0, hashCount / 10.0);
        features[3] = Math.min(1.0, cveCount / 5.0);
        features[4] = sumReputation / total; // Mean reputation
        features[5] = domainCount > 0 ? (domainEntropySum / domainCount) / 5.0 : 0.0; // Normalized Shannon entropy
        features[6] = rawIocs.toLowerCase().contains("tor") || rawIocs.contains(".onion") ? 1.0 : 0.0;
        features[7] = rawIocs.toLowerCase().contains("duckdns") || rawIocs.toLowerCase().contains("no-ip") ? 1.0 : 0.0;
        features[8] = rawIocs.contains(":443") || rawIocs.contains(":8443") ? 1.0 : 0.0; // TLS C2
        features[9] = rawIocs.contains(":80") || rawIocs.contains(":8080") ? 1.0 : 0.0;  // Plain HTTP
        features[10] = rawIocs.contains(":53") ? 1.0 : 0.0;                              // DNS tunneling
        features[11] = rawIocs.contains(":22") || rawIocs.contains(":3389") ? 1.0 : 0.0; // SSH / RDP
        features[12] = (ipCount > 0 && domainCount == 0) ? 1.0 : 0.0;                   // Direct IP C2
        features[13] = (cveCount > 0) ? 1.0 : 0.0;                                       // Exploits unpatched CVE
        features[14] = Math.min(1.0, total / 20.0);                                      // IOC Density
        features[15] = Math.min(1.0, Math.log1p(total) / 4.0);

        return features;
    }

    /**
     * Extracts an 8-dimensional temporal feature vector encoding timezone, workday, and cycle.
     */
    public double[] extractTemporalFeatures(LocalDateTime dt) {
        double[] features = new double[8];
        int hour = dt.getHour();
        int dayOfWeek = dt.getDayOfWeek().getValue(); // 1 (Mon) to 7 (Sun)

        // Cyclical time encodings (sin & cos)
        double hourAngle = 2.0 * Math.PI * hour / 24.0;
        features[0] = Math.sin(hourAngle);
        features[1] = Math.cos(hourAngle);

        double dayAngle = 2.0 * Math.PI * dayOfWeek / 7.0;
        features[2] = Math.sin(dayAngle);
        features[3] = Math.cos(dayAngle);

        // State Working Hour Alignments (UTC basis):
        // Moscow Time (UTC+3): 09:00 - 18:00 MSK -> 06:00 - 15:00 UTC
        int utcHour = dt.getHour(); // Assuming UTC input
        features[4] = (utcHour >= 6 && utcHour <= 15 && dayOfWeek <= 5) ? 1.0 : 0.0;

        // Beijing Time (UTC+8): 09:00 - 18:00 CST -> 01:00 - 10:00 UTC
        features[5] = (utcHour >= 1 && utcHour <= 10 && dayOfWeek <= 5) ? 1.0 : 0.0;

        // Tehran Time (UTC+3:30): 08:00 - 17:00 IRST (Workweek Sat-Wed)
        features[6] = (utcHour >= 5 && utcHour <= 14 && (dayOfWeek == 6 || dayOfWeek == 7 || dayOfWeek <= 3)) ? 1.0 : 0.0;

        // Weekend activity flag
        features[7] = (dayOfWeek == 6 || dayOfWeek == 7) ? 1.0 : 0.0;

        return features;
    }

    private double calculateShannonEntropy(String s) {
        if (s == null || s.isEmpty()) return 0.0;
        int[] freq = new int[256];
        for (char c : s.toCharArray()) {
            if (c < 256) freq[c]++;
        }
        double entropy = 0.0;
        int len = s.length();
        for (int f : freq) {
            if (f > 0) {
                double p = (double) f / len;
                entropy -= p * (Math.log(p) / Math.log(2.0));
            }
        }
        return entropy;
    }
}
