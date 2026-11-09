package org.geocyber.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "indicators_of_compromise")
public class IndicatorOfCompromise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IocType iocType;

    @Column(nullable = false, length = 255)
    private String rawValue;

    @Column(nullable = false, length = 255)
    private String normalizedValue;

    @Column(nullable = false, length = 255)
    private String defangedValue;

    private Double reputationScore = 0.5; // 0.0 (benign) to 1.0 (confirmed malicious)

    @Column(length = 20)
    private String confidenceLevel = "HIGH"; // LOW, MEDIUM, HIGH

    @Column(length = 100)
    private String threatFeed; // e.g. APTnotes, Mandiant CTI, AlienVault OTX, CISA

    @Column(columnDefinition = "TEXT")
    private String associatedActors; // APT28, Turla

    @Column(columnDefinition = "TEXT")
    private String associatedCampaigns; // SolarWinds, Ghostwriter

    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    private LocalDateTime createdAt = LocalDateTime.now();

    public IndicatorOfCompromise() {}

    public IndicatorOfCompromise(IocType iocType, String rawValue, String normalizedValue, String defangedValue,
                                 Double reputationScore, String confidenceLevel, String threatFeed,
                                 String associatedActors, String associatedCampaigns) {
        this.iocType = iocType;
        this.rawValue = rawValue;
        this.normalizedValue = normalizedValue;
        this.defangedValue = defangedValue;
        this.reputationScore = reputationScore;
        this.confidenceLevel = confidenceLevel;
        this.threatFeed = threatFeed;
        this.associatedActors = associatedActors;
        this.associatedCampaigns = associatedCampaigns;
        this.firstSeen = LocalDateTime.now().minusDays(30);
        this.lastSeen = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public IocType getIocType() { return iocType; }
    public void setIocType(IocType iocType) { this.iocType = iocType; }

    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }

    public String getNormalizedValue() { return normalizedValue; }
    public void setNormalizedValue(String normalizedValue) { this.normalizedValue = normalizedValue; }

    public String getDefangedValue() { return defangedValue; }
    public void setDefangedValue(String defangedValue) { this.defangedValue = defangedValue; }

    public Double getReputationScore() { return reputationScore; }
    public void setReputationScore(Double reputationScore) { this.reputationScore = reputationScore; }

    public String getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(String confidenceLevel) { this.confidenceLevel = confidenceLevel; }

    public String getThreatFeed() { return threatFeed; }
    public void setThreatFeed(String threatFeed) { this.threatFeed = threatFeed; }

    public String getAssociatedActors() { return associatedActors; }
    public void setAssociatedActors(String associatedActors) { this.associatedActors = associatedActors; }

    public String getAssociatedCampaigns() { return associatedCampaigns; }
    public void setAssociatedCampaigns(String associatedCampaigns) { this.associatedCampaigns = associatedCampaigns; }

    public LocalDateTime getFirstSeen() { return firstSeen; }
    public void setFirstSeen(LocalDateTime firstSeen) { this.firstSeen = firstSeen; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
