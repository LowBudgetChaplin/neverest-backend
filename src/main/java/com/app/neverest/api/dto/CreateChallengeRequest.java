package com.app.neverest.api.dto;

import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.ChallengeFrequency;
import com.app.neverest.domain.ChallengeMode;
import java.time.LocalDateTime;

public record CreateChallengeRequest(
        String title,
        String description,
        ActivityType activityType,
        ChallengeMode mode,
        ChallengeFrequency frequency,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Integer pointsReward,
        Double targetValue,
        String targetUnit
) {
}
