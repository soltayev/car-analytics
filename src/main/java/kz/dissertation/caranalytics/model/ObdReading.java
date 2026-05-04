package kz.dissertation.caranalytics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "obd_readings")
public class ObdReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private DiagnosticSession session;

    @Column(nullable = false)
    private String parameterName;

    @Column(length = 20)
    private String pidCode;

    @Column(nullable = false, length = 20)
    private String sourceMode;

    @Column(nullable = false)
    private Boolean freezeFrame;

    @Column(nullable = false)
    private Boolean manufacturerSpecific;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal parameterValue;

    @Column(nullable = false)
    private String unit;

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

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getPidCode() {
        return pidCode;
    }

    public void setPidCode(String pidCode) {
        this.pidCode = pidCode;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode) {
        this.sourceMode = sourceMode;
    }

    public Boolean getFreezeFrame() {
        return freezeFrame;
    }

    public void setFreezeFrame(Boolean freezeFrame) {
        this.freezeFrame = freezeFrame;
    }

    public Boolean getManufacturerSpecific() {
        return manufacturerSpecific;
    }

    public void setManufacturerSpecific(Boolean manufacturerSpecific) {
        this.manufacturerSpecific = manufacturerSpecific;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(BigDecimal parameterValue) {
        this.parameterValue = parameterValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
