package com.app.neverest.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String body,
        boolean read,
        UUID challengeId,
        UUID submissionId,
        LocalDateTime createdAt
) {
}
