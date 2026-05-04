package kz.dissertation.caranalytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class RawObdFrameRequest {

    @NotBlank
    private String mode;

    private String pid;

    @NotBlank
    private String rawResponse;

    @NotBlank
    private String decodedLabel;

    private Boolean manufacturerSpecific;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime frameTimestamp;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public String getDecodedLabel() {
        return decodedLabel;
    }

    public void setDecodedLabel(String decodedLabel) {
        this.decodedLabel = decodedLabel;
    }

    public Boolean getManufacturerSpecific() {
        return manufacturerSpecific;
    }

    public void setManufacturerSpecific(Boolean manufacturerSpecific) {
        this.manufacturerSpecific = manufacturerSpecific;
    }

    public LocalDateTime getFrameTimestamp() {
        return frameTimestamp;
    }

    public void setFrameTimestamp(LocalDateTime frameTimestamp) {
        this.frameTimestamp = frameTimestamp;
    }
}
