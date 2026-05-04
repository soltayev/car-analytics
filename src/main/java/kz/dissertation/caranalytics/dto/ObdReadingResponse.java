package kz.dissertation.caranalytics.dto;

import java.math.BigDecimal;

public record ObdReadingResponse(
        String parameterName,
        String pidCode,
        String sourceMode,
        Boolean freezeFrame,
        Boolean manufacturerSpecific,
        String description,
        BigDecimal parameterValue,
        String unit
) {
}
