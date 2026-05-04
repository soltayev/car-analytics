package kz.dissertation.caranalytics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "raw_obd_frames")
public class RawObdFrame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private DiagnosticSession session;

    @Column(nullable = false, length = 20)
    private String mode;

    @Column(length = 20)
    private String pid;

    @Column(nullable = false, length = 2000)
    private String rawResponse;

    @Column(nullable = false)
    private String decodedLabel;

    @Column(nullable = false)
    private Boolean manufacturerSpecific;

    @Column(nullable = false)
    private LocalDateTime frameTimestamp;

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
