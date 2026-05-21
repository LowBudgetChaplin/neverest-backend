package com.app.neverest.persistence.entity;

import com.app.neverest.domain.ActivityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nev_events")
public class EventEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 32)
    private ActivityType activityType;

    @Column(name = "location", nullable = false, length = 200)
    private String location;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "points_reward", nullable = false)
    private int pointsReward;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected EventEntity() {
    }

    public EventEntity(
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

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public int getPointsReward() {
        return pointsReward;
    }
}
