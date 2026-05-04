package kz.dissertation.caranalytics.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import kz.dissertation.caranalytics.dto.DiagnosticReportResponse;
import kz.dissertation.caranalytics.dto.DiagnosticSessionRequest;
import kz.dissertation.caranalytics.dto.DiagnosticSessionResponse;
import kz.dissertation.caranalytics.dto.FaultCodeDictionaryResponse;
import kz.dissertation.caranalytics.dto.FaultCodeRequest;
import kz.dissertation.caranalytics.dto.FaultCodeResponse;
import kz.dissertation.caranalytics.dto.ObdReadingResponse;
import kz.dissertation.caranalytics.dto.RepairGuideResponse;
import kz.dissertation.caranalytics.dto.RawObdFrameResponse;
import kz.dissertation.caranalytics.dto.RecommendationResponse;
import kz.dissertation.caranalytics.dto.ServiceCenterResponse;
import kz.dissertation.caranalytics.dto.SparePartResponse;
import kz.dissertation.caranalytics.dto.VehicleInfoItemResponse;
import kz.dissertation.caranalytics.dto.VehicleResponse;
import kz.dissertation.caranalytics.model.DiagnosticReport;
import kz.dissertation.caranalytics.exception.ResourceNotFoundException;
import kz.dissertation.caranalytics.model.DiagnosticSession;
import kz.dissertation.caranalytics.model.FaultCode;
import kz.dissertation.caranalytics.model.FaultCodeType;
import kz.dissertation.caranalytics.model.ObdReading;
import kz.dissertation.caranalytics.model.RawObdFrame;
import kz.dissertation.caranalytics.model.Recommendation;
import kz.dissertation.caranalytics.model.SeverityLevel;
import kz.dissertation.caranalytics.model.Vehicle;
import kz.dissertation.caranalytics.model.VehicleInfoItem;
import kz.dissertation.caranalytics.repository.DiagnosticSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DiagnosticSessionServiceImpl implements DiagnosticSessionService {

    private final DiagnosticSessionRepository diagnosticSessionRepository;
    private final VehicleServiceImpl vehicleService;
    private final DiagnosticAiAssistant diagnosticAiAssistant;
    private final SupportCatalogService supportCatalogService;
    private final FaultCodeDictionaryService faultCodeDictionaryService;

    public DiagnosticSessionServiceImpl(
            DiagnosticSessionRepository diagnosticSessionRepository,
            VehicleServiceImpl vehicleService,
            DiagnosticAiAssistant diagnosticAiAssistant,
            SupportCatalogService supportCatalogService,
            FaultCodeDictionaryService faultCodeDictionaryService
    ) {
        this.diagnosticSessionRepository = diagnosticSessionRepository;
        this.vehicleService = vehicleService;
        this.diagnosticAiAssistant = diagnosticAiAssistant;
        this.supportCatalogService = supportCatalogService;
        this.faultCodeDictionaryService = faultCodeDictionaryService;
    }

    @Override
    @Transactional
    public DiagnosticSessionResponse create(DiagnosticSessionRequest request) {
        Vehicle vehicle = vehicleService.getVehicle(request.getVehicleId());
        LocalDateTime sessionStartedAt = LocalDateTime.now();
        List<FaultCodeRequest> enrichedFaultCodes = faultCodeDictionaryService.enrichFaultCodes(request.getFaultCodes());

        DiagnosticSession session = new DiagnosticSession();
        session.setVehicle(vehicle);
        session.setConnectionType(request.getConnectionType());
        session.setAdapterName(request.getAdapterName());
        session.setAdapterIdentifier(request.getAdapterIdentifier());
        session.setProtocol(request.getProtocol());
        session.setStartedAt(sessionStartedAt);
        session.setOverallStatus(diagnosticAiAssistant.buildOverallStatus(enrichedFaultCodes));

        safeList(request.getRawFrames()).forEach(frameRequest -> {
            RawObdFrame frame = new RawObdFrame();
            frame.setSession(session);
            frame.setMode(frameRequest.getMode());
            frame.setPid(frameRequest.getPid());
            frame.setRawResponse(frameRequest.getRawResponse());
            frame.setDecodedLabel(frameRequest.getDecodedLabel());
            frame.setManufacturerSpecific(Boolean.TRUE.equals(frameRequest.getManufacturerSpecific()));
            frame.setFrameTimestamp(
                    frameRequest.getFrameTimestamp() == null ? sessionStartedAt : frameRequest.getFrameTimestamp()
            );
            session.getRawFrames().add(frame);
        });

        safeList(request.getVehicleInfoItems()).forEach(itemRequest -> {
            VehicleInfoItem item = new VehicleInfoItem();
            item.setSession(session);
            item.setInfoKey(itemRequest.getInfoKey());
            item.setInfoValue(itemRequest.getInfoValue());
            item.setSourceMode(itemRequest.getSourceMode());
            session.getVehicleInfoItems().add(item);
        });

        safeList(request.getReadings()).forEach(readingRequest -> {
            ObdReading reading = new ObdReading();
            reading.setSession(session);
            reading.setParameterName(readingRequest.getParameterName());
            reading.setPidCode(readingRequest.getPidCode());
            reading.setSourceMode(defaultString(readingRequest.getSourceMode(), "01"));
            reading.setFreezeFrame(Boolean.TRUE.equals(readingRequest.getFreezeFrame()));
            reading.setManufacturerSpecific(Boolean.TRUE.equals(readingRequest.getManufacturerSpecific()));
            reading.setDescription(readingRequest.getDescription());
            reading.setParameterValue(readingRequest.getParameterValue());
            reading.setUnit(readingRequest.getUnit());
            session.getReadings().add(reading);
        });

        enrichedFaultCodes.forEach(faultCodeRequest -> {
            FaultCode faultCode = new FaultCode();
            faultCode.setSession(session);
            faultCode.setCode(faultCodeRequest.getCode());
            faultCode.setDescription(faultCodeRequest.getDescription());
            faultCode.setFaultCodeType(
                    faultCodeRequest.getFaultCodeType() == null ? FaultCodeType.STORED : faultCodeRequest.getFaultCodeType()
            );
            faultCode.setSourceMode(defaultString(faultCodeRequest.getSourceMode(), "03"));
            faultCode.setManufacturerSpecific(Boolean.TRUE.equals(faultCodeRequest.getManufacturerSpecific()));
            faultCode.setSeverity(faultCodeRequest.getSeverity() == null ? SeverityLevel.MEDIUM : faultCodeRequest.getSeverity());
            session.getFaultCodes().add(faultCode);
        });

        diagnosticAiAssistant.buildRecommendations(request.getReadings(), enrichedFaultCodes)
                .forEach(draft -> {
                    Recommendation recommendation = new Recommendation();
                    recommendation.setSession(session);
                    recommendation.setType(draft.type());
                    recommendation.setMessage(draft.message());
                    recommendation.setActionLabel(draft.actionLabel());
                    recommendation.setReferenceCode(draft.referenceCode());
                    session.getRecommendations().add(recommendation);
                });

        DiagnosticAiAssistant.ReportDraft reportDraft = diagnosticAiAssistant.buildReport(
                request.getReadings(),
                enrichedFaultCodes
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
                                frame.getManufacturerSpecific(),
                                frame.getFrameTimestamp()
                        ))
                        .toList(),
                session.getVehicleInfoItems().stream()
                        .map(item -> new VehicleInfoItemResponse(
                                item.getInfoKey(),
                                item.getInfoValue(),
                                item.getSourceMode()
                        ))
                        .toList(),
                session.getReadings().stream()
                        .map(reading -> new ObdReadingResponse(
                                reading.getParameterName(),
                                reading.getPidCode(),
                                reading.getSourceMode(),
                                reading.getFreezeFrame(),
                                reading.getManufacturerSpecific(),
                                reading.getDescription(),
                                reading.getParameterValue(),
                                reading.getUnit()
                        ))
                        .toList(),
                session.getFaultCodes().stream()
                        .map(this::mapFaultCode)
                        .toList(),
                session.getRecommendations().stream()
                        .map(recommendation -> mapRecommendation(session, recommendation))
                        .toList(),
                session.getReport() == null ? null : mapReport(session.getReport())
        );
    }

    private RecommendationResponse mapRecommendation(DiagnosticSession session, Recommendation recommendation) {
        List<ServiceCenterResponse> serviceCenters = supportCatalogService.suggestServiceCenters(
                recommendation.getType(),
                session,
                recommendation.getReferenceCode()
        );
        List<SparePartResponse> spareParts = supportCatalogService.suggestSpareParts(
                session.getVehicle(),
                recommendation.getReferenceCode()
        );
        List<RepairGuideResponse> repairGuides = supportCatalogService.suggestRepairGuides(
                recommendation.getReferenceCode()
        );

        return new RecommendationResponse(
                recommendation.getType(),
                recommendation.getMessage(),
                recommendation.getActionLabel(),
                recommendation.getReferenceCode(),
                serviceCenters,
                spareParts,
                repairGuides
        );
    }

    private FaultCodeResponse mapFaultCode(FaultCode faultCode) {
        FaultCodeDictionaryResponse dictionaryEntry = faultCodeDictionaryService.lookupByCode(faultCode.getCode())
                .orElse(null);

        return new FaultCodeResponse(
                faultCode.getCode(),
                faultCode.getDescription(),
                faultCode.getFaultCodeType(),
                faultCode.getSourceMode(),
                faultCode.getManufacturerSpecific(),
                faultCode.getSeverity(),
                dictionaryEntry
        );
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
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
