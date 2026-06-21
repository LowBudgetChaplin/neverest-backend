package com.app.neverest.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Event {

    private final UUID id;
    private final String title;
    private final ActivityType activityType;
    private final String location;
    private final LocalDateTime startsAt;
    private final int pointsReward;
    private final Integer capacity;
    private final int attendeeCount;
    private final int participantCount;
    private final String description;
    private final EventRecurrence recurrence;
    private final String routeMapUrl;
    private final String stravaClubUrl;
    private final String whatsappGroupUrl;

    public Event(
            UUID id,
            String title,
            ActivityType activityType,
            String location,
            LocalDateTime startsAt,
            int pointsReward,
            Integer capacity,
            int attendeeCount,
            int participantCount,
            String description,
            EventRecurrence recurrence,
            String routeMapUrl,
            String stravaClubUrl,
            String whatsappGroupUrl
    ) {
        this.id = id;
        this.title = title;
        this.activityType = activityType;
        this.location = location;
        this.startsAt = startsAt;
        this.pointsReward = pointsReward;
        this.capacity = capacity;
        this.attendeeCount = attendeeCount;
        this.participantCount = participantCount;
        this.description = description;
        this.recurrence = recurrence;
        this.routeMapUrl = routeMapUrl;
        this.stravaClubUrl = stravaClubUrl;
        this.whatsappGroupUrl = whatsappGroupUrl;
    }

    public UUID id() { return id; }
    public String title() { return title; }
    public ActivityType activityType() { return activityType; }
    public String location() { return location; }
    public LocalDateTime startsAt() { return startsAt; }
    public int pointsReward() { return pointsReward; }
    public Integer capacity() { return capacity; }
    public int attendeeCount() { return attendeeCount; }
    public int participantCount() { return participantCount; }
    public String description() { return description; }
    public EventRecurrence recurrence() { return recurrence; }
    public String routeMapUrl() { return routeMapUrl; }
    public String stravaClubUrl() { return stravaClubUrl; }
    public String whatsappGroupUrl() { return whatsappGroupUrl; }
}
