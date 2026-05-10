package kz.dissertation.caranalytics.dto;

import java.time.LocalDateTime;
import java.util.List;
import kz.dissertation.caranalytics.model.ReportUrgency;

public record DiagnosticReportResponse(
        Long id,
        Integer healthScore,
        ReportUrgency urgency,
        Boolean drivable,
        Boolean towRecommended,
        String primaryIssue,
        String summary,
        String summaryRu,
        String summaryEn,
        String riskForecast,
        String riskForecastRu,
        String riskForecastEn,
        LocalDateTime generatedAt,
        List<String> nextActions,
        List<String> nextActionsRu,
        List<String> nextActionsEn
) {
}
