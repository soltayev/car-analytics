package kz.dissertation.caranalytics.dto;

import kz.dissertation.caranalytics.model.SeverityLevel;

public record FaultCodeDictionaryResponse(
        Long id,
        String code,
        String title,
        String titleRu,
        String titleEn,
        String description,
        String descriptionRu,
        String descriptionEn,
        String systemName,
        String systemNameRu,
        String systemNameEn,
        String subsystem,
        String subsystemRu,
        String subsystemEn,
        Boolean manufacturerSpecific,
        SeverityLevel defaultSeverity,
        String possibleCauses,
        String possibleCausesRu,
        String possibleCausesEn,
        String recommendedActions,
        String recommendedActionsRu,
        String recommendedActionsEn,
        Boolean drivableAllowed
) {
}
