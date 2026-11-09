package org.geocyber.ai.model;

import org.ejml.simple.SimpleMatrix;
import org.geocyber.ai.features.MultiModalFeatureVector;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Baseline 2: Single-Modality CTI Transformer (MITRE ATT&CK TTPs only).
 * Target reproduction benchmark accuracy: ~77.4%.
 */
@Component
public class SingleModalityCtiTransformer {

    private SimpleMatrix W_ttp_proj; // 32 x 30
    private SimpleMatrix W_attn;     // 32 x 32
    private SimpleMatrix W_classifier;// 10 x 32
    private SimpleMatrix b_classifier;// 10 x 1
    private boolean isTrained = false;

    public SingleModalityCtiTransformer() {
        Random rand = new Random(1337);
        W_ttp_proj = randomMatrix(32, 30, rand);
        W_attn = randomMatrix(32, 32, rand);
        W_classifier = randomMatrix(MultimodalCrossAttentionTransformer.ACTOR_CLASSES.size(), 32, rand);
        b_classifier = new SimpleMatrix(MultimodalCrossAttentionTransformer.ACTOR_CLASSES.size(), 1);
        b_classifier.zero();
    }

    private SimpleMatrix randomMatrix(int r, int c, Random rand) {
        SimpleMatrix m = new SimpleMatrix(r, c);
        double std = Math.sqrt(2.0 / (r + c));
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) m.set(i, j, rand.nextGaussian() * std);
        }
        return m;
    }

    public Map<String, Double> predictProbabilities(MultiModalFeatureVector fv) {
        double[] ttps = fv.getTtpFeatures();
        SimpleMatrix x = new SimpleMatrix(30, 1);
        for (int i = 0; i < 30; i++) x.set(i, 0, ttps[i]);

        SimpleMatrix h = gelu(W_ttp_proj.mult(x)); // 32 x 1
        SimpleMatrix attnContext = gelu(W_attn.mult(h)); // Self-attention projection
        SimpleMatrix logits = W_classifier.mult(attnContext).plus(b_classifier);

        double[] raw = new double[MultimodalCrossAttentionTransformer.ACTOR_CLASSES.size()];
        double maxL = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < raw.length; i++) {
            raw[i] = logits.get(i, 0);
            maxL = Math.max(maxL, raw[i]);
        }

        double sumExp = 0.0;
        double[] p = new double[raw.length];
        for (int i = 0; i < raw.length; i++) {
            p[i] = Math.exp(raw[i] - maxL);
            sumExp += p[i];
        }

        Map<String, Double> result = new HashMap<>();
        for (int i = 0; i < raw.length; i++) {
            result.put(MultimodalCrossAttentionTransformer.ACTOR_CLASSES.get(i), p[i] / sumExp);
        }
        return result;
    }

    private SimpleMatrix gelu(SimpleMatrix m) {
        SimpleMatrix res = new SimpleMatrix(m.getNumRows(), m.getNumCols());
        for (int r = 0; r < m.getNumRows(); r++) {
            for (int c = 0; c < m.getNumCols(); c++) {
                double val = m.get(r, c);
                res.set(r, c, 0.5 * val * (1.0 + Math.tanh(Math.sqrt(2.0 / Math.PI) * (val + 0.044715 * Math.pow(val, 3)))));
            }
        }
        return res;
    }

    public void setTrained(boolean trained) { this.isTrained = trained; }
    public boolean isTrained() { return isTrained; }
}
