package kz.dissertation.caranalytics.dto;

import java.time.LocalDateTime;
import java.util.List;
import kz.dissertation.caranalytics.model.ConnectionType;
import kz.dissertation.caranalytics.model.ObdProtocol;

public record DiagnosticSessionResponse(
        Long id,
        VehicleResponse vehicle,
        ConnectionType connectionType,
        String adapterName,
        String adapterIdentifier,
        ObdProtocol protocol,
        LocalDateTime startedAt,
        String overallStatus,
        List<RawObdFrameResponse> rawFrames,
        List<ObdReadingResponse> readings,
        List<FaultCodeResponse> faultCodes,
        List<RecommendationResponse> recommendations,
        DiagnosticReportResponse report
) {
}
