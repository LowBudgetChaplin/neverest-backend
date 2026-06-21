package com.app.neverest.persistence.entity;

import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.EventRecurrence;
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

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "description", length = 700)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence", nullable = false, length = 32)
    private EventRecurrence recurrence = EventRecurrence.NONE;

    @Column(name = "route_map_url", length = 500)
    private String routeMapUrl;

    @Column(name = "strava_club_url", length = 300)
    private String stravaClubUrl;

    @Column(name = "whatsapp_group_url", length = 300)
    private String whatsappGroupUrl;

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
        this(id, title, activityType, location, startsAt, pointsReward,
                null, null, EventRecurrence.NONE, null, null, null);
    }

    public EventEntity(
            UUID id,
            String title,
            ActivityType activityType,
            String location,
            LocalDateTime startsAt,
            int pointsReward,
            Integer capacity,
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
        this.description = description;
        this.recurrence = recurrence != null ? recurrence : EventRecurrence.NONE;
        this.routeMapUrl = routeMapUrl;
        this.stravaClubUrl = stravaClubUrl;
        this.whatsappGroupUrl = whatsappGroupUrl;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (recurrence == null) {
            recurrence = EventRecurrence.NONE;
        }
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public ActivityType getActivityType() { return activityType; }
    public String getLocation() { return location; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public int getPointsReward() { return pointsReward; }
    public Integer getCapacity() { return capacity; }
    public String getDescription() { return description; }
    public EventRecurrence getRecurrence() { return recurrence; }
    public String getRouteMapUrl() { return routeMapUrl; }
    public String getStravaClubUrl() { return stravaClubUrl; }
    public String getWhatsappGroupUrl() { return whatsappGroupUrl; }

    public void setTitle(String title) { this.title = title; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
    public void setLocation(String location) { this.location = location; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public void setPointsReward(int pointsReward) { this.pointsReward = pointsReward; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setDescription(String description) { this.description = description; }
    public void setRecurrence(EventRecurrence recurrence) {
        this.recurrence = recurrence != null ? recurrence : EventRecurrence.NONE;
    }
    public void setRouteMapUrl(String routeMapUrl) { this.routeMapUrl = routeMapUrl; }
    public void setStravaClubUrl(String stravaClubUrl) { this.stravaClubUrl = stravaClubUrl; }
    public void setWhatsappGroupUrl(String whatsappGroupUrl) { this.whatsappGroupUrl = whatsappGroupUrl; }
}
