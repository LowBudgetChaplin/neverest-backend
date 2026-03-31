package com.app.neverest.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class RewardRedemption {

    private final UUID id;
    private final UUID rewardId;
    private final UUID userId;
    private final String rewardTitle;
    private final int pointsSpent;
    private final String redemptionCode;
    private final LocalDateTime redeemedAt;
    private final int userAvailablePointsAfterRedemption;

    public RewardRedemption(
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

    public UUID id() {
        return id;
    }

    public UUID rewardId() {
        return rewardId;
    }

    public UUID userId() {
        return userId;
    }

    public String rewardTitle() {
        return rewardTitle;
    }

    public int pointsSpent() {
        return pointsSpent;
    }

    public String redemptionCode() {
        return redemptionCode;
    }

    public LocalDateTime redeemedAt() {
        return redeemedAt;
    }

    public int userAvailablePointsAfterRedemption() {
        return userAvailablePointsAfterRedemption;
    }
}
