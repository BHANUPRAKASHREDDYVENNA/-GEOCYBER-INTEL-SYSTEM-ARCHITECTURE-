package org.geocyber.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "apt_actors")
public class AptActor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String country; // e.g. RU, CN, KP, IR, US

    @Column(length = 100)
    private String countryName;

    @Column(columnDefinition = "TEXT")
    private String aliases; // e.g. "Fancy Bear, Sofacy, STRONTIUM, Sednit, Forest Blizzard"

    @Column(length = 50)
    private String motivation; // Espionage, Sabotage, Financial, Disruption

    @Column(columnDefinition = "TEXT")
    private String targetSectors; // Defense, Energy, Government, Telecom, Finance

    @Column(columnDefinition = "TEXT")
    private String targetCountries; // US, UA, DE, UK, PL, TW, KR

    @Column(columnDefinition = "TEXT")
    private String observedTtps; // T1566.001, T1059.001, T1027, T1071.001

    @Column(columnDefinition = "TEXT")
    private String observedMalware; // X-Agent, Zebrocy, Cobalt Strike

    private Double confidenceScore = 0.85;

    @Column(length = 20)
    private String firstSeen;

    @Column(length = 20)
    private String lastSeen;

    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String provenance; // Mandiant, CrowdStrike, CISA, MITRE

    private LocalDateTime createdAt = LocalDateTime.now();

    public AptActor() {}

    public AptActor(String name, String country, String countryName, String aliases, 
                    String motivation, String targetSectors, String targetCountries, 
                    String observedTtps, String observedMalware, Double confidenceScore, 
                    String firstSeen, String lastSeen, Boolean active, String description, String provenance) {
        this.name = name;
        this.country = country;
        this.countryName = countryName;
        this.aliases = aliases;
        this.motivation = motivation;
        this.targetSectors = targetSectors;
        this.targetCountries = targetCountries;
        this.observedTtps = observedTtps;
        this.observedMalware = observedMalware;
        this.confidenceScore = confidenceScore;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
        this.active = active;
        this.description = description;
        this.provenance = provenance;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getAliases() { return aliases; }
    public void setAliases(String aliases) { this.aliases = aliases; }

    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }

    public String getTargetSectors() { return targetSectors; }
    public void setTargetSectors(String targetSectors) { this.targetSectors = targetSectors; }

    public String getTargetCountries() { return targetCountries; }
    public void setTargetCountries(String targetCountries) { this.targetCountries = targetCountries; }

    public String getObservedTtps() { return observedTtps; }
    public void setObservedTtps(String observedTtps) { this.observedTtps = observedTtps; }

    public String getObservedMalware() { return observedMalware; }
    public void setObservedMalware(String observedMalware) { this.observedMalware = observedMalware; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getFirstSeen() { return firstSeen; }
    public void setFirstSeen(String firstSeen) { this.firstSeen = firstSeen; }

    public String getLastSeen() { return lastSeen; }
    public void setLastSeen(String lastSeen) { this.lastSeen = lastSeen; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProvenance() { return provenance; }
    public void setProvenance(String provenance) { this.provenance = provenance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
