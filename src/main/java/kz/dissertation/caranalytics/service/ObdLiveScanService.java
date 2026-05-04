package kz.dissertation.caranalytics.service;

import kz.dissertation.caranalytics.dto.DiagnosticSessionRequest;
import kz.dissertation.caranalytics.dto.DiagnosticSessionResponse;
import kz.dissertation.caranalytics.dto.WifiObdScanRequest;
import kz.dissertation.caranalytics.model.ConnectionType;
import kz.dissertation.caranalytics.model.ObdProtocol;
import org.springframework.stereotype.Service;

@Service
public class ObdLiveScanService {

    private final Elm327WifiScanner elm327WifiScanner;
    private final DiagnosticSessionService diagnosticSessionService;

    public ObdLiveScanService(
            Elm327WifiScanner elm327WifiScanner,
            DiagnosticSessionService diagnosticSessionService
    ) {
        this.elm327WifiScanner = elm327WifiScanner;
        this.diagnosticSessionService = diagnosticSessionService;
    }

    public DiagnosticSessionResponse scanViaWifi(WifiObdScanRequest request) {
        Elm327WifiScanner.LiveScanPayload payload = elm327WifiScanner.scan(request);

        DiagnosticSessionRequest sessionRequest = new DiagnosticSessionRequest();
        sessionRequest.setVehicleId(request.getVehicleId());
        sessionRequest.setConnectionType(ConnectionType.WIFI);
        sessionRequest.setAdapterName(request.getAdapterName());
        sessionRequest.setAdapterIdentifier(resolveAdapterIdentifier(request));
        sessionRequest.setProtocol(request.getProtocol() == null ? ObdProtocol.UNKNOWN : request.getProtocol());
        sessionRequest.setRawFrames(payload.rawFrames());
        sessionRequest.setVehicleInfoItems(payload.vehicleInfoItems());
        sessionRequest.setReadings(payload.readings());
        sessionRequest.setFaultCodes(payload.faultCodes());

        return diagnosticSessionService.create(sessionRequest);
    }

    private String resolveAdapterIdentifier(WifiObdScanRequest request) {
        if (request.getAdapterIdentifier() == null || request.getAdapterIdentifier().isBlank()) {
            return request.getHost() + ":" + request.getPort();
        }
        return request.getAdapterIdentifier();
    }
}
