package com.app.neverest.api.dto;

import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.ChallengeFrequency;
import com.app.neverest.domain.ChallengeMode;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChallengeResponse(
        UUID id,
        String title,
        String description,
        ActivityType activityType,
        ChallengeMode mode,
        ChallengeFrequency frequency,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        int pointsReward,
        Double targetValue,
        String targetUnit
) {
}
