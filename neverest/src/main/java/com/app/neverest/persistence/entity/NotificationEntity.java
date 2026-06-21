package com.app.neverest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nev_notifications")
public class NotificationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "type", nullable = false, length = 40)
    private String type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", nullable = false, length = 600)
    private String body;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "challenge_id")
    private UUID challengeId;

    @Column(name = "submission_id")
    private UUID submissionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected NotificationEntity() {
    }

    public NotificationEntity(
            UUID id,
            UUID userId,
            String type,
            String title,
            String body,
            UUID challengeId,
            UUID submissionId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.read = false;
        this.challengeId = challengeId;
        this.submissionId = submissionId;
        this.createdAt = createdAt;
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

    public UUID getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public boolean isRead() {
        return read;
    }

    public UUID getChallengeId() {
        return challengeId;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void markRead() {
        this.read = true;
    }
}
