package kz.dissertation.caranalytics.dto;

import java.util.List;
import kz.dissertation.caranalytics.model.RecommendationType;

public record RecommendationResponse(
        RecommendationType type,
        String message,
        String messageRu,
        String messageEn,
        String actionLabel,
        String actionLabelRu,
        String actionLabelEn,
        String referenceCode,
        List<ServiceCenterResponse> serviceCenters,
        List<SparePartResponse> spareParts,
        List<RepairGuideResponse> repairGuides
) {
}
