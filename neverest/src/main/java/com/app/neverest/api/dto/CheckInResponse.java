package com.app.neverest.api.dto;

import java.util.UUID;

public record CheckInResponse(
        UUID eventId,
        UUID userId,
        String userName,
        String userAvatarB64,
        int pointsAwarded,
        int updatedTotalPoints,
        int checkInCount,
        Integer capacity
) {
}
