package kz.dissertation.caranalytics.dto;

public record VehicleResponse(
        Long id,
        String vin,
        String brand,
        String model,
        Integer productionYear,
        String engineType
) {
}
