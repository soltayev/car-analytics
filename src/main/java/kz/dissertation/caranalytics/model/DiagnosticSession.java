package kz.dissertation.caranalytics.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diagnostic_sessions")
public class DiagnosticSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionType connectionType;

    @Column(nullable = false)
    private String adapterName;

    @Column(nullable = false)
    private String adapterIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObdProtocol protocol;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private String overallStatus;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RawObdFrame> rawFrames = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VehicleInfoItem> vehicleInfoItems = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ObdReading> readings = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FaultCode> faultCodes = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Recommendation> recommendations = new ArrayList<>();

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DiagnosticReport report;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public List<RawObdFrame> getRawFrames() {
        return rawFrames;
    }

    public List<VehicleInfoItem> getVehicleInfoItems() {
        return vehicleInfoItems;
    }

    public List<ObdReading> getReadings() {
        return readings;
    }

    public List<FaultCode> getFaultCodes() {
        return faultCodes;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public DiagnosticReport getReport() {
        return report;
    }

    public void setReport(DiagnosticReport report) {
        this.report = report;
    }
}
