package org.geocyber.ai.model;

import org.geocyber.ai.features.MultiModalFeatureVector;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Baseline 1: Multi-variate Naive Bayes Classifier.
 * Target reproduction benchmark accuracy: ~58.2%.
 */
@Component
public class NaiveBayesAttributionModel {

    private final Map<String, Double> classPriors = new HashMap<>();
    private final Map<String, double[]> classMeans = new HashMap<>();
    private final Map<String, double[]> classVars = new HashMap<>();
    private boolean isTrained = false;

    public void train(List<MultiModalFeatureVector> dataset) {
        if (dataset == null || dataset.isEmpty()) return;

        Map<String, List<double[]>> grouped = new HashMap<>();
        for (MultiModalFeatureVector fv : dataset) {
            String actor = fv.getGroundTruthActor();
            if (actor != null && !actor.isBlank()) {
                grouped.computeIfAbsent(actor, k -> new ArrayList<>()).add(fv.flatten());
            }
        }

        int totalSamples = dataset.size();
        int featureDim = dataset.getFirst().flatten().length;

        for (Map.Entry<String, List<double[]>> entry : grouped.entrySet()) {
            String actor = entry.getKey();
            List<double[]> samples = entry.getValue();
            classPriors.put(actor, (double) samples.size() / totalSamples);

            double[] mean = new double[featureDim];
            for (double[] s : samples) {
                for (int i = 0; i < featureDim; i++) mean[i] += s[i];
            }
            for (int i = 0; i < featureDim; i++) mean[i] /= samples.size();
            classMeans.put(actor, mean);

            double[] var = new double[featureDim];
            for (double[] s : samples) {
                for (int i = 0; i < featureDim; i++) {
                    double d = s[i] - mean[i];
                    var[i] += d * d;
                }
            }
            for (int i = 0; i < featureDim; i++) {
                var[i] = (var[i] / samples.size()) + 1e-4; // Smoothing epsilon
            }
            classVars.put(actor, var);
        }

        isTrained = true;
    }

    public Map<String, Double> predictProbabilities(MultiModalFeatureVector fv) {
        if (!isTrained || classPriors.isEmpty()) {
            Map<String, Double> uniform = new HashMap<>();
            for (String a : MultimodalCrossAttentionTransformer.ACTOR_CLASSES) {
                uniform.put(a, 1.0 / MultimodalCrossAttentionTransformer.ACTOR_CLASSES.size());
            }
            return uniform;
        }

        double[] x = fv.flatten();
        Map<String, Double> logPosteriors = new HashMap<>();
        double maxLog = Double.NEGATIVE_INFINITY;

        for (Map.Entry<String, Double> entry : classPriors.entrySet()) {
            String actor = entry.getKey();
            double prior = entry.getValue();
            double[] mean = classMeans.get(actor);
            double[] var = classVars.get(actor);

            double logLikelihood = Math.log(prior);
            for (int i = 0; i < x.length; i++) {
                double diff = x[i] - mean[i];
                double v = var[i];
                logLikelihood -= 0.5 * (Math.log(2 * Math.PI * v) + (diff * diff) / v);
            }

            logPosteriors.put(actor, logLikelihood);
            maxLog = Math.max(maxLog, logLikelihood);
        }

        // Softmax normalization
        double sumExp = 0.0;
        Map<String, Double> probs = new HashMap<>();
        for (Map.Entry<String, Double> entry : logPosteriors.entrySet()) {
            double p = Math.exp(entry.getValue() - maxLog);
            probs.put(entry.getKey(), p);
            sumExp += p;
        }

        for (Map.Entry<String, Double> entry : probs.entrySet()) {
            probs.put(entry.getKey(), entry.getValue() / sumExp);
        }

        return probs;
    }

    public boolean isTrained() { return isTrained; }
}
