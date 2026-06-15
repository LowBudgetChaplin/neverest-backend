package com.app.neverest.persistence.entity;

import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.ChallengeFrequency;
import com.app.neverest.domain.ChallengeMode;
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
@Table(name = "nev_challenges")
public class ChallengeEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "description", nullable = false, length = 600)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 32)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 32)
    private ChallengeMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 32)
    private ChallengeFrequency frequency;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "points_reward", nullable = false)
    private int pointsReward;

    @Column(name = "target_value")
    private Double targetValue;

    @Column(name = "target_unit", length = 64)
    private String targetUnit;

    // Partner-owned challenges: owner = partner user; reward is a benefit, not points.
    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    @Column(name = "reward_kind", length = 32)
    private String rewardKind; // POINTS / DISCOUNT / FREE_ITEM / SERVICE

    @Column(name = "reward_label", length = 200)
    private String rewardLabel;

    @Column(name = "brand", length = 120)
    private String brand;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ChallengeEntity() {
    }

    public ChallengeEntity(
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

    public String getDescription() {
        return description;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public ChallengeMode getMode() {
        return mode;
    }

    public ChallengeFrequency getFrequency() {
        return frequency;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public int getPointsReward() {
        return pointsReward;
    }

    public Double getTargetValue() {
        return targetValue;
    }

    public String getTargetUnit() {
        return targetUnit;
    }

    public UUID getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(UUID ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getRewardKind() { return rewardKind; }
    public void setRewardKind(String rewardKind) { this.rewardKind = rewardKind; }
    public String getRewardLabel() { return rewardLabel; }
    public void setRewardLabel(String rewardLabel) { this.rewardLabel = rewardLabel; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    // Setters used when a partner edits their own challenge.
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }
}
