package org.geocyber.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mitre_techniques")
public class MitreAttackTechnique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String techniqueId; // e.g. T1566.001

    @Column(nullable = false, length = 200)
    private String name; // Spearphishing Attachment

    @Column(nullable = false, length = 100)
    private String tactic; // Initial Access, Execution, Persistence...

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String platforms; // Windows, Linux, macOS

    @Column(columnDefinition = "TEXT")
    private String detection;

    private LocalDateTime createdAt = LocalDateTime.now();

    public MitreAttackTechnique() {}

    public MitreAttackTechnique(String techniqueId, String name, String tactic, String description, String platforms, String detection) {
        this.techniqueId = techniqueId;
        this.name = name;
        this.tactic = tactic;
        this.description = description;
        this.platforms = platforms;
        this.detection = detection;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTechniqueId() { return techniqueId; }
    public void setTechniqueId(String techniqueId) { this.techniqueId = techniqueId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTactic() { return tactic; }
    public void setTactic(String tactic) { this.tactic = tactic; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPlatforms() { return platforms; }
    public void setPlatforms(String platforms) { this.platforms = platforms; }

    public String getDetection() { return detection; }
    public void setDetection(String detection) { this.detection = detection; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
