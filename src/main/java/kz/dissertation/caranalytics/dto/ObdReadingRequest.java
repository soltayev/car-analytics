package kz.dissertation.caranalytics.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ObdReadingRequest {

    @NotBlank
    private String parameterName;

    private String pidCode;

    private String sourceMode;

    private Boolean freezeFrame;

    private Boolean manufacturerSpecific;

    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal parameterValue;

    @NotBlank
    private String unit;

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
