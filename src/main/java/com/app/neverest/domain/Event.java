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

    public Event(
            UUID id,
            String title,
            ActivityType activityType,
            String location,
            LocalDateTime startsAt,
            int pointsReward
    ) {
        this.id = id;
        this.title = title;
        this.activityType = activityType;
        this.location = location;
        this.startsAt = startsAt;
        this.pointsReward = pointsReward;
    }

    public UUID id() {
        return id;
    }

    public String title() {
        return title;
    }

    public ActivityType activityType() {
        return activityType;
    }

    public String location() {
        return location;
    }

    public LocalDateTime startsAt() {
        return startsAt;
    }

    public int pointsReward() {
        return pointsReward;
    }
}
