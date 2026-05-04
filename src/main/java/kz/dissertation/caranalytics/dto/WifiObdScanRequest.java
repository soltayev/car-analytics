package kz.dissertation.caranalytics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import kz.dissertation.caranalytics.model.ObdProtocol;

public class WifiObdScanRequest {

    @NotNull
    private Long vehicleId;

    @NotBlank
    private String host;

    @NotNull
    @Min(1)
    @Max(65535)
    private Integer port;

    @NotBlank
    private String adapterName;

    private String adapterIdentifier;

    private ObdProtocol protocol;

    @Min(1000)
    @Max(15000)
    private Integer socketTimeoutMs;

    private List<String> currentDataPids;

    private List<String> freezeFramePids;

    private Boolean includeStoredFaultCodes;

    private Boolean includePendingFaultCodes;

    private Boolean includeFreezeFrame;

    private Boolean includeVehicleInfo;

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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

    public Integer getSocketTimeoutMs() {
        return socketTimeoutMs;
    }

    public void setSocketTimeoutMs(Integer socketTimeoutMs) {
        this.socketTimeoutMs = socketTimeoutMs;
    }

    public List<String> getCurrentDataPids() {
        return currentDataPids;
    }

    public void setCurrentDataPids(List<String> currentDataPids) {
        this.currentDataPids = currentDataPids;
    }

    public List<String> getFreezeFramePids() {
        return freezeFramePids;
    }

    public void setFreezeFramePids(List<String> freezeFramePids) {
        this.freezeFramePids = freezeFramePids;
    }

    public Boolean getIncludeStoredFaultCodes() {
        return includeStoredFaultCodes;
    }

    public void setIncludeStoredFaultCodes(Boolean includeStoredFaultCodes) {
        this.includeStoredFaultCodes = includeStoredFaultCodes;
    }

    public Boolean getIncludePendingFaultCodes() {
        return includePendingFaultCodes;
    }

    public void setIncludePendingFaultCodes(Boolean includePendingFaultCodes) {
        this.includePendingFaultCodes = includePendingFaultCodes;
    }

    public Boolean getIncludeFreezeFrame() {
        return includeFreezeFrame;
    }

    public void setIncludeFreezeFrame(Boolean includeFreezeFrame) {
        this.includeFreezeFrame = includeFreezeFrame;
    }

    public Boolean getIncludeVehicleInfo() {
        return includeVehicleInfo;
    }

    public void setIncludeVehicleInfo(Boolean includeVehicleInfo) {
        this.includeVehicleInfo = includeVehicleInfo;
    }
}
