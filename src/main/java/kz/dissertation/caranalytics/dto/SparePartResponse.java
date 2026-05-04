package kz.dissertation.caranalytics.dto;

import java.math.BigDecimal;
import kz.dissertation.caranalytics.model.SparePartStockStatus;

public record SparePartResponse(
        Long id,
        String partNumber,
        String partName,
        String manufacturer,
        String subsystem,
        String compatibleBrand,
        String compatibleModel,
        String faultCode,
        BigDecimal price,
        String currency,
        SparePartStockStatus stockStatus
) {
}
