package com.app.neverest.api.dto;

import com.app.neverest.domain.ActivityType;
import java.time.LocalDateTime;

public record CreatePartnerChallengeRequest(
        String title,
        String description,
        ActivityType activityType,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String rewardKind,   // DISCOUNT / FREE_ITEM / SERVICE
        String rewardLabel,  // ex: "-20% la print"
        String brand,        // ex: "Mibe Print Studio"
        Double targetValue,
        String targetUnit
) {
}
