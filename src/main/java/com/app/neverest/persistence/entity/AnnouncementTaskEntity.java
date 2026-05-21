package com.app.neverest.persistence.entity;

import com.app.neverest.integration.AnnouncementChannel;
import com.app.neverest.integration.AnnouncementTaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "nev_announcement_tasks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_nev_announcement_tasks_event_channel",
                columnNames = {"event_id", "channel"}
        )
)
public class AnnouncementTaskEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 32)
    private AnnouncementChannel channel;

    @Column(name = "payload", nullable = false, length = 4000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AnnouncementTaskStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "last_status_code")
    private Integer lastStatusCode;

    @Column(name = "last_error", length = 700)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AnnouncementTaskEntity() {
    }

    public AnnouncementTaskEntity(
            UUID id,
            UUID eventId,
            AnnouncementChannel channel,
            String payload,
            int maxAttempts
    ) {
        this.id = id;
        this.eventId = eventId;
        this.channel = channel;
        this.payload = payload;
        this.status = AnnouncementTaskStatus.PENDING;
        this.attemptCount = 0;
        this.maxAttempts = maxAttempts;
        this.nextAttemptAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public AnnouncementChannel getChannel() {
        return channel;
    }

    public String getPayload() {
        return payload;
    }

    public AnnouncementTaskStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public Integer getLastStatusCode() {
        return lastStatusCode;
    }

    public String getLastError() {
        return lastError;
    }

    public void markSent(Integer statusCode, String detail) {
        this.status = AnnouncementTaskStatus.SENT;
        this.attemptCount += 1;
        this.lastAttemptAt = LocalDateTime.now();
        this.lastStatusCode = statusCode;
        this.lastError = detail;
        this.nextAttemptAt = this.lastAttemptAt;
        this.updatedAt = this.lastAttemptAt;
    }

    public void markSkipped(String detail) {
        this.status = AnnouncementTaskStatus.SKIPPED;
        this.lastAttemptAt = LocalDateTime.now();
        this.lastStatusCode = null;
        this.lastError = detail;
        this.nextAttemptAt = this.lastAttemptAt;
        this.updatedAt = this.lastAttemptAt;
    }

    public void markFailed(Integer statusCode, String detail, int backoffSeconds) {
        this.status = AnnouncementTaskStatus.FAILED;
        this.attemptCount += 1;
        this.lastAttemptAt = LocalDateTime.now();
        this.lastStatusCode = statusCode;
        this.lastError = detail;
        this.nextAttemptAt = this.lastAttemptAt.plusSeconds(Math.max(backoffSeconds, 1));
        this.updatedAt = this.lastAttemptAt;
    }

    public boolean canRetry() {
        return attemptCount < maxAttempts;
    }

    public void resetForManualRetry() {
        this.status = AnnouncementTaskStatus.PENDING;
        this.nextAttemptAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastError = "Manual retry requested.";
    }
}
