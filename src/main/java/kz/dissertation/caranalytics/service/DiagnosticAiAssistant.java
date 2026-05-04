package kz.dissertation.caranalytics.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import kz.dissertation.caranalytics.dto.FaultCodeRequest;
import kz.dissertation.caranalytics.dto.ObdReadingRequest;
import kz.dissertation.caranalytics.model.RecommendationType;
import kz.dissertation.caranalytics.model.ReportUrgency;
import kz.dissertation.caranalytics.model.SeverityLevel;
import org.springframework.stereotype.Component;

@Component
public class DiagnosticAiAssistant {

    public String buildOverallStatus(List<FaultCodeRequest> faultCodes) {
        if (faultCodes == null || faultCodes.isEmpty()) {
            return "No critical fault codes detected. Vehicle condition is stable.";
        }

        SeverityLevel highest = faultCodes.stream()
                .map(this::severityOrDefault)
                .max(Comparator.comparingInt(this::weight))
                .orElse(SeverityLevel.LOW);

        return switch (highest) {
            case LOW -> "Minor deviations detected. Preventive monitoring is recommended.";
            case MEDIUM -> "Moderate fault detected. Service planning is recommended.";
            case HIGH -> "Significant fault detected. Repair should be scheduled soon.";
            case CRITICAL -> "Critical fault detected. Immediate diagnostic service is required.";
        };
    }

    public List<AiRecommendationDraft> buildRecommendations(
            List<ObdReadingRequest> readings,
            List<FaultCodeRequest> faultCodes
    ) {
        List<AiRecommendationDraft> drafts = new ArrayList<>();

        if (faultCodes == null || faultCodes.isEmpty()) {
            drafts.add(new AiRecommendationDraft(
                    RecommendationType.MONITORING,
                    "Clear DTC faults were not detected. Continue monitoring OBD2 telemetry and repeat diagnostics if symptoms persist.",
                    "Repeat scan later",
                    null
            ));
        } else {
            for (FaultCodeRequest code : faultCodes) {
                switch (severityOrDefault(code)) {
                    case LOW -> drafts.add(new AiRecommendationDraft(
                            RecommendationType.MONITORING,
                            "Code " + code.getCode() + " indicates a non-critical issue. Start with inspection and repeat the scan after short-term driving.",
                            "Inspect and monitor",
                            code.getCode()
                    ));
                    case MEDIUM -> drafts.add(new AiRecommendationDraft(
                            RecommendationType.SELF_REPAIR,
                            "Code " + code.getCode() + " may require basic repair steps: inspect connectors, sensors, wiring, and perform component cleaning or replacement.",
                            "View repair steps",
                            code.getCode()
                    ));
                    case HIGH -> drafts.add(new AiRecommendationDraft(
                            RecommendationType.PARTS_SEARCH,
                            "Code " + code.getCode() + " suggests replacing a faulty component. Prepare OEM or compatible spare part search by VIN and engine type.",
                            "Find spare parts",
                            code.getCode()
                    ));
                    case CRITICAL -> drafts.add(new AiRecommendationDraft(
                            RecommendationType.SERVICE_CENTER,
                            "Code " + code.getCode() + " is critical. Stop intensive use of the vehicle and contact a service center for advanced diagnostics.",
                            "Go to service center",
                            code.getCode()
                    ));
                }
            }
        }

        for (ObdReadingRequest reading : readings) {
            if ("engine_temperature".equalsIgnoreCase(reading.getParameterName())
                    && reading.getParameterValue().doubleValue() > 105.0) {
                drafts.add(new AiRecommendationDraft(
                        RecommendationType.SERVICE_CENTER,
                        "Engine temperature is above normal range. Check coolant system, radiator fan, and thermostat immediately.",
                        "Check cooling system",
                        "TEMP_OVERHEAT"
                ));
            }
        }

        return drafts;
    }

    public ReportDraft buildReport(
            List<ObdReadingRequest> readings,
            List<FaultCodeRequest> faultCodes
    ) {
        List<FaultCodeRequest> safeFaultCodes = faultCodes == null ? List.of() : faultCodes;
        List<String> anomalies = collectReadingAnomalies(readings);

        SeverityLevel highestSeverity = safeFaultCodes.stream()
                .map(this::severityOrDefault)
                .max(Comparator.comparingInt(this::weight))
                .orElse(null);

        int healthScore = 100;
        for (FaultCodeRequest code : safeFaultCodes) {
            healthScore -= switch (severityOrDefault(code)) {
                case LOW -> 5;
                case MEDIUM -> 15;
                case HIGH -> 30;
                case CRITICAL -> 45;
            };
        }

        healthScore -= anomalies.size() * 8;
        if (healthScore < 0) {
            healthScore = 0;
        }

        ReportUrgency urgency = resolveUrgency(highestSeverity, anomalies);
        boolean drivable = urgency != ReportUrgency.IMMEDIATE_STOP;
        boolean towRecommended = urgency == ReportUrgency.IMMEDIATE_STOP;

        String primaryIssue = resolvePrimaryIssue(safeFaultCodes, anomalies);
        String summary = resolveSummary(urgency, safeFaultCodes, anomalies);
        List<String> nextActions = resolveNextActions(urgency, safeFaultCodes, anomalies);

        return new ReportDraft(
                healthScore,
                urgency,
                drivable,
                towRecommended,
                primaryIssue,
                summary,
                nextActions
        );
    }

    private int weight(SeverityLevel level) {
        return switch (level) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }

    private ReportUrgency resolveUrgency(SeverityLevel highestSeverity, List<String> anomalies) {
        if (highestSeverity == SeverityLevel.CRITICAL || hasCriticalAnomaly(anomalies)) {
            return ReportUrgency.IMMEDIATE_STOP;
        }
        if (highestSeverity == SeverityLevel.HIGH) {
            return ReportUrgency.URGENT_SERVICE;
        }
        if (highestSeverity == SeverityLevel.MEDIUM || !anomalies.isEmpty()) {
            return ReportUrgency.SCHEDULE_SERVICE;
        }
        return ReportUrgency.MONITOR;
    }

    private boolean hasCriticalAnomaly(List<String> anomalies) {
        return anomalies.stream().anyMatch(anomaly ->
                anomaly.contains("engine overheating")
                        || anomaly.contains("critically low oil pressure")
        );
    }

    private List<String> collectReadingAnomalies(List<ObdReadingRequest> readings) {
        List<String> anomalies = new ArrayList<>();
        for (ObdReadingRequest reading : readings) {
            String name = reading.getParameterName().toLowerCase();
            double value = reading.getParameterValue().doubleValue();

            if ("engine_temperature".equals(name) && value > 105.0) {
                anomalies.add("engine overheating");
            }
            if ("battery_voltage".equals(name) && value < 11.8) {
                anomalies.add("low battery voltage");
            }
            if ("oil_pressure".equals(name) && value < 20.0) {
                anomalies.add("critically low oil pressure");
            }
        }
        return anomalies;
    }

    private String resolvePrimaryIssue(List<FaultCodeRequest> faultCodes, List<String> anomalies) {
        if (!faultCodes.isEmpty()) {
            FaultCodeRequest code = faultCodes.stream()
                    .max(Comparator.comparingInt(item -> weight(severityOrDefault(item))))
                    .orElse(faultCodes.get(0));
            return code.getCode() + ": " + code.getDescription();
        }
        if (!anomalies.isEmpty()) {
            return anomalies.get(0);
        }
        return "No major diagnostic issue detected";
    }

    private String resolveSummary(
            ReportUrgency urgency,
            List<FaultCodeRequest> faultCodes,
            List<String> anomalies
    ) {
        int faultCount = faultCodes.size();
        int anomalyCount = anomalies.size();

        return switch (urgency) {
            case MONITOR -> "Vehicle telemetry is currently stable. No urgent repair action is required.";
            case SCHEDULE_SERVICE -> "Diagnostic session found " + faultCount
                    + " fault codes and " + anomalyCount
                    + " telemetry anomalies. Maintenance should be scheduled.";
            case URGENT_SERVICE -> "Serious issues were detected. The vehicle should be inspected by service personnel as soon as possible.";
            case IMMEDIATE_STOP -> "Critical fault pattern detected. Continued driving may damage the vehicle or create a safety risk.";
        };
    }

    private List<String> resolveNextActions(
            ReportUrgency urgency,
            List<FaultCodeRequest> faultCodes,
            List<String> anomalies
    ) {
        List<String> actions = new ArrayList<>();

        switch (urgency) {
            case MONITOR -> {
                actions.add("Keep the vehicle under observation and repeat the OBD2 scan if symptoms return.");
                actions.add("Store the current session as a baseline for comparison.");
            }
            case SCHEDULE_SERVICE -> {
                actions.add("Book a maintenance visit and provide the saved diagnostic session to the technician.");
                actions.add("Inspect wiring, connectors, and related sensors before replacing parts.");
            }
            case URGENT_SERVICE -> {
                actions.add("Limit vehicle operation until a workshop confirms the root cause.");
                actions.add("Prepare VIN-based spare part lookup for the suspected subsystem.");
            }
            case IMMEDIATE_STOP -> {
                actions.add("Stop intensive driving and arrange immediate technical inspection.");
                actions.add("Consider vehicle towing if oil pressure or cooling system issues are present.");
            }
        }

        if (!faultCodes.isEmpty()) {
            actions.add("Export DTC codes for repair history: " + faultCodes.stream()
                    .map(FaultCodeRequest::getCode)
                    .distinct()
                    .limit(3)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse(""));
        }

        if (!anomalies.isEmpty()) {
            actions.add("Re-check abnormal telemetry values after connector and sensor inspection.");
        }

        return actions.stream().distinct().limit(5).toList();
    }

    private SeverityLevel severityOrDefault(FaultCodeRequest request) {
        return request.getSeverity() == null ? SeverityLevel.MEDIUM : request.getSeverity();
    }

    public record AiRecommendationDraft(
            RecommendationType type,
            String message,
            String actionLabel,
            String referenceCode
    ) {
    }

    public record ReportDraft(
            Integer healthScore,
            ReportUrgency urgency,
            Boolean drivable,
            Boolean towRecommended,
            String primaryIssue,
            String summary,
            List<String> nextActions
    ) {
    }
}
