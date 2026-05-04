package kz.dissertation.caranalytics.dto;

import java.math.BigDecimal;

public record ServiceCenterResponse(
        Long id,
        String name,
        String city,
        String address,
        String phone,
        String specialization,
        Boolean emergencySupport,
        BigDecimal rating,
        Double latitude,
        Double longitude,
        Double distanceKm
) {
}
