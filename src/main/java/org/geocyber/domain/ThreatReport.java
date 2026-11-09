package org.geocyber.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "threat_reports")
public class ThreatReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 100)
    private String publisher; // Mandiant, CrowdStrike, CISA, Kaspersky, ESET

    private LocalDate publicationDate;

    @Column(columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(length = 64)
    private String sha256Hash;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String fullContent;

    @Column(columnDefinition = "TEXT")
    private String extractedActors; // APT28, Fancy Bear

    @Column(columnDefinition = "TEXT")
    private String extractedMalware; // X-Agent, Zebrocy

    @Column(columnDefinition = "TEXT")
    private String extractedTtps; // T1566.001, T1059.001

    private LocalDateTime createdAt = LocalDateTime.now();

    public ThreatReport() {}

    public ThreatReport(String title, String publisher, LocalDate publicationDate, String sourceUrl,
                        String sha256Hash, String summary, String fullContent,
                        String extractedActors, String extractedMalware, String extractedTtps) {
        this.title = title;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.sourceUrl = sourceUrl;
        this.sha256Hash = sha256Hash;
        this.summary = summary;
        this.fullContent = fullContent;
        this.extractedActors = extractedActors;
        this.extractedMalware = extractedMalware;
        this.extractedTtps = extractedTtps;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getFullContent() { return fullContent; }
    public void setFullContent(String fullContent) { this.fullContent = fullContent; }

    public String getExtractedActors() { return extractedActors; }
    public void setExtractedActors(String extractedActors) { this.extractedActors = extractedActors; }

    public String getExtractedMalware() { return extractedMalware; }
    public void setExtractedMalware(String extractedMalware) { this.extractedMalware = extractedMalware; }

    public String getExtractedTtps() { return extractedTtps; }
    public void setExtractedTtps(String extractedTtps) { this.extractedTtps = extractedTtps; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
