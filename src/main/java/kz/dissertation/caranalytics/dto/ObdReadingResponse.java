package kz.dissertation.caranalytics.dto;

import java.math.BigDecimal;

public record ObdReadingResponse(
        String parameterName,
        String pidCode,
        BigDecimal parameterValue,
        String unit
) {
}
