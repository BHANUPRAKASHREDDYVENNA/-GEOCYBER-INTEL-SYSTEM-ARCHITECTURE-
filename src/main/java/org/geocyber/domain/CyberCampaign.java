package org.geocyber.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cyber_campaigns")
public class CyberCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(length = 100)
    private String attributedActorName;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String targetNations;

    @Column(columnDefinition = "TEXT")
    private String targetSectors;

    @Column(columnDefinition = "TEXT")
    private String geopoliticalObjective;

    @Column(columnDefinition = "TEXT")
    private String observedTtps;

    @Column(columnDefinition = "TEXT")
    private String observedMalware;

    @Column(columnDefinition = "TEXT")
    private String observedIocs;

    private Double gdeltCorrelationScore = 0.0;

    private LocalDateTime createdAt = LocalDateTime.now();

    public CyberCampaign() {}

    public CyberCampaign(String name, String attributedActorName, LocalDate startDate, LocalDate endDate,
                         String targetNations, String targetSectors, String geopoliticalObjective,
                         String observedTtps, String observedMalware, String observedIocs, Double gdeltCorrelationScore) {
        this.name = name;
        this.attributedActorName = attributedActorName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetNations = targetNations;
        this.targetSectors = targetSectors;
        this.geopoliticalObjective = geopoliticalObjective;
        this.observedTtps = observedTtps;
        this.observedMalware = observedMalware;
        this.observedIocs = observedIocs;
        this.gdeltCorrelationScore = gdeltCorrelationScore;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAttributedActorName() { return attributedActorName; }
    public void setAttributedActorName(String attributedActorName) { this.attributedActorName = attributedActorName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getTargetNations() { return targetNations; }
    public void setTargetNations(String targetNations) { this.targetNations = targetNations; }

    public String getTargetSectors() { return targetSectors; }
    public void setTargetSectors(String targetSectors) { this.targetSectors = targetSectors; }

    public String getGeopoliticalObjective() { return geopoliticalObjective; }
    public void setGeopoliticalObjective(String geopoliticalObjective) { this.geopoliticalObjective = geopoliticalObjective; }

    public String getObservedTtps() { return observedTtps; }
    public void setObservedTtps(String observedTtps) { this.observedTtps = observedTtps; }

    public String getObservedMalware() { return observedMalware; }
    public void setObservedMalware(String observedMalware) { this.observedMalware = observedMalware; }

    public String getObservedIocs() { return observedIocs; }
    public void setObservedIocs(String observedIocs) { this.observedIocs = observedIocs; }

    public Double getGdeltCorrelationScore() { return gdeltCorrelationScore; }
    public void setGdeltCorrelationScore(Double gdeltCorrelationScore) { this.gdeltCorrelationScore = gdeltCorrelationScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
