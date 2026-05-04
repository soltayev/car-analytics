package kz.dissertation.caranalytics.dto;

import kz.dissertation.caranalytics.model.RecommendationType;

public record RecommendationResponse(
        RecommendationType type,
        String message,
        String actionLabel
) {
}
