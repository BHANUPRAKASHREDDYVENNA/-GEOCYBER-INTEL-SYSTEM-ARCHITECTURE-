package org.geocyber.nlp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NlpAnalysisServiceTest {

    private NlpAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new NlpAnalysisService();
    }

    @Test
    void testTokenizationAndStopWords() {
        String text = "The Russian GRU deployed a destructive wiper against Ukraine.";
        List<String> tokens = service.tokenize(text);

        assertThat(tokens).contains("russian", "gru", "deployed", "destructive", "wiper", "ukraine");
        assertThat(tokens).doesNotContain("the", "a");
    }

    @Test
    void testConflictToneScoring() {
        String conflictNews = "Severe missile strike and military invasion escalation causing war and cyberattack.";
        double conflictScore = service.computeConflictTone(conflictNews);
        assertThat(conflictScore).isGreaterThan(0.5);

        String peaceNews = "Diplomatic peace treaty signed during bilateral cooperation summit.";
        double peaceScore = service.computeConflictTone(peaceNews);
        assertThat(peaceScore).isLessThan(0.0);
    }

    @Test
    void testCountryEntityExtraction() {
        String text = "Intrusions observed originating from Moscow and targeting Taiwan and Ukraine critical infrastructure.";
        Set<String> countries = service.extractCountries(text);

        assertThat(countries).contains("RU", "TW", "UA");
    }

    @Test
    void testNewsEmbeddingGeneration() {
        String text = "Spearphishing campaign targeting energy infrastructure with credential dumping and cobalt strike.";
        double[] embedding = service.generateNewsEmbedding(text);

        assertThat(embedding).hasSize(64);

        // Verify L2 norm equals 1.0 (or 0 for empty)
        double normSq = 0.0;
        for (double v : embedding) normSq += v * v;
        assertThat(Math.sqrt(normSq)).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-4));
    }
}
