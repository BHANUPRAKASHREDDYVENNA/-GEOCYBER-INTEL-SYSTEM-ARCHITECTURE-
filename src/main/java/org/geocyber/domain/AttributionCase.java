package org.geocyber.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attribution_cases")
public class AttributionCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String caseNumber; // e.g. CASE-2026-0042

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String leadAnalyst;

    @Column(length = 30)
    private String status = "OPEN"; // OPEN, IN_ANALYSIS, ATTRIBUTED, RESOLVED, CLOSED

    private LocalDate incidentDate;

    @Column(length = 10)
    private String incidentTargetCountry;

    @Column(length = 100)
    private String incidentTargetSector;

    @Column(columnDefinition = "TEXT")
    private String observedTtps;

    @Column(columnDefinition = "TEXT")
    private String observedIocs;

    @Column(columnDefinition = "TEXT")
    private String geopoliticalContext;

    @Column(length = 100)
    private String attributedActor;

    private Double calibratedConfidence;

    private Boolean isUnknownActor = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String evidenceBreakdown; // JSON string of supporting/contradicting evidence

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public AttributionCase() {}

    public AttributionCase(String caseNumber, String title, String leadAnalyst, String status,
                           LocalDate incidentDate, String incidentTargetCountry, String incidentTargetSector,
                           String observedTtps, String observedIocs, String geopoliticalContext,
                           String attributedActor, Double calibratedConfidence, Boolean isUnknownActor,
                           String notes, String evidenceBreakdown) {
        this.caseNumber = caseNumber;
        this.title = title;
        this.leadAnalyst = leadAnalyst;
        this.status = status;
        this.incidentDate = incidentDate;
        this.incidentTargetCountry = incidentTargetCountry;
        this.incidentTargetSector = incidentTargetSector;
        this.observedTtps = observedTtps;
        this.observedIocs = observedIocs;
        this.geopoliticalContext = geopoliticalContext;
        this.attributedActor = attributedActor;
        this.calibratedConfidence = calibratedConfidence;
        this.isUnknownActor = isUnknownActor;
        this.notes = notes;
        this.evidenceBreakdown = evidenceBreakdown;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLeadAnalyst() { return leadAnalyst; }
    public void setLeadAnalyst(String leadAnalyst) { this.leadAnalyst = leadAnalyst; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }

    public String getIncidentTargetCountry() { return incidentTargetCountry; }
    public void setIncidentTargetCountry(String incidentTargetCountry) { this.incidentTargetCountry = incidentTargetCountry; }

    public String getIncidentTargetSector() { return incidentTargetSector; }
    public void setIncidentTargetSector(String incidentTargetSector) { this.incidentTargetSector = incidentTargetSector; }

    public String getObservedTtps() { return observedTtps; }
    public void setObservedTtps(String observedTtps) { this.observedTtps = observedTtps; }

    public String getObservedIocs() { return observedIocs; }
    public void setObservedIocs(String observedIocs) { this.observedIocs = observedIocs; }

    public String getGeopoliticalContext() { return geopoliticalContext; }
    public void setGeopoliticalContext(String geopoliticalContext) { this.geopoliticalContext = geopoliticalContext; }

    public String getAttributedActor() { return attributedActor; }
    public void setAttributedActor(String attributedActor) { this.attributedActor = attributedActor; }

    public Double getCalibratedConfidence() { return calibratedConfidence; }
    public void setCalibratedConfidence(Double calibratedConfidence) { this.calibratedConfidence = calibratedConfidence; }

    public Boolean getIsUnknownActor() { return isUnknownActor; }
    public void setIsUnknownActor(Boolean isUnknownActor) { this.isUnknownActor = isUnknownActor; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getEvidenceBreakdown() { return evidenceBreakdown; }
    public void setEvidenceBreakdown(String evidenceBreakdown) { this.evidenceBreakdown = evidenceBreakdown; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
