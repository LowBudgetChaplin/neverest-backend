package com.app.neverest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "nev_audit_logs")
public class AuditLogEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "action", nullable = false, length = 80)
    private String action;

    @Column(name = "actor", nullable = false, length = 220)
    private String actor;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "message", nullable = false, length = 700)
    private String message;

    @Column(name = "metadata_encoded", nullable = false, length = 4000)
    private String metadataEncoded;

    protected AuditLogEntity() {
    }

    public AuditLogEntity(
            UUID id,
            Instant createdAt,
            String action,
            String actor,
            boolean success,
            String message,
            String metadataEncoded
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.action = action;
        this.actor = actor;
        this.success = success;
        this.message = message;
        this.metadataEncoded = metadataEncoded;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getAction() {
        return action;
    }

    public String getActor() {
        return actor;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getMetadataEncoded() {
        return metadataEncoded;
    }
}
