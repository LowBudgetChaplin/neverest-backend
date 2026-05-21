package com.app.neverest.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Challenge {

    private final UUID id;
    private final String title;
    private final String description;
    private final ActivityType activityType;
    private final ChallengeMode mode;
    private final ChallengeFrequency frequency;
    private final LocalDateTime startsAt;
    private final LocalDateTime endsAt;
    private final int pointsReward;
    private final Double targetValue;
    private final String targetUnit;

    public Challenge(
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
        this.id = id;
        this.title = title;
        this.description = description;
        this.activityType = activityType;
        this.mode = mode;
        this.frequency = frequency;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.pointsReward = pointsReward;
        this.targetValue = targetValue;
        this.targetUnit = targetUnit;
    }

    public UUID id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public ActivityType activityType() {
        return activityType;
    }

    public ChallengeMode mode() {
        return mode;
    }

    public ChallengeFrequency frequency() {
        return frequency;
    }

    public LocalDateTime startsAt() {
        return startsAt;
    }

    public LocalDateTime endsAt() {
        return endsAt;
    }

    public int pointsReward() {
        return pointsReward;
    }

    public Double targetValue() {
        return targetValue;
    }

    public String targetUnit() {
        return targetUnit;
    }
}
