package org.geocyber.benchmark;

import org.geocyber.ai.model.MultimodalCrossAttentionTransformer;
import org.geocyber.ai.model.NaiveBayesAttributionModel;
import org.geocyber.ai.model.SingleModalityCtiTransformer;
import org.geocyber.repository.BenchmarkRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BenchmarkServiceTest {

    private BenchmarkService benchmarkService;

    @BeforeEach
    void setUp() {
        SyntheticDatasetGenerator generator = new SyntheticDatasetGenerator();
        MultimodalCrossAttentionTransformer transformer = new MultimodalCrossAttentionTransformer();
        NaiveBayesAttributionModel naiveBayes = new NaiveBayesAttributionModel();
        SingleModalityCtiTransformer ctiTransformer = new SingleModalityCtiTransformer();
        BenchmarkRecordRepository recordRepository = Mockito.mock(BenchmarkRecordRepository.class);

        benchmarkService = new BenchmarkService(generator, transformer, naiveBayes, ctiTransformer, recordRepository);
    }

    @Test
    void testRunComprehensiveBenchmarkSuite() {
        List<BenchmarkResult> results = benchmarkService.runComprehensiveBenchmarkSuite();

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(7); // Naive Bayes, CTI Transformer, Full Proposed, 4 Ablations

        BenchmarkResult nb = results.stream().filter(r -> r.getModelName().contains("Naive Bayes")).findFirst().orElseThrow();
        BenchmarkResult cti = results.stream().filter(r -> r.getModelName().contains("CTI Transformer")).findFirst().orElseThrow();
        BenchmarkResult proposed = results.stream().filter(r -> r.getModelName().contains("Proposed")).findFirst().orElseThrow();

        // Check reproduction targets
        assertThat(nb.getAccuracy()).isCloseTo(0.582, org.assertj.core.data.Offset.offset(0.02));
        assertThat(cti.getAccuracy()).isCloseTo(0.774, org.assertj.core.data.Offset.offset(0.02));
        assertThat(proposed.getAccuracy()).isCloseTo(0.895, org.assertj.core.data.Offset.offset(0.01));
        assertThat(proposed.getTop3Accuracy()).isCloseTo(0.972, org.assertj.core.data.Offset.offset(0.01));
        assertThat(proposed.getMacroF1()).isCloseTo(0.891, org.assertj.core.data.Offset.offset(0.01));
    }
}
