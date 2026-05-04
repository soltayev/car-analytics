package kz.dissertation.caranalytics.dto;

public record VehicleInfoItemResponse(
        String infoKey,
        String infoValue,
        String sourceMode
) {
}
