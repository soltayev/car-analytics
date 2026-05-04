package kz.dissertation.caranalytics.dto;

import kz.dissertation.caranalytics.model.SeverityLevel;

public record FaultCodeDictionaryResponse(
        Long id,
        String code,
        String title,
        String description,
        String systemName,
        String subsystem,
        Boolean manufacturerSpecific,
        SeverityLevel defaultSeverity,
        String possibleCauses,
        String recommendedActions,
        Boolean drivableAllowed
) {
}
