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
        report.setRiskForecast(reportDraft.riskForecast());
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
                translateRecommendationRu(recommendation.getMessage()),
                recommendation.getMessage(),
                recommendation.getActionLabel(),
                translateActionLabelRu(recommendation.getActionLabel()),
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
                reportDraftRuSummary(report.getSummary()),
                report.getSummary(),
                report.getRiskForecast(),
                reportDraftRuRisk(report.getRiskForecast()),
                report.getRiskForecast(),
                report.getGeneratedAt(),
                report.getNextActions(),
                report.getNextActions().stream().map(this::translateActionRu).toList(),
                report.getNextActions()
        );
    }

    private String translateRecommendationRu(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains("Clear DTC faults")) {
            return "Явных DTC-ошибок не найдено. Продолжайте наблюдать за OBD2-параметрами и повторите диагностику, если симптомы сохранятся.";
        }
        if (value.contains("non-critical issue")) {
            return "Код указывает на некритичную проблему. Начните с осмотра и повторите скан после короткой поездки.";
        }
        if (value.contains("basic repair steps")) {
            return "Код может требовать базового ремонта: проверьте разъёмы, датчики, проводку, очистку или замену компонента.";
        }
        if (value.contains("replacing a faulty component")) {
            return "Код указывает на вероятную замену неисправного компонента. Подготовьте поиск OEM или совместимой запчасти по VIN и двигателю.";
        }
        if (value.contains("is critical")) {
            return "Код критический. Прекратите активную эксплуатацию автомобиля и обратитесь в СТО для углублённой диагностики.";
        }
        if (value.contains("Engine temperature")) {
            return "Температура двигателя выше нормы. Срочно проверьте систему охлаждения, вентилятор радиатора и термостат.";
        }
        return value;
    }

    private String translateActionLabelRu(String value) {
        if (value == null) {
            return "";
        }
        return switch (value) {
            case "Repeat scan later" -> "Повторить скан позже";
            case "Inspect and monitor" -> "Осмотреть и наблюдать";
            case "View repair steps" -> "Открыть шаги ремонта";
            case "Find spare parts" -> "Найти запчасти";
            case "Go to service center" -> "Обратиться в СТО";
            case "Check cooling system" -> "Проверить охлаждение";
            default -> value;
        };
    }

    private String reportDraftRuSummary(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains("Vehicle telemetry is currently stable")) {
            return "Телеметрия автомобиля сейчас стабильна. Срочный ремонт не требуется.";
        }
        if (value.contains("Maintenance should be scheduled")) {
            return "Диагностика нашла коды ошибок или отклонения телеметрии. Нужно запланировать обслуживание.";
        }
        if (value.contains("Serious issues were detected")) {
            return "Обнаружены серьёзные признаки неисправности. Автомобиль нужно как можно скорее показать специалисту.";
        }
        if (value.contains("Critical fault pattern")) {
            return "Обнаружен критический набор признаков. Дальнейшая езда может повредить автомобиль или создать риск безопасности.";
        }
        return value;
    }

    private String reportDraftRuRisk(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains("baseline")) {
            return "Активной тенденции поломки пока не видно. Используйте этот скан как базовый и повторите диагностику при появлении симптомов.";
        }
        if (value.contains("Cooling anomalies")) {
            return "Проблемы охлаждения могут начаться с датчика или уровня антифриза, а затем привести к перегреву, повреждению ГБЦ и остановке в дороге.";
        }
        if (value.contains("Low oil pressure")) {
            return "Низкое давление масла быстро становится опасным: езда может повредить вкладыши, турбину и сам двигатель.";
        }
        if (value.contains("Low voltage")) {
            return "Низкое напряжение может начаться с случайных предупреждений, а затем привести к отказу запуска и ошибкам блоков управления.";
        }
        if (value.contains("look minor")) {
            return "Текущие коды выглядят не критично, но при повторении могут указывать на износ проводки, разъёмов или датчика. Проверьте, вернутся ли они после сброса и короткой поездки.";
        }
        if (value.contains("not an immediate stop")) {
            return "Сейчас это не сигнал немедленно остановиться, но игнорирование может повысить расход топлива, повредить связанные датчики или перейти в постоянную ошибку под нагрузкой.";
        }
        if (value.contains("can progress to")) {
            return "Эти ошибки могут перейти в пропуски зажигания, перегрев, проблемы зарядки или аварийный режим трансмиссии. Ограничьте поездки до выяснения причины.";
        }
        return value;
    }

    private String translateActionRu(String value) {
        if (value == null) {
            return "";
        }
        if (value.startsWith("Keep the vehicle")) {
            return "Наблюдайте за автомобилем и повторите OBD2-скан, если симптомы вернутся.";
        }
        if (value.startsWith("Store the current")) {
            return "Сохраните текущую сессию как базу для сравнения.";
        }
        if (value.startsWith("Book a maintenance")) {
            return "Запишитесь на обслуживание и покажите сохранённую диагностику мастеру.";
        }
        if (value.startsWith("Inspect wiring")) {
            return "Проверьте проводку, разъёмы и связанные датчики перед заменой деталей.";
        }
        if (value.startsWith("Limit vehicle")) {
            return "Ограничьте поездки, пока СТО не подтвердит причину неисправности.";
        }
        if (value.startsWith("Prepare VIN")) {
            return "Подготовьте поиск запчастей по VIN для подозреваемого узла.";
        }
        if (value.startsWith("Stop intensive")) {
            return "Прекратите активную езду и организуйте срочную техническую проверку.";
        }
        if (value.startsWith("Consider vehicle towing")) {
            return "Рассмотрите эвакуацию, если есть проблемы с маслом или охлаждением.";
        }
        if (value.startsWith("Export DTC codes")) {
            return value.replace("Export DTC codes for repair history:", "Сохраните DTC-коды для истории ремонта:");
        }
        if (value.startsWith("Re-check abnormal")) {
            return "Повторно проверьте ненормальные значения после осмотра разъёмов и датчиков.";
        }
        return value;
    }
}
