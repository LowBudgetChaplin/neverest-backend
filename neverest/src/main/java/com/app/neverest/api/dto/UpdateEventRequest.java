package com.app.neverest.api.dto;

import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.EventRecurrence;
import java.time.LocalDateTime;

public record UpdateEventRequest(
        String title,
        ActivityType activityType,
        String location,
        LocalDateTime startsAt,
        Integer pointsReward,
        Integer capacity,
        Boolean clearCapacity,
        String description,
        EventRecurrence recurrence,
        String routeMapUrl,
        String stravaClubUrl,
        String whatsappGroupUrl
) {
}
