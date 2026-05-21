package com.app.neverest.api.dto;

import com.app.neverest.domain.ActivityType;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        ActivityType activityType,
        String location,
        LocalDateTime startsAt,
        int pointsReward
) {
}
