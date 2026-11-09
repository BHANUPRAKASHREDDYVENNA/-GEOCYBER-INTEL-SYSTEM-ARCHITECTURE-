package org.geocyber.ai.model;

import org.geocyber.ai.features.MultiModalFeatureVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MultimodalTransformerTest {

    private MultimodalCrossAttentionTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new MultimodalCrossAttentionTransformer();
    }

    @Test
    void testForwardPass() {
        double[] news = new double[64];
        double[] gdelt = new double[6];
        double[] ttps = new double[30];
        double[] iocs = new double[16];
        double[] temp = new double[8];

        news[0] = 1.0;
        gdelt[0] = 0.8;
        ttps[0] = 1.0;
        iocs[0] = 0.5;
        temp[0] = 1.0;

        MultiModalFeatureVector fv = new MultiModalFeatureVector(news, gdelt, ttps, iocs, temp, "APT28", "CAMP-1");

        MultimodalCrossAttentionTransformer.InferenceResult res = transformer.forward(fv, 1.0);

        assertThat(res).isNotNull();
        assertThat(res.probabilities).hasSize(MultimodalCrossAttentionTransformer.ACTOR_CLASSES.size());

        // Probabilities must sum to 1.0
        double sum = 0.0;
        for (double p : res.probabilities) sum += p;
        assertThat(sum).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-4));

        // Cross-attention matrix must be 5x5
        assertThat(res.crossAttentionMatrix).hasDimensions(5, 5);

        // Modality weights must sum to 1.0
        double modSum = 0.0;
        for (double w : res.modalityWeights) modSum += w;
        assertThat(modSum).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-4));
    }

    @Test
    void testTemperatureCalibrationEffect() {
        double[] news = new double[64];
        double[] gdelt = new double[6];
        double[] ttps = new double[30];
        double[] iocs = new double[16];
        double[] temp = new double[8];

        MultiModalFeatureVector fv = new MultiModalFeatureVector(news, gdelt, ttps, iocs, temp, "APT28", "CAMP-1");

        // High temperature (softens distribution, lowering max probability)
        MultimodalCrossAttentionTransformer.InferenceResult soft = transformer.forward(fv, 5.0);
        // Low temperature (sharpens distribution, raising max probability)
        MultimodalCrossAttentionTransformer.InferenceResult sharp = transformer.forward(fv, 0.2);

        assertThat(sharp.confidence).isGreaterThanOrEqualTo(soft.confidence);
    }
}
