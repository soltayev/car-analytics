package kz.dissertation.caranalytics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import kz.dissertation.caranalytics.model.ConnectionType;
import kz.dissertation.caranalytics.model.ObdProtocol;

public class DiagnosticSessionRequest {

    @NotNull
    private Long vehicleId;

    @NotNull
    private ConnectionType connectionType;

    @NotBlank
    private String adapterName;

    @NotBlank
    private String adapterIdentifier;

    @NotNull
    private ObdProtocol protocol;

    @Valid
    @NotEmpty
    private List<RawObdFrameRequest> rawFrames;

    @Valid
    @NotEmpty
    private List<ObdReadingRequest> readings;

    @Valid
    private List<FaultCodeRequest> faultCodes;

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public String getAdapterIdentifier() {
        return adapterIdentifier;
    }

    public void setAdapterIdentifier(String adapterIdentifier) {
        this.adapterIdentifier = adapterIdentifier;
    }

    public ObdProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ObdProtocol protocol) {
        this.protocol = protocol;
    }

    public List<RawObdFrameRequest> getRawFrames() {
        return rawFrames;
    }

    public void setRawFrames(List<RawObdFrameRequest> rawFrames) {
        this.rawFrames = rawFrames;
    }

    public List<ObdReadingRequest> getReadings() {
        return readings;
    }

    public void setReadings(List<ObdReadingRequest> readings) {
        this.readings = readings;
    }

    public List<FaultCodeRequest> getFaultCodes() {
        return faultCodes;
    }

    public void setFaultCodes(List<FaultCodeRequest> faultCodes) {
        this.faultCodes = faultCodes;
    }
}
