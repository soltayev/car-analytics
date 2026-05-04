package kz.dissertation.caranalytics.dto;

import kz.dissertation.caranalytics.model.FaultCodeType;
import kz.dissertation.caranalytics.model.SeverityLevel;

public record FaultCodeResponse(
        String code,
        String description,
        FaultCodeType faultCodeType,
        String sourceMode,
        Boolean manufacturerSpecific,
        SeverityLevel severity,
        FaultCodeDictionaryResponse dictionaryEntry
) {
}
