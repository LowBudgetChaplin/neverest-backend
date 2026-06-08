package com.app.neverest.persistence.entity;

import com.app.neverest.domain.ActivityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nev_users")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "avatar_b64", columnDefinition = "MEDIUMTEXT")
    private String avatarB64;

    @Column(name = "qr_code", nullable = false, length = 32, unique = true)
    private String qrCode;

    @Column(name = "auth_subject", length = 200, unique = true)
    private String authSubject;

    @Column(name = "password_hash", length = 120)
    private String passwordHash;

    @Column(name = "role", nullable = false, length = 24)
    private String role;

    @Column(name = "total_points", nullable = false)
    private int totalPoints;

    @Column(name = "available_points", nullable = false)
    private int availablePoints;

    @Column(name = "points_padel", nullable = false)
    private int pointsPadel;

    @Column(name = "points_mountain", nullable = false)
    private int pointsMountain;

    @Column(name = "points_running", nullable = false)
    private int pointsRunning;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected UserEntity() {
    }

    public UserEntity(UUID id, String displayName, String qrCode, String authSubject) {
        this(id, displayName, qrCode, authSubject, null, "USER");
    }

    public UserEntity(
            UUID id,
            String displayName,
            String qrCode,
            String authSubject,
            String passwordHash,
            String role
    ) {
        this.id = id;
        this.displayName = displayName;
        this.qrCode = qrCode;
        this.authSubject = authSubject;
        this.passwordHash = passwordHash;
        this.role = role == null || role.isBlank() ? "USER" : role.trim().toUpperCase();
        this.totalPoints = 0;
        this.availablePoints = 0;
        this.pointsPadel = 0;
        this.pointsMountain = 0;
        this.pointsRunning = 0;
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

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatarB64() {
        return avatarB64;
    }

    public void setAvatarB64(String avatarB64) {
        this.avatarB64 = avatarB64;
    }

    public String getQrCode() {
        return qrCode;
    }

    public String getAuthSubject() {
        return authSubject;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(String role) {
        this.role = role == null || role.isBlank() ? "USER" : role.trim().toUpperCase();
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getAvailablePoints() {
        return availablePoints;
    }

    public int getPointsPadel() {
        return pointsPadel;
    }

    public int getPointsMountain() {
        return pointsMountain;
    }

    public int getPointsRunning() {
        return pointsRunning;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int pointsFor(ActivityType activityType) {
        if (activityType == ActivityType.PADEL) {
            return pointsPadel;
        }
        if (activityType == ActivityType.MOUNTAIN) {
            return pointsMountain;
        }
        return pointsRunning;
    }

    public void awardPoints(ActivityType activityType, int points) {
        this.totalPoints += points;
        this.availablePoints += points;

        if (activityType == ActivityType.PADEL) {
            this.pointsPadel += points;
        } else if (activityType == ActivityType.MOUNTAIN) {
            this.pointsMountain += points;
        } else {
            this.pointsRunning += points;
        }
    }

    public boolean spendPoints(int points) {
        if (points <= 0 || availablePoints < points) {
            return false;
        }

        availablePoints -= points;
        return true;
    }
}
