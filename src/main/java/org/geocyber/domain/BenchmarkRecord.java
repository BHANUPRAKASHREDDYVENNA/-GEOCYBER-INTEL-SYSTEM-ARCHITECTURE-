package org.geocyber.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "benchmark_records")
public class BenchmarkRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime runTimestamp = LocalDateTime.now();

    @Column(nullable = false, length = 100)
    private String modelName; // Naive Bayes, CTI Transformer, Multimodal Cross-Attention

    @Column(nullable = false, length = 100)
    private String splitStrategy; // 5-Fold Campaign-Grouped, Temporal Split

    private Double accuracy;
    private Double precisionScore;
    private Double recallScore;
    private Double macroF1;
    private Double weightedF1;
    private Double top1Accuracy;
    private Double top3Accuracy;
    private Double brierScore;
    private Double expectedCalibrationError;

    @Column(length = 255)
    private String ablationNotes;

    public BenchmarkRecord() {}

    public BenchmarkRecord(String modelName, String splitStrategy, Double accuracy, Double precisionScore,
                           Double recallScore, Double macroF1, Double weightedF1, Double top1Accuracy,
                           Double top3Accuracy, Double brierScore, Double expectedCalibrationError, String ablationNotes) {
        this.modelName = modelName;
        this.splitStrategy = splitStrategy;
        this.accuracy = accuracy;
        this.precisionScore = precisionScore;
        this.recallScore = recallScore;
        this.macroF1 = macroF1;
        this.weightedF1 = weightedF1;
        this.top1Accuracy = top1Accuracy;
        this.top3Accuracy = top3Accuracy;
        this.brierScore = brierScore;
        this.expectedCalibrationError = expectedCalibrationError;
        this.ablationNotes = ablationNotes;
        this.runTimestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getRunTimestamp() { return runTimestamp; }
    public void setRunTimestamp(LocalDateTime runTimestamp) { this.runTimestamp = runTimestamp; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getSplitStrategy() { return splitStrategy; }
    public void setSplitStrategy(String splitStrategy) { this.splitStrategy = splitStrategy; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

    public Double getPrecisionScore() { return precisionScore; }
    public void setPrecisionScore(Double precisionScore) { this.precisionScore = precisionScore; }

    public Double getRecallScore() { return recallScore; }
    public void setRecallScore(Double recallScore) { this.recallScore = recallScore; }

    public Double getMacroF1() { return macroF1; }
    public void setMacroF1(Double macroF1) { this.macroF1 = macroF1; }

    public Double getWeightedF1() { return weightedF1; }
    public void setWeightedF1(Double weightedF1) { this.weightedF1 = weightedF1; }

    public Double getTop1Accuracy() { return top1Accuracy; }
    public void setTop1Accuracy(Double top1Accuracy) { this.top1Accuracy = top1Accuracy; }

    public Double getTop3Accuracy() { return top3Accuracy; }
    public void setTop3Accuracy(Double top3Accuracy) { this.top3Accuracy = top3Accuracy; }

    public Double getBrierScore() { return brierScore; }
    public void setBrierScore(Double brierScore) { this.brierScore = brierScore; }

    public Double getExpectedCalibrationError() { return expectedCalibrationError; }
    public void setExpectedCalibrationError(Double expectedCalibrationError) { this.expectedCalibrationError = expectedCalibrationError; }

    public String getAblationNotes() { return ablationNotes; }
    public void setAblationNotes(String ablationNotes) { this.ablationNotes = ablationNotes; }
}
