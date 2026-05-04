package kz.dissertation.caranalytics.dto;

import jakarta.validation.constraints.NotBlank;
import kz.dissertation.caranalytics.model.FaultCodeType;
import kz.dissertation.caranalytics.model.SeverityLevel;

public class FaultCodeRequest {

    @NotBlank
    private String code;

    private String description;

    private FaultCodeType faultCodeType;

    private String sourceMode;

    private Boolean manufacturerSpecific;

    private SeverityLevel severity;

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
