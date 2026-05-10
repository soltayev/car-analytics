package kz.dissertation.caranalytics;

import java.math.BigDecimal;
import java.util.List;
import kz.dissertation.caranalytics.dto.FaultCodeRequest;
import kz.dissertation.caranalytics.dto.ObdReadingRequest;
import kz.dissertation.caranalytics.model.ReportUrgency;
import kz.dissertation.caranalytics.model.SeverityLevel;
import kz.dissertation.caranalytics.service.DiagnosticAiAssistant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosticAiAssistantTest {

    private final DiagnosticAiAssistant diagnosticAiAssistant = new DiagnosticAiAssistant();

    @Test
    void buildReportMarksCriticalSessionAsImmediateStop() {
        ObdReadingRequest temperature = reading("engine_temperature", "05", "108", "C");
        FaultCodeRequest faultCode = faultCode("P0118", "Engine coolant temperature circuit high", SeverityLevel.CRITICAL);

        DiagnosticAiAssistant.ReportDraft report = diagnosticAiAssistant.buildReport(
                List.of(temperature),
                List.of(faultCode)
        );

        assertEquals(ReportUrgency.IMMEDIATE_STOP, report.urgency());
        assertFalse(report.drivable());
        assertTrue(report.towRecommended());
        assertTrue(report.healthScore() < 60);
        assertTrue(report.riskForecast().contains("overheating"));
    }

    @Test
    void buildReportMarksHealthySessionAsMonitor() {
        ObdReadingRequest rpm = reading("rpm", "0C", "850", "rpm");

        DiagnosticAiAssistant.ReportDraft report = diagnosticAiAssistant.buildReport(
                List.of(rpm),
                List.of()
        );

        assertEquals(ReportUrgency.MONITOR, report.urgency());
        assertTrue(report.drivable());
        assertFalse(report.towRecommended());
        assertEquals("No major diagnostic issue detected", report.primaryIssue());
        assertTrue(report.riskForecast().contains("baseline"));
    }

    private static ObdReadingRequest reading(String name, String pid, String value, String unit) {
        ObdReadingRequest request = new ObdReadingRequest();
        request.setParameterName(name);
        request.setPidCode(pid);
        request.setParameterValue(new BigDecimal(value));
        request.setUnit(unit);
        return request;
    }

    private static FaultCodeRequest faultCode(String code, String description, SeverityLevel severity) {
        FaultCodeRequest request = new FaultCodeRequest();
        request.setCode(code);
        request.setDescription(description);
        request.setSeverity(severity);
        return request;
    }
}
