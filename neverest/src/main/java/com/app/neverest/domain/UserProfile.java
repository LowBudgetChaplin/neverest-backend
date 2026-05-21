package com.app.neverest.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class UserProfile {

    private final UUID id;
    private final String displayName;
    private final String qrCode;
    private final String authSubject;
    private int totalPoints;
    private int availablePoints;
    private final EnumMap<ActivityType, Integer> pointsByActivity;

    public UserProfile(UUID id, String displayName, String qrCode) {
        this(id, displayName, qrCode, null, 0, 0, 0, 0, 0);
    }

    public UserProfile(UUID id, String displayName, String qrCode, String authSubject) {
        this(id, displayName, qrCode, authSubject, 0, 0, 0, 0, 0);
    }

    public UserProfile(
            UUID id,
            String displayName,
            String qrCode,
            String authSubject,
            int totalPoints,
            int availablePoints,
            int pointsPadel,
            int pointsMountain,
            int pointsRunning
    ) {
        this.id = id;
        this.displayName = displayName;
        this.qrCode = qrCode;
        this.authSubject = authSubject;
        this.totalPoints = totalPoints;
        this.availablePoints = availablePoints;
        this.pointsByActivity = new EnumMap<>(ActivityType.class);
        pointsByActivity.put(ActivityType.PADEL, pointsPadel);
        pointsByActivity.put(ActivityType.MOUNTAIN, pointsMountain);
        pointsByActivity.put(ActivityType.RUNNING, pointsRunning);
    }

    public UUID id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String qrCode() {
        return qrCode;
    }

    public String authSubject() {
        return authSubject;
    }

    public synchronized int totalPoints() {
        return totalPoints;
    }

    public synchronized int availablePoints() {
        return availablePoints;
    }

    public Map<ActivityType, Integer> pointsByActivity() {
        return Map.copyOf(pointsByActivity);
    }

    public int pointsForActivity(ActivityType activityType) {
        return pointsByActivity.getOrDefault(activityType, 0);
    }

    public synchronized void awardPoints(ActivityType activityType, int points) {
        totalPoints += points;
        availablePoints += points;
        pointsByActivity.merge(activityType, points, Integer::sum);
    }

    public synchronized boolean spendPoints(int points) {
        if (points <= 0) {
            return false;
        }
        if (availablePoints < points) {
            return false;
        }

        availablePoints -= points;
        return true;
    }
}
