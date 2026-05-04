package kz.dissertation.caranalytics.service;

import java.time.LocalDateTime;
import java.util.List;
import kz.dissertation.caranalytics.dto.DiagnosticReportResponse;
import kz.dissertation.caranalytics.dto.DiagnosticSessionRequest;
import kz.dissertation.caranalytics.dto.DiagnosticSessionResponse;
import kz.dissertation.caranalytics.dto.FaultCodeResponse;
import kz.dissertation.caranalytics.dto.ObdReadingResponse;
import kz.dissertation.caranalytics.dto.RawObdFrameResponse;
import kz.dissertation.caranalytics.dto.RecommendationResponse;
import kz.dissertation.caranalytics.dto.VehicleResponse;
import kz.dissertation.caranalytics.model.DiagnosticReport;
import kz.dissertation.caranalytics.exception.ResourceNotFoundException;
import kz.dissertation.caranalytics.model.DiagnosticSession;
import kz.dissertation.caranalytics.model.FaultCode;
import kz.dissertation.caranalytics.model.ObdReading;
import kz.dissertation.caranalytics.model.RawObdFrame;
import kz.dissertation.caranalytics.model.Recommendation;
import kz.dissertation.caranalytics.model.Vehicle;
import kz.dissertation.caranalytics.repository.DiagnosticSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DiagnosticSessionServiceImpl implements DiagnosticSessionService {

    private final DiagnosticSessionRepository diagnosticSessionRepository;
    private final VehicleServiceImpl vehicleService;
    private final DiagnosticAiAssistant diagnosticAiAssistant;

    public DiagnosticSessionServiceImpl(
            DiagnosticSessionRepository diagnosticSessionRepository,
            VehicleServiceImpl vehicleService,
            DiagnosticAiAssistant diagnosticAiAssistant
    ) {
        this.diagnosticSessionRepository = diagnosticSessionRepository;
        this.vehicleService = vehicleService;
        this.diagnosticAiAssistant = diagnosticAiAssistant;
    }

    @Override
    @Transactional
    public DiagnosticSessionResponse create(DiagnosticSessionRequest request) {
        Vehicle vehicle = vehicleService.getVehicle(request.getVehicleId());
        LocalDateTime sessionStartedAt = LocalDateTime.now();

        DiagnosticSession session = new DiagnosticSession();
        session.setVehicle(vehicle);
        session.setConnectionType(request.getConnectionType());
        session.setAdapterName(request.getAdapterName());
        session.setAdapterIdentifier(request.getAdapterIdentifier());
        session.setProtocol(request.getProtocol());
        session.setStartedAt(sessionStartedAt);
        session.setOverallStatus(diagnosticAiAssistant.buildOverallStatus(request.getFaultCodes()));

        request.getRawFrames().forEach(frameRequest -> {
            RawObdFrame frame = new RawObdFrame();
            frame.setSession(session);
            frame.setMode(frameRequest.getMode());
            frame.setPid(frameRequest.getPid());
            frame.setRawResponse(frameRequest.getRawResponse());
            frame.setDecodedLabel(frameRequest.getDecodedLabel());
            frame.setFrameTimestamp(
                    frameRequest.getFrameTimestamp() == null ? sessionStartedAt : frameRequest.getFrameTimestamp()
            );
            session.getRawFrames().add(frame);
        });

        request.getReadings().forEach(readingRequest -> {
            ObdReading reading = new ObdReading();
            reading.setSession(session);
            reading.setParameterName(readingRequest.getParameterName());
            reading.setPidCode(readingRequest.getPidCode());
            reading.setParameterValue(readingRequest.getParameterValue());
            reading.setUnit(readingRequest.getUnit());
            session.getReadings().add(reading);
        });

        if (request.getFaultCodes() != null) {
            request.getFaultCodes().forEach(faultCodeRequest -> {
                FaultCode faultCode = new FaultCode();
                faultCode.setSession(session);
                faultCode.setCode(faultCodeRequest.getCode());
                faultCode.setDescription(faultCodeRequest.getDescription());
                faultCode.setSeverity(faultCodeRequest.getSeverity());
                session.getFaultCodes().add(faultCode);
            });
        }

        diagnosticAiAssistant.buildRecommendations(request.getReadings(), request.getFaultCodes())
                .forEach(draft -> {
                    Recommendation recommendation = new Recommendation();
                    recommendation.setSession(session);
                    recommendation.setType(draft.type());
                    recommendation.setMessage(draft.message());
                    recommendation.setActionLabel(draft.actionLabel());
                    session.getRecommendations().add(recommendation);
                });

        DiagnosticAiAssistant.ReportDraft reportDraft = diagnosticAiAssistant.buildReport(
                request.getReadings(),
                request.getFaultCodes()
        );

        DiagnosticReport report = new DiagnosticReport();
        report.setSession(session);
        report.setHealthScore(reportDraft.healthScore());
        report.setUrgency(reportDraft.urgency());
        report.setDrivable(reportDraft.drivable());
        report.setTowRecommended(reportDraft.towRecommended());
        report.setPrimaryIssue(reportDraft.primaryIssue());
        report.setSummary(reportDraft.summary());
        report.setGeneratedAt(sessionStartedAt);
        report.getNextActions().addAll(reportDraft.nextActions());
        session.setReport(report);

        return map(diagnosticSessionRepository.save(session));
    }

    @Override
    public List<DiagnosticSessionResponse> findAll() {
        return diagnosticSessionRepository.findAll().stream().map(this::map).toList();
    }

    @Override
    public DiagnosticSessionResponse findById(Long id) {
        DiagnosticSession session = diagnosticSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticSession", id));
        return map(session);
    }

    @Override
    public DiagnosticReportResponse getReport(Long id) {
        DiagnosticSession session = diagnosticSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticSession", id));
        if (session.getReport() == null) {
            throw new ResourceNotFoundException("DiagnosticReport", id);
        }
        return mapReport(session.getReport());
    }

    private DiagnosticSessionResponse map(DiagnosticSession session) {
        Vehicle vehicle = session.getVehicle();
        VehicleResponse vehicleResponse = new VehicleResponse(
                vehicle.getId(),
                vehicle.getVin(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getProductionYear(),
                vehicle.getEngineType()
        );

        return new DiagnosticSessionResponse(
                session.getId(),
                vehicleResponse,
                session.getConnectionType(),
                session.getAdapterName(),
                session.getAdapterIdentifier(),
                session.getProtocol(),
                session.getStartedAt(),
                session.getOverallStatus(),
                session.getRawFrames().stream()
                        .map(frame -> new RawObdFrameResponse(
                                frame.getMode(),
                                frame.getPid(),
                                frame.getRawResponse(),
                                frame.getDecodedLabel(),
                                frame.getFrameTimestamp()
                        ))
                        .toList(),
                session.getReadings().stream()
                        .map(reading -> new ObdReadingResponse(
                                reading.getParameterName(),
                                reading.getPidCode(),
                                reading.getParameterValue(),
                                reading.getUnit()
                        ))
                        .toList(),
                session.getFaultCodes().stream()
                        .map(faultCode -> new FaultCodeResponse(
                                faultCode.getCode(),
                                faultCode.getDescription(),
                                faultCode.getSeverity()
                        ))
                        .toList(),
                session.getRecommendations().stream()
                        .map(recommendation -> new RecommendationResponse(
                                recommendation.getType(),
                                recommendation.getMessage(),
                                recommendation.getActionLabel()
                        ))
                        .toList(),
                session.getReport() == null ? null : mapReport(session.getReport())
        );
    }

    private DiagnosticReportResponse mapReport(DiagnosticReport report) {
        return new DiagnosticReportResponse(
                report.getId(),
                report.getHealthScore(),
                report.getUrgency(),
                report.getDrivable(),
                report.getTowRecommended(),
                report.getPrimaryIssue(),
                report.getSummary(),
                report.getGeneratedAt(),
                report.getNextActions()
        );
    }
}
