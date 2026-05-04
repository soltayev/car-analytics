package kz.dissertation.caranalytics.dto;

import java.time.LocalDateTime;

public record RawObdFrameResponse(
        String mode,
        String pid,
        String rawResponse,
        String decodedLabel,
        Boolean manufacturerSpecific,
        LocalDateTime frameTimestamp
) {
}
