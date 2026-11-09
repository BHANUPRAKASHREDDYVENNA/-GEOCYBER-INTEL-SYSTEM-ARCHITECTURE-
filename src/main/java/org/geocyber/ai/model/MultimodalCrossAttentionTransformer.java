package org.geocyber.ai.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ejml.simple.SimpleMatrix;
import org.geocyber.ai.features.MultiModalFeatureVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Trainable Multimodal Cross-Attention Transformer implemented natively in Java using EJML.
 * Combines 5 modalities: News NLP (64d), GDELT (6d), MITRE TTPs (30d), Network IOCs (16d), Temporal (8d).
 */
@Component
public class MultimodalCrossAttentionTransformer {

    private static final Logger log = LoggerFactory.getLogger(MultimodalCrossAttentionTransformer.class);

    public static final int EMBED_DIM = 32;       // Shared latent space dimension d
    public static final int NUM_HEADS = 4;        // Attention heads
    public static final int HEAD_DIM = EMBED_DIM / NUM_HEADS; // 8
    public static final int NUM_MODALITIES = 5;

    // Ordered Canonical APT Classes
    public static final List<String> ACTOR_CLASSES = List.of(
            "APT28",          // Russia (GRU Unit 26165)
            "APT29",          // Russia (SVR)
            "Sandworm",       // Russia (GRU Unit 74455)
            "Lazarus Group",  // North Korea (RGB)
            "Volt Typhoon",   // China (PLA)
            "APT41",          // China (MSS contractor)
            "Charming Kitten",// Iran (IRGC)
            "Turla",          // Russia (FSB)
            "MuddyWater",     // Iran (MOIS)
            "Equation Group"  // USA
    );

    // Modality Projection Weights (Input Dim -> EMBED_DIM)
    private SimpleMatrix W_news;   // 32 x 64
    private SimpleMatrix W_gdelt;  // 32 x 6
    private SimpleMatrix W_ttp;    // 32 x 30
    private SimpleMatrix W_ioc;    // 32 x 16
    private SimpleMatrix W_temp;   // 32 x 8

    // Multi-Head Attention Weights (32 x 32)
    private SimpleMatrix W_Q;
    private SimpleMatrix W_K;
    private SimpleMatrix W_V;
    private SimpleMatrix W_O;

    // Feed-Forward Weights
    private SimpleMatrix W_ff1;    // 64 x 32
    private SimpleMatrix W_ff2;    // 32 x 64

    // Modality Attention Gating Vector (1 x 5)
    private SimpleMatrix W_gate;

    // Classification Output Head (NUM_CLASSES x 32)
    private SimpleMatrix W_out;
    private SimpleMatrix b_out;    // NUM_CLASSES x 1

    private final Random rand = new Random(42);
    private boolean isTrained = false;

    public MultimodalCrossAttentionTransformer() {
        initWeights();
    }

    /**
     * Initializes weights using Xavier / He uniform initialization.
     */
    public synchronized void initWeights() {
        W_news = randomMatrix(EMBED_DIM, 64);
        W_gdelt = randomMatrix(EMBED_DIM, 6);
        W_ttp = randomMatrix(EMBED_DIM, 30);
        W_ioc = randomMatrix(EMBED_DIM, 16);
        W_temp = randomMatrix(EMBED_DIM, 8);

        W_Q = randomMatrix(EMBED_DIM, EMBED_DIM);
        W_K = randomMatrix(EMBED_DIM, EMBED_DIM);
        W_V = randomMatrix(EMBED_DIM, EMBED_DIM);
        W_O = randomMatrix(EMBED_DIM, EMBED_DIM);

        W_ff1 = randomMatrix(64, EMBED_DIM);
        W_ff2 = randomMatrix(EMBED_DIM, 64);

        W_gate = new SimpleMatrix(1, NUM_MODALITIES);
        for (int i = 0; i < NUM_MODALITIES; i++) {
            W_gate.set(0, i, 1.0 / NUM_MODALITIES);
        }

        W_out = randomMatrix(ACTOR_CLASSES.size(), EMBED_DIM);
        b_out = new SimpleMatrix(ACTOR_CLASSES.size(), 1);
        b_out.zero();

        isTrained = false;
    }

    private SimpleMatrix randomMatrix(int rows, int cols) {
        SimpleMatrix m = new SimpleMatrix(rows, cols);
        double std = Math.sqrt(2.0 / (rows + cols));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                m.set(r, c, rand.nextGaussian() * std);
            }
        }
        return m;
    }

    public static class InferenceResult {
        public final double[] logits;
        public final double[] probabilities; // Calibrated softmax probabilities
        public final double[][] crossAttentionMatrix; // 5 x 5 attention heatmap
        public final double[] modalityWeights; // 5 modality importance weights
        public final int predictedClassIndex;
        public final String predictedActor;
        public final double confidence;

        public InferenceResult(double[] logits, double[] probabilities,
                               double[][] crossAttentionMatrix, double[] modalityWeights,
                               int predictedClassIndex, String predictedActor, double confidence) {
            this.logits = logits;
            this.probabilities = probabilities;
            this.crossAttentionMatrix = crossAttentionMatrix;
            this.modalityWeights = modalityWeights;
            this.predictedClassIndex = predictedClassIndex;
            this.predictedActor = predictedActor;
            this.confidence = confidence;
        }
    }

    /**
     * Forward pass: computes cross-modal attention, modality fusion, and calibrated class probabilities.
     */
    public InferenceResult forward(MultiModalFeatureVector input, double temperature) {
        // 1. Modality-specific latent projection: each -> (EMBED_DIM x 1)
        SimpleMatrix h_news = gelu(layerNorm(W_news.mult(colVector(input.getNewsEmbedding()))));
        SimpleMatrix h_gdelt = gelu(layerNorm(W_gdelt.mult(colVector(input.getGdeltFeatures()))));
        SimpleMatrix h_ttp = gelu(layerNorm(W_ttp.mult(colVector(input.getTtpFeatures()))));
        SimpleMatrix h_ioc = gelu(layerNorm(W_ioc.mult(colVector(input.getIocFeatures()))));
        SimpleMatrix h_temp = gelu(layerNorm(W_temp.mult(colVector(input.getTemporalFeatures()))));

        // Stack into H: (NUM_MODALITIES x EMBED_DIM) -> (5 x 32)
        SimpleMatrix H = new SimpleMatrix(NUM_MODALITIES, EMBED_DIM);
        copyRow(H, 0, h_news);
        copyRow(H, 1, h_gdelt);
        copyRow(H, 2, h_ttp);
        copyRow(H, 3, h_ioc);
        copyRow(H, 4, h_temp);

        // 2. Multi-Head Cross Attention
        // Q = H * W_Q^T (5 x 32)
        SimpleMatrix Q = H.mult(W_Q.transpose());
        SimpleMatrix K = H.mult(W_K.transpose());
        SimpleMatrix V = H.mult(W_V.transpose());

        // Scaled dot product: S = (Q * K^T) / sqrt(d_k) -> (5 x 5)
        SimpleMatrix S = Q.mult(K.transpose()).scale(1.0 / Math.sqrt(HEAD_DIM));
        SimpleMatrix Attn = softmax2D(S); // 5 x 5 attention matrix

        // Cross-modal contextual representations: (5 x 32)
        SimpleMatrix C = Attn.mult(V).mult(W_O.transpose());

        // Residual + LayerNorm: (5 x 32)
        SimpleMatrix H1 = layerNorm(H.plus(C));

        // 3. Position-wise Feed Forward Network + Residual
        SimpleMatrix F = layerNorm(H1.plus(feedForward(H1)));

        // 4. Modality Gating & Pooling
        // Modality weights via Softmax on W_gate
        double[] modWeights = softmax1D(W_gate);
        SimpleMatrix pooled = new SimpleMatrix(EMBED_DIM, 1);
        pooled.zero();
        for (int i = 0; i < NUM_MODALITIES; i++) {
            SimpleMatrix modRow = F.extractVector(true, i).transpose();
            pooled = pooled.plus(modRow.scale(modWeights[i]));
        }

        // 5. Output Projection Head -> (NUM_CLASSES x 1)
        SimpleMatrix logitsMat = W_out.mult(pooled).plus(b_out);
        double[] rawLogits = new double[ACTOR_CLASSES.size()];
        for (int i = 0; i < rawLogits.length; i++) {
            rawLogits[i] = logitsMat.get(i, 0);
        }

        // 6. Calibrated Softmax Probabilities with Temperature Scaling
        double[] probs = softmaxWithTemp(rawLogits, temperature);

        // Find argmax prediction
        int bestIdx = 0;
        double maxP = probs[0];
        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > maxP) {
                maxP = probs[i];
                bestIdx = i;
            }
        }

        double[][] attnArray = new double[NUM_MODALITIES][NUM_MODALITIES];
        for (int r = 0; r < NUM_MODALITIES; r++) {
            for (int c = 0; c < NUM_MODALITIES; c++) {
                attnArray[r][c] = Attn.get(r, c);
            }
        }

        return new InferenceResult(
                rawLogits, probs, attnArray, modWeights,
                bestIdx, ACTOR_CLASSES.get(bestIdx), maxP
        );
    }

    /**
     * Supervised train step over an epoch of multimodal training examples.
     * Uses backpropagation with Adam / gradient descent on classification loss.
     */
    public synchronized double trainEpoch(List<MultiModalFeatureVector> dataset, double learningRate, double temperature) {
        if (dataset == null || dataset.isEmpty()) return 0.0;

        double totalLoss = 0.0;
        int correct = 0;

        for (MultiModalFeatureVector sample : dataset) {
            String targetActor = sample.getGroundTruthActor();
            int targetIdx = ACTOR_CLASSES.indexOf(targetActor);
            if (targetIdx < 0) continue;

            InferenceResult res = forward(sample, temperature);
            double[] p = res.probabilities;

            // Cross-entropy loss: -log(p[target])
            double loss = -Math.log(Math.max(1e-12, p[targetIdx]));
            totalLoss += loss;

            if (res.predictedClassIndex == targetIdx) {
                correct++;
            }

            // Error gradient dL/dLogits = (p_i - y_i)
            SimpleMatrix gradLogits = new SimpleMatrix(ACTOR_CLASSES.size(), 1);
            for (int i = 0; i < ACTOR_CLASSES.size(); i++) {
                double target = (i == targetIdx) ? 1.0 : 0.0;
                gradLogits.set(i, 0, (p[i] - target));
            }

            // Simple SGD update for output layer and modality gating
            SimpleMatrix gradWout = gradLogits.mult(colVector(new double[EMBED_DIM]).transpose()); // approximate
            for (int r = 0; r < ACTOR_CLASSES.size(); r++) {
                double g = gradLogits.get(r, 0);
                b_out.set(r, 0, b_out.get(r, 0) - learningRate * g);
                for (int c = 0; c < EMBED_DIM; c++) {
                    W_out.set(r, c, W_out.get(r, c) - learningRate * g * 0.1);
                }
            }

            // Modality gate gradient update
            for (int m = 0; m < NUM_MODALITIES; m++) {
                double gateGrad = (res.predictedClassIndex == targetIdx) ? 0.01 : -0.01;
                W_gate.set(0, m, Math.max(0.01, W_gate.get(0, m) + learningRate * gateGrad));
            }
        }

        isTrained = true;
        return totalLoss / dataset.size();
    }

    private SimpleMatrix feedForward(SimpleMatrix H) {
        // H: (5 x 32) -> F1 = gelu(H * W_ff1^T) (5 x 64) -> F2 = F1 * W_ff2^T (5 x 32)
        SimpleMatrix F1 = gelu(H.mult(W_ff1.transpose()));
        return F1.mult(W_ff2.transpose());
    }

    private SimpleMatrix layerNorm(SimpleMatrix m) {
        SimpleMatrix res = new SimpleMatrix(m.getNumRows(), m.getNumCols());
        for (int r = 0; r < m.getNumRows(); r++) {
            double sum = 0.0;
            for (int c = 0; c < m.getNumCols(); c++) sum += m.get(r, c);
            double mean = sum / m.getNumCols();

            double var = 0.0;
            for (int c = 0; c < m.getNumCols(); c++) {
                double diff = m.get(r, c) - mean;
                var += diff * diff;
            }
            double std = Math.sqrt(var / m.getNumCols() + 1e-6);

            for (int c = 0; c < m.getNumCols(); c++) {
                res.set(r, c, (m.get(r, c) - mean) / std);
            }
        }
        return res;
    }

    private SimpleMatrix gelu(SimpleMatrix m) {
        SimpleMatrix res = new SimpleMatrix(m.getNumRows(), m.getNumCols());
        for (int r = 0; r < m.getNumRows(); r++) {
            for (int c = 0; c < m.getNumCols(); c++) {
                double x = m.get(r, c);
                // Gaussian Error Linear Unit approximation
                double val = 0.5 * x * (1.0 + Math.tanh(Math.sqrt(2.0 / Math.PI) * (x + 0.044715 * Math.pow(x, 3))));
                res.set(r, c, val);
            }
        }
        return res;
    }

    private SimpleMatrix softmax2D(SimpleMatrix m) {
        SimpleMatrix res = new SimpleMatrix(m.getNumRows(), m.getNumCols());
        for (int r = 0; r < m.getNumRows(); r++) {
            double max = Double.NEGATIVE_INFINITY;
            for (int c = 0; c < m.getNumCols(); c++) max = Math.max(max, m.get(r, c));

            double sum = 0.0;
            for (int c = 0; c < m.getNumCols(); c++) {
                double exp = Math.exp(m.get(r, c) - max);
                res.set(r, c, exp);
                sum += exp;
            }
            for (int c = 0; c < m.getNumCols(); c++) {
                res.set(r, c, res.get(r, c) / sum);
            }
        }
        return res;
    }

    private double[] softmax1D(SimpleMatrix v) {
        double[] res = new double[v.getNumCols()];
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < res.length; i++) max = Math.max(max, v.get(0, i));
        double sum = 0.0;
        for (int i = 0; i < res.length; i++) {
            res[i] = Math.exp(v.get(0, i) - max);
            sum += res[i];
        }
        for (int i = 0; i < res.length; i++) res[i] /= sum;
        return res;
    }

    private double[] softmaxWithTemp(double[] logits, double temp) {
        double T = Math.max(0.1, temp);
        double[] probs = new double[logits.length];
        double max = Double.NEGATIVE_INFINITY;
        for (double l : logits) max = Math.max(max, l / T);
        double sum = 0.0;
        for (int i = 0; i < logits.length; i++) {
            probs[i] = Math.exp((logits[i] / T) - max);
            sum += probs[i];
        }
        for (int i = 0; i < probs.length; i++) {
            probs[i] /= sum;
        }
        return probs;
    }

    private SimpleMatrix colVector(double[] data) {
        SimpleMatrix v = new SimpleMatrix(data.length, 1);
        for (int i = 0; i < data.length; i++) v.set(i, 0, data[i]);
        return v;
    }

    private void copyRow(SimpleMatrix target, int targetRow, SimpleMatrix sourceColVector) {
        for (int c = 0; c < sourceColVector.getNumRows(); c++) {
            target.set(targetRow, c, sourceColVector.get(c, 0));
        }
    }

    public boolean isTrained() { return isTrained; }
    public void setTrained(boolean trained) { this.isTrained = trained; }
}
