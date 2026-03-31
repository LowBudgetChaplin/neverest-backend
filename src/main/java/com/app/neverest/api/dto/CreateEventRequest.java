package com.app.neverest.api.dto;

import com.app.neverest.domain.ActivityType;
import java.time.LocalDateTime;

public record CreateEventRequest(
        String title,
        ActivityType activityType,
        String location,
        LocalDateTime startsAt,
        Integer pointsReward
) {
}
