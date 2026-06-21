package com.app.neverest.api.dto;

import com.app.neverest.domain.ActivityType;
import java.time.LocalDateTime;

public record UpdateChallengeRequest(
        String title,
        String description,
        ActivityType activityType,
        Integer pointsReward,
        Double targetValue,
        String targetUnit,
        LocalDateTime startsAt,
        LocalDateTime endsAt
) {
}
