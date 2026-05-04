package kz.dissertation.caranalytics.dto;

import jakarta.validation.constraints.NotBlank;

public class VehicleInfoItemRequest {

    @NotBlank
    private String infoKey;

    @NotBlank
    private String infoValue;

    @NotBlank
    private String sourceMode;

    public String getInfoKey() {
        return infoKey;
    }

    public void setInfoKey(String infoKey) {
        this.infoKey = infoKey;
    }

    public String getInfoValue() {
        return infoValue;
    }

    public void setInfoValue(String infoValue) {
        this.infoValue = infoValue;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode) {
        this.sourceMode = sourceMode;
    }
}
