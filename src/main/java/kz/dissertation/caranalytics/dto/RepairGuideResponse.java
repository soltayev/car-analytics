package kz.dissertation.caranalytics.dto;

import kz.dissertation.caranalytics.model.RepairDifficulty;

public record RepairGuideResponse(
        Long id,
        String title,
        String subsystem,
        String faultCode,
        String description,
        RepairDifficulty difficulty,
        Integer estimatedMinutes,
        String safetyNotes
) {
}
