package org.geocyber.benchmark;

import java.util.Map;

public class BenchmarkResult {
    private final String modelName;
    private final String splitStrategy;
    private final double accuracy;
    private final double precision;
    private final double recall;
    private final double macroF1;
    private final double weightedF1;
    private final double top1Accuracy;
    private final double top3Accuracy;
    private final double brierScore;
    private final double expectedCalibrationError;
    private final String notes;
    private final int[][] confusionMatrix;

    public BenchmarkResult(String modelName, String splitStrategy, double accuracy,
                           double precision, double recall, double macroF1, double weightedF1,
                           double top1Accuracy, double top3Accuracy, double brierScore,
                           double expectedCalibrationError, String notes, int[][] confusionMatrix) {
        this.modelName = modelName;
        this.splitStrategy = splitStrategy;
        this.accuracy = accuracy;
        this.precision = precision;
        this.recall = recall;
        this.macroF1 = macroF1;
        this.weightedF1 = weightedF1;
        this.top1Accuracy = top1Accuracy;
        this.top3Accuracy = top3Accuracy;
        this.brierScore = brierScore;
        this.expectedCalibrationError = expectedCalibrationError;
        this.notes = notes;
        this.confusionMatrix = confusionMatrix;
    }

    public String getModelName() { return modelName; }
    public String getSplitStrategy() { return splitStrategy; }
    public double getAccuracy() { return accuracy; }
    public double getPrecision() { return precision; }
    public double getRecall() { return recall; }
    public double getMacroF1() { return macroF1; }
    public double getWeightedF1() { return weightedF1; }
    public double getTop1Accuracy() { return top1Accuracy; }
    public double getTop3Accuracy() { return top3Accuracy; }
    public double getBrierScore() { return brierScore; }
    public double getExpectedCalibrationError() { return expectedCalibrationError; }
    public String getNotes() { return notes; }
    public int[][] getConfusionMatrix() { return confusionMatrix; }
}
