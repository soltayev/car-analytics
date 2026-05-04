package kz.dissertation.caranalytics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fault_codes")
public class FaultCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private DiagnosticSession session;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FaultCodeType faultCodeType;

    @Column(nullable = false, length = 20)
    private String sourceMode;

    @Column(nullable = false)
    private Boolean manufacturerSpecific;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeverityLevel severity;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FaultCodeType getFaultCodeType() {
        return faultCodeType;
    }

    public void setFaultCodeType(FaultCodeType faultCodeType) {
        this.faultCodeType = faultCodeType;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode) {
        this.sourceMode = sourceMode;
    }

    public Boolean getManufacturerSpecific() {
        return manufacturerSpecific;
    }

    public void setManufacturerSpecific(Boolean manufacturerSpecific) {
        this.manufacturerSpecific = manufacturerSpecific;
    }

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }
}
