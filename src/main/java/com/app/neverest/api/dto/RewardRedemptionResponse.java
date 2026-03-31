package com.app.neverest.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RewardRedemptionResponse(
        UUID id,
        UUID rewardId,
        UUID userId,
        String rewardTitle,
        int pointsSpent,
        String redemptionCode,
        LocalDateTime redeemedAt,
        int userAvailablePointsAfterRedemption
) {
}
