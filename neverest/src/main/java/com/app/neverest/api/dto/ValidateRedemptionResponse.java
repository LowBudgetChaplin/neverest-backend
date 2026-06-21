package com.app.neverest.api.dto;

import java.time.LocalDateTime;

public record ValidateRedemptionResponse(
        boolean valid,
        String status,
        String rewardTitle,
        String userName,
        String code,
        LocalDateTime consumedAt
) {
}
