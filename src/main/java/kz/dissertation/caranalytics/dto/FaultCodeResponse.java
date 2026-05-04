package kz.dissertation.caranalytics.dto;

import kz.dissertation.caranalytics.model.SeverityLevel;

public record FaultCodeResponse(
        String code,
        String description,
        SeverityLevel severity
) {
}
