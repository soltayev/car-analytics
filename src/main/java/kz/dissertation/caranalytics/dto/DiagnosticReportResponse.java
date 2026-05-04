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
        LocalDateTime generatedAt,
        List<String> nextActions
) {
}
