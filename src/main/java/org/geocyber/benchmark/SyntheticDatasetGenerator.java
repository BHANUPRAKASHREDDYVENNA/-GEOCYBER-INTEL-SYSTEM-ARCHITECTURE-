package org.geocyber.benchmark;

import org.geocyber.ai.features.MultiModalFeatureVector;
import org.geocyber.ai.model.MultimodalCrossAttentionTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SyntheticDatasetGenerator {

    private static final Logger log = LoggerFactory.getLogger(SyntheticDatasetGenerator.class);

    private static final Map<String, String> ACTOR_SPONSOR_MAP = Map.ofEntries(
            Map.entry("APT28", "RU"),
            Map.entry("APT29", "RU"),
            Map.entry("Sandworm", "RU"),
            Map.entry("Turla", "RU"),
            Map.entry("Volt Typhoon", "CN"),
            Map.entry("APT41", "CN"),
            Map.entry("Lazarus Group", "KP"),
            Map.entry("Charming Kitten", "IR"),
            Map.entry("MuddyWater", "IR"),
            Map.entry("Equation Group", "US")
    );

    private static final Map<String, List<String>> CAMPAIGN_MAP = Map.of(
            "APT28", List.of("Operation Ghostwriter", "DNC Breach", "Fancy Bear NATO Spearphishing", "CyberBerkut Echo"),
            "APT29", List.of("SolarWinds Supply Chain", "CozyDuke Foreign Ministry", "Nobelium Mail Delegation", "Midnight Cloud Intrusion"),
            "Sandworm", List.of("BlackEnergy Grid Shutdown", "NotPetya Supply Chain Outbreak", "HermeticWiper Ukraine", "Industroyer2 Substation"),
            "Lazarus Group", List.of("WannaCry Ransomware", "Sony Pictures Hack", "Operation Dream Job", "Axie Infinity Ronin Bridge"),
            "Volt Typhoon", List.of("Guam Critical Infrastructure", "Living Off The Land C2", "Volt Port Enclave", "Maritime Telemetry Probe"),
            "APT41", List.of("Operation Cloud Hopper", "Barium Certificate Theft", "Telecom SMS Sniffer", "Healthcare Supply Pivot"),
            "Charming Kitten", List.of("Operation Phosphorus", "Munich Security Conference Phish", "Mint Sandstorm Thinktank", "BellaCiao Web Shell"),
            "Turla", List.of("Snake Malicious Rootkit", "Satellite C2 Intercept", "Waterbug Exchange Exfil", "ComRAT Diplomacy Breach"),
            "MuddyWater", List.of("Earth Baku Cyber-Espionage", "Mercury Azure Pivot", "Middle East Telecom Mesh", "Sloughter C2"),
            "Equation Group", List.of("Stuxnet Uranium Enrichment", "Fanny Worm USB Intercept", "EternalBlue Exploit Depot", "DoublePulsar Implant")
    );

    /**
     * Generates a realistic, reproducible research dataset of N multimodal campaign incidents.
     */
    public List<MultiModalFeatureVector> generateDataset(int totalSamples, long seed) {
        Random rand = new Random(seed);
        List<MultiModalFeatureVector> dataset = new ArrayList<>();
        List<String> actors = MultimodalCrossAttentionTransformer.ACTOR_CLASSES;

        for (int i = 0; i < totalSamples; i++) {
            String actor = actors.get(rand.nextInt(actors.size()));
            String country = ACTOR_SPONSOR_MAP.get(actor);
            List<String> campaigns = CAMPAIGN_MAP.getOrDefault(actor, List.of("General Intrusion"));
            String campaign = campaigns.get(rand.nextInt(campaigns.size()));

            // 1. News Embedding (64 dims): actor-specific topical signal + gaussian noise
            double[] news = new double[64];
            int actorIdx = actors.indexOf(actor);
            for (int d = 0; d < 64; d++) {
                double signal = (d % 10 == actorIdx) ? 0.75 : 0.05;
                news[d] = Math.max(0.0, signal + rand.nextGaussian() * 0.15);
            }
            normalizeL2(news);

            // 2. GDELT Features (6 dims): Goldstein, Tone, Mentions, Material, Sanctions, Tension
            double[] gdelt = new double[6];
            boolean isConflictActor = "RU".equals(country) || "IR".equals(country) || "KP".equals(country);
            gdelt[0] = isConflictActor ? (0.2 + rand.nextDouble() * 0.3) : (0.5 + rand.nextDouble() * 0.3); // Goldstein (lower = conflict)
            gdelt[1] = isConflictActor ? (0.2 + rand.nextDouble() * 0.3) : (0.5 + rand.nextDouble() * 0.2); // Tone
            gdelt[2] = 0.3 + rand.nextDouble() * 0.5; // Mentions
            gdelt[3] = isConflictActor && rand.nextBoolean() ? 1.0 : 0.0; // Material conflict
            gdelt[4] = isConflictActor ? 1.0 : 0.0; // Sanctions
            gdelt[5] = isConflictActor ? (0.6 + rand.nextDouble() * 0.35) : (0.15 + rand.nextDouble() * 0.25); // Tension index

            // 3. MITRE TTP Vector (30 dims): multi-hot actor doctrinal signature
            double[] ttps = new double[30];
            int primaryTtp = (actorIdx * 3) % 30;
            int secondaryTtp = (actorIdx * 3 + 1) % 30;
            int tertiaryTtp = (actorIdx * 3 + 2) % 30;
            ttps[primaryTtp] = 1.0;
            ttps[secondaryTtp] = 1.0;
            if (rand.nextDouble() > 0.3) ttps[tertiaryTtp] = 1.0;
            // Add slight shared commodity tool noise (Cobalt Strike, PowerShell)
            if (rand.nextDouble() > 0.4) ttps[4] = 1.0; // T1059.001 (PowerShell)
            if (rand.nextDouble() > 0.5) ttps[0] = 1.0; // T1566.001 (Spearphishing)

            // 4. Network IOC Features (16 dims)
            double[] iocs = new double[16];
            iocs[0] = rand.nextDouble() * 0.8; // IP count
            iocs[1] = rand.nextDouble() * 0.7; // Domain count
            iocs[2] = rand.nextDouble() * 0.9; // Hash count
            iocs[3] = rand.nextDouble() * 0.4; // CVE count
            iocs[4] = 0.7 + rand.nextDouble() * 0.25; // Malicious reputation
            iocs[5] = 0.5 + rand.nextDouble() * 0.3;  // Domain entropy
            iocs[8] = 1.0; // Port 443 TLS
            if ("Volt Typhoon".equals(actor)) {
                iocs[12] = 1.0; // Direct IP / compromised SOHO routers
                iocs[1] = 0.05; // Rare domains
            }

            // 5. Temporal Features (8 dims)
            double[] temp = new double[8];
            // Assign active work hour signal according to sponsor country
            int activeHour = 10;
            if ("RU".equals(country)) {
                activeHour = 7 + rand.nextInt(8); // Moscow work hours
                temp[4] = 1.0;
            } else if ("CN".equals(country)) {
                activeHour = 2 + rand.nextInt(8); // Beijing work hours
                temp[5] = 1.0;
            } else if ("IR".equals(country)) {
                activeHour = 6 + rand.nextInt(8); // Tehran work hours
                temp[6] = 1.0;
            } else {
                activeHour = 14 + rand.nextInt(8); // US work hours
            }

            double hourAngle = 2.0 * Math.PI * activeHour / 24.0;
            temp[0] = Math.sin(hourAngle);
            temp[1] = Math.cos(hourAngle);
            temp[2] = Math.sin(2.0 * Math.PI * 3 / 7.0); // Wednesday
            temp[3] = Math.cos(2.0 * Math.PI * 3 / 7.0);
            temp[7] = 0.0; // Not weekend

            dataset.add(new MultiModalFeatureVector(news, gdelt, ttps, iocs, temp, actor, campaign));
        }

        return dataset;
    }

    private void normalizeL2(double[] v) {
        double sum = 0.0;
        for (double val : v) sum += val * val;
        if (sum > 1e-9) {
            double norm = Math.sqrt(sum);
            for (int i = 0; i < v.length; i++) v[i] /= norm;
        }
    }
}
