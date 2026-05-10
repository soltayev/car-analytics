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

    @Column(nullable = false, length = 255)
    private String titleRu;

    @Column(nullable = false, length = 255)
    private String titleEn;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 2000)
    private String descriptionRu;

    @Column(nullable = false, length = 2000)
    private String descriptionEn;

    @Column(nullable = false, length = 120)
    private String systemName;

    @Column(nullable = false, length = 120)
    private String systemNameRu;

    @Column(nullable = false, length = 120)
    private String systemNameEn;

    @Column(nullable = false, length = 120)
    private String subsystem;

    @Column(nullable = false, length = 120)
    private String subsystemRu;

    @Column(nullable = false, length = 120)
    private String subsystemEn;

    @Column(nullable = false)
    private Boolean manufacturerSpecific;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeverityLevel defaultSeverity;

    @Column(nullable = false, length = 2000)
    private String possibleCauses;

    @Column(nullable = false, length = 2000)
    private String possibleCausesRu;

    @Column(nullable = false, length = 2000)
    private String possibleCausesEn;

    @Column(nullable = false, length = 2000)
    private String recommendedActions;

    @Column(nullable = false, length = 2000)
    private String recommendedActionsRu;

    @Column(nullable = false, length = 2000)
    private String recommendedActionsEn;

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

    public String getTitleRu() {
        return titleRu;
    }

    public void setTitleRu(String titleRu) {
        this.titleRu = titleRu;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionRu() {
        return descriptionRu;
    }

    public void setDescriptionRu(String descriptionRu) {
        this.descriptionRu = descriptionRu;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getSystemNameRu() {
        return systemNameRu;
    }

    public void setSystemNameRu(String systemNameRu) {
        this.systemNameRu = systemNameRu;
    }

    public String getSystemNameEn() {
        return systemNameEn;
    }

    public void setSystemNameEn(String systemNameEn) {
        this.systemNameEn = systemNameEn;
    }

    public String getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    public String getSubsystemRu() {
        return subsystemRu;
    }

    public void setSubsystemRu(String subsystemRu) {
        this.subsystemRu = subsystemRu;
    }

    public String getSubsystemEn() {
        return subsystemEn;
    }

    public void setSubsystemEn(String subsystemEn) {
        this.subsystemEn = subsystemEn;
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

    public String getPossibleCausesRu() {
        return possibleCausesRu;
    }

    public void setPossibleCausesRu(String possibleCausesRu) {
        this.possibleCausesRu = possibleCausesRu;
    }

    public String getPossibleCausesEn() {
        return possibleCausesEn;
    }

    public void setPossibleCausesEn(String possibleCausesEn) {
        this.possibleCausesEn = possibleCausesEn;
    }

    public String getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(String recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public String getRecommendedActionsRu() {
        return recommendedActionsRu;
    }

    public void setRecommendedActionsRu(String recommendedActionsRu) {
        this.recommendedActionsRu = recommendedActionsRu;
    }

    public String getRecommendedActionsEn() {
        return recommendedActionsEn;
    }

    public void setRecommendedActionsEn(String recommendedActionsEn) {
        this.recommendedActionsEn = recommendedActionsEn;
    }

    public Boolean getDrivableAllowed() {
        return drivableAllowed;
    }

    public void setDrivableAllowed(Boolean drivableAllowed) {
        this.drivableAllowed = drivableAllowed;
    }
}
