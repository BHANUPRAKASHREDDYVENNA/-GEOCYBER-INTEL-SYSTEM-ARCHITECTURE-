package org.geocyber.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "gdelt_events")
public class GdeltEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String globalEventId;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(length = 50)
    private String actor1Code;

    @Column(length = 10)
    private String actor1Country;

    @Column(length = 50)
    private String actor2Code;

    @Column(length = 10)
    private String actor2Country;

    @Column(length = 20)
    private String eventCode; // CAMEO event code (e.g., 190, 140, 020)

    private String eventDescription;

    private Double goldsteinScale; // -10.0 (extreme conflict) to +10.0 (high cooperation)

    private Double avgTone; // -100 to +100

    private Integer numMentions;

    @Column(length = 10)
    private String actionGeoCountry;

    private Double actionGeoLat;

    private Double actionGeoLong;

    @Column(columnDefinition = "TEXT")
    private String sourceUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    public GdeltEvent() {}

    public GdeltEvent(String globalEventId, LocalDate eventDate, String actor1Code, String actor1Country,
                      String actor2Code, String actor2Country, String eventCode, String eventDescription,
                      Double goldsteinScale, Double avgTone, Integer numMentions, String actionGeoCountry,
                      Double actionGeoLat, Double actionGeoLong, String sourceUrl) {
        this.globalEventId = globalEventId;
        this.eventDate = eventDate;
        this.actor1Code = actor1Code;
        this.actor1Country = actor1Country;
        this.actor2Code = actor2Code;
        this.actor2Country = actor2Country;
        this.eventCode = eventCode;
        this.eventDescription = eventDescription;
        this.goldsteinScale = goldsteinScale;
        this.avgTone = avgTone;
        this.numMentions = numMentions;
        this.actionGeoCountry = actionGeoCountry;
        this.actionGeoLat = actionGeoLat;
        this.actionGeoLong = actionGeoLong;
        this.sourceUrl = sourceUrl;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGlobalEventId() { return globalEventId; }
    public void setGlobalEventId(String globalEventId) { this.globalEventId = globalEventId; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getActor1Code() { return actor1Code; }
    public void setActor1Code(String actor1Code) { this.actor1Code = actor1Code; }

    public String getActor1Country() { return actor1Country; }
    public void setActor1Country(String actor1Country) { this.actor1Country = actor1Country; }

    public String getActor2Code() { return actor2Code; }
    public void setActor2Code(String actor2Code) { this.actor2Code = actor2Code; }

    public String getActor2Country() { return actor2Country; }
    public void setActor2Country(String actor2Country) { this.actor2Country = actor2Country; }

    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }

    public String getEventDescription() { return eventDescription; }
    public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }

    public Double getGoldsteinScale() { return goldsteinScale; }
    public void setGoldsteinScale(Double goldsteinScale) { this.goldsteinScale = goldsteinScale; }

    public Double getAvgTone() { return avgTone; }
    public void setAvgTone(Double avgTone) { this.avgTone = avgTone; }

    public Integer getNumMentions() { return numMentions; }
    public void setNumMentions(Integer numMentions) { this.numMentions = numMentions; }

    public String getActionGeoCountry() { return actionGeoCountry; }
    public void setActionGeoCountry(String actionGeoCountry) { this.actionGeoCountry = actionGeoCountry; }

    public Double getActionGeoLat() { return actionGeoLat; }
    public void setActionGeoLat(Double actionGeoLat) { this.actionGeoLat = actionGeoLat; }

    public Double getActionGeoLong() { return actionGeoLong; }
    public void setActionGeoLong(Double actionGeoLong) { this.actionGeoLong = actionGeoLong; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
