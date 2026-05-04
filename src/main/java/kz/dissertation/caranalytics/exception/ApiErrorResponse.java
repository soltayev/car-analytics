package kz.dissertation.caranalytics.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        Map<String, String> validationErrors
) {
}
