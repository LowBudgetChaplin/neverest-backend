package com.app.neverest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "nev_event_checkins",
        uniqueConstraints = @UniqueConstraint(name = "uk_nev_event_checkins_event_user", columnNames = {"event_id", "user_id"})
)
public class EventCheckInEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "checked_in_at", nullable = false)
    private LocalDateTime checkedInAt;

    protected EventCheckInEntity() {
    }

    public EventCheckInEntity(UUID id, UUID eventId, UUID userId) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
    }

    @PrePersist
    void onCreate() {
        if (checkedInAt == null) {
            checkedInAt = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getUserId() {
        return userId;
    }
}
