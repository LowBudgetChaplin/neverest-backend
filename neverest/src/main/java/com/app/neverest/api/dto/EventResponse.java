package com.app.neverest.api.dto;

import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.EventRecurrence;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        ActivityType activityType,
        String location,
        LocalDateTime startsAt,
        int pointsReward,
        Integer capacity,
        int attendeeCount,
        String description,
        EventRecurrence recurrence,
        String routeMapUrl,
        String stravaClubUrl,
        String whatsappGroupUrl
) {
}
