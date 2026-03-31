package com.app.neverest.api.dto;

import java.util.UUID;

public record CheckInResponse(
        UUID eventId,
        UUID userId,
        int pointsAwarded,
        int updatedTotalPoints
) {
}
