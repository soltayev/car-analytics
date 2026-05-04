package kz.dissertation.caranalytics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fault_code_dictionary")
public class FaultCodeDictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 120)
    private String systemName;

    @Column(nullable = false, length = 120)
    private String subsystem;

    @Column(nullable = false)
    private Boolean manufacturerSpecific;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeverityLevel defaultSeverity;

    @Column(nullable = false, length = 2000)
    private String possibleCauses;

    @Column(nullable = false, length = 2000)
    private String recommendedActions;

    @Column(nullable = false)
    private Boolean drivableAllowed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    public Boolean getManufacturerSpecific() {
        return manufacturerSpecific;
    }

    public void setManufacturerSpecific(Boolean manufacturerSpecific) {
        this.manufacturerSpecific = manufacturerSpecific;
    }

    public SeverityLevel getDefaultSeverity() {
        return defaultSeverity;
    }

    public void setDefaultSeverity(SeverityLevel defaultSeverity) {
        this.defaultSeverity = defaultSeverity;
    }

    public String getPossibleCauses() {
        return possibleCauses;
    }

    public void setPossibleCauses(String possibleCauses) {
        this.possibleCauses = possibleCauses;
    }

    public String getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(String recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public Boolean getDrivableAllowed() {
        return drivableAllowed;
    }

    public void setDrivableAllowed(Boolean drivableAllowed) {
        this.drivableAllowed = drivableAllowed;
    }
}
