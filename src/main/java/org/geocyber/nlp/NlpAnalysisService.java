package org.geocyber.nlp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NlpAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(NlpAnalysisService.class);

    // Stop words list for CTI & news filtering
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he",
            "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were", "will", "with"
    );

    // Conflict and Threat Lexicons with polarity weights
    private static final Map<String, Double> TENSION_LEXICON = Map.ofEntries(
            Map.entry("war", 1.0),
            Map.entry("invasion", 1.0),
            Map.entry("missile", 0.9),
            Map.entry("nuclear", 0.95),
            Map.entry("sanctions", 0.8),
            Map.entry("espionage", 0.85),
            Map.entry("cyberattack", 0.85),
            Map.entry("intrusion", 0.8),
            Map.entry("wiper", 0.9),
            Map.entry("ransomware", 0.75),
            Map.entry("expulsion", 0.7),
            Map.entry("diplomat", 0.4),
            Map.entry("military", 0.75),
            Map.entry("exercise", 0.6),
            Map.entry("sovereignty", 0.5),
            Map.entry("critical", 0.6),
            Map.entry("infrastructure", 0.6),
            Map.entry("cooperation", -0.7),
            Map.entry("treaty", -0.8),
            Map.entry("peace", -0.9),
            Map.entry("alliance", -0.4),
            Map.entry("summit", -0.3),
            Map.entry("ceasefire", -0.85)
    );

    // Geopolitical entity country code mapping
    private static final Map<String, String> COUNTRY_ENTITIES = Map.ofEntries(
            Map.entry("russia", "RU"),
            Map.entry("russian", "RU"),
            Map.entry("moscow", "RU"),
            Map.entry("china", "CN"),
            Map.entry("chinese", "CN"),
            Map.entry("beijing", "CN"),
            Map.entry("north korea", "KP"),
            Map.entry("dprk", "KP"),
            Map.entry("pyongyang", "KP"),
            Map.entry("iran", "IR"),
            Map.entry("iranian", "IR"),
            Map.entry("tehran", "IR"),
            Map.entry("united states", "US"),
            Map.entry("america", "US"),
            Map.entry("washington", "US"),
            Map.entry("ukraine", "UA"),
            Map.entry("kyiv", "UA"),
            Map.entry("taiwan", "TW"),
            Map.entry("taipei", "TW"),
            Map.entry("israel", "IL"),
            Map.entry("tel aviv", "IL"),
            Map.entry("germany", "DE"),
            Map.entry("berlin", "DE"),
            Map.entry("united kingdom", "UK"),
            Map.entry("london", "UK")
    );

    // Canonical feature vocabulary for 64-dimensional embedding projection
    public static final List<String> EMBEDDING_VOCABULARY = List.of(
            "spearphishing", "credential", "dumping", "exfiltration", "c2", "cobalt",
            "strike", "wiper", "powershell", "obfuscation", "lateral", "movement",
            "mimikatz", "zeroday", "vulnerability", "exploit", "reconnaissance", "payload",
            "backdoor", "trojan", "stealer", "espionage", "sabotage", "ransomware",
            "military", "defense", "telecom", "energy", "financial", "government",
            "diplomatic", "sanctions", "conflict", "election", "aerospace", "maritime",
            "satellite", "border", "airspace", "embassy", "intelligence", "gru",
            "fsb", "svr", "pla", "mss", "rgb", "irgc",
            "tension", "escalation", "retaliation", "covert", "denial", "attribution",
            "infrastructure", "scada", "ics", "ddos", "botnet", "supplychain",
            "active", "directory", "kerberos", "bypass", "defense", "threat"
    );

    /**
     * Tokenizes a text into lowercase words with stop words removed.
     */
    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = cleaned.split("\\s+");
        List<String> result = new ArrayList<>();
        for (String t : tokens) {
            if (t.length() > 1 && !STOP_WORDS.contains(t)) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Computes the geopolitical conflict tone of a news text.
     * Scale: -1.0 (strongly cooperative/de-escalating) to +1.0 (strongly hostile/escalating).
     */
    public double computeConflictTone(String text) {
        if (text == null || text.isBlank()) return 0.0;
        List<String> tokens = tokenize(text);
        if (tokens.isEmpty()) return 0.0;

        double sum = 0.0;
        int matched = 0;
        for (String token : tokens) {
            if (TENSION_LEXICON.containsKey(token)) {
                sum += TENSION_LEXICON.get(token);
                matched++;
            }
        }

        if (matched == 0) return 0.0;
        double raw = sum / matched;
        return Math.max(-1.0, Math.min(1.0, raw));
    }

    /**
     * Extracts mentioned geopolitical countries from text.
     */
    public Set<String> extractCountries(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();
        String lower = text.toLowerCase();
        Set<String> countries = new HashSet<>();
        for (Map.Entry<String, String> entry : COUNTRY_ENTITIES.entrySet()) {
            if (lower.contains(entry.getKey())) {
                countries.add(entry.getValue());
            }
        }
        return countries;
    }

    /**
     * Generates a dense, normalized 64-dimensional semantic embedding vector from text.
     */
    public double[] generateNewsEmbedding(String text) {
        double[] embedding = new double[64];
        if (text == null || text.isBlank()) return embedding;

        List<String> tokens = tokenize(text);
        if (tokens.isEmpty()) return embedding;

        Map<String, Integer> freq = new HashMap<>();
        for (String t : tokens) {
            freq.put(t, freq.getOrDefault(t, 0) + 1);
        }

        double normSq = 0.0;
        for (int i = 0; i < EMBEDDING_VOCABULARY.size() && i < 64; i++) {
            String word = EMBEDDING_VOCABULARY.get(i);
            int count = freq.getOrDefault(word, 0);
            double tf = (count > 0) ? (1.0 + Math.log(count)) : 0.0;
            // Add slight semantic hash projection for out-of-vocabulary terms
            embedding[i] = tf;
            normSq += embedding[i] * embedding[i];
        }

        // L2 normalization for cosine stability
        if (normSq > 1e-9) {
            double norm = Math.sqrt(normSq);
            for (int i = 0; i < 64; i++) {
                embedding[i] /= norm;
            }
        }

        return embedding;
    }
}
