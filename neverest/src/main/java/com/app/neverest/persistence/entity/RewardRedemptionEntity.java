package com.app.neverest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nev_reward_redemptions")
public class RewardRedemptionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "reward_id", nullable = false)
    private UUID rewardId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reward_title", nullable = false, length = 180)
    private String rewardTitle;

    @Column(name = "points_spent", nullable = false)
    private int pointsSpent;

    @Column(name = "redemption_code", nullable = false, length = 40, unique = true)
    private String redemptionCode;

    @Column(name = "redeemed_at", nullable = false)
    private LocalDateTime redeemedAt;

    @Column(name = "user_available_points_after_redemption", nullable = false)
    private int userAvailablePointsAfterRedemption;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    protected RewardRedemptionEntity() {
    }

    public RewardRedemptionEntity(
            UUID id,
            UUID rewardId,
            UUID userId,
            String rewardTitle,
            int pointsSpent,
            String redemptionCode,
            LocalDateTime redeemedAt,
            int userAvailablePointsAfterRedemption
    ) {
        this.id = id;
        this.rewardId = rewardId;
        this.userId = userId;
        this.rewardTitle = rewardTitle;
        this.pointsSpent = pointsSpent;
        this.redemptionCode = redemptionCode;
        this.redeemedAt = redeemedAt;
        this.userAvailablePointsAfterRedemption = userAvailablePointsAfterRedemption;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRewardId() {
        return rewardId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRewardTitle() {
        return rewardTitle;
    }

    public int getPointsSpent() {
        return pointsSpent;
    }

    public String getRedemptionCode() {
        return redemptionCode;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public int getUserAvailablePointsAfterRedemption() {
        return userAvailablePointsAfterRedemption;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public void consume() {
        this.consumedAt = LocalDateTime.now();
    }
}
