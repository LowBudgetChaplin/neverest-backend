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
        name = "nev_event_participants",
        uniqueConstraints = @UniqueConstraint(name = "uk_nev_event_participants_event_user", columnNames = {"event_id", "user_id"})
)
public class EventParticipantEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    protected EventParticipantEntity() {
    }

    public EventParticipantEntity(UUID id, UUID eventId, UUID userId) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
    }

    @PrePersist
    void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
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

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
