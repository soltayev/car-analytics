package kz.dissertation.caranalytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kz.dissertation.caranalytics.model.SeverityLevel;

public class FaultCodeRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String description;

    @NotNull
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

    public SeverityLevel getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }
}
