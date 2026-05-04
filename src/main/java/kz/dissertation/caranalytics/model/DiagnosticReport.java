package kz.dissertation.caranalytics.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diagnostic_reports")
public class DiagnosticReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "session_id", unique = true)
    private DiagnosticSession session;

    @Column(nullable = false)
    private Integer healthScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportUrgency urgency;

    @Column(nullable = false)
    private Boolean drivable;

    @Column(nullable = false)
    private Boolean towRecommended;

    @Column(nullable = false, length = 1000)
    private String primaryIssue;

    @Column(nullable = false, length = 2000)
    private String summary;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "diagnostic_report_actions", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "action_text", nullable = false, length = 500)
    private List<String> nextActions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DiagnosticSession getSession() {
        return session;
    }

    public void setSession(DiagnosticSession session) {
        this.session = session;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public ReportUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(ReportUrgency urgency) {
        this.urgency = urgency;
    }

    public Boolean getDrivable() {
        return drivable;
    }

    public void setDrivable(Boolean drivable) {
        this.drivable = drivable;
    }

    public Boolean getTowRecommended() {
        return towRecommended;
    }

    public void setTowRecommended(Boolean towRecommended) {
        this.towRecommended = towRecommended;
    }

    public String getPrimaryIssue() {
        return primaryIssue;
    }

    public void setPrimaryIssue(String primaryIssue) {
        this.primaryIssue = primaryIssue;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<String> getNextActions() {
        return nextActions;
    }
}
