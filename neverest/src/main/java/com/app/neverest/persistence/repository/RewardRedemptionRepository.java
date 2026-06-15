package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.RewardRedemptionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRedemptionRepository extends JpaRepository<RewardRedemptionEntity, UUID> {

    List<RewardRedemptionEntity> findAllByOrderByRedeemedAtDesc();

    List<RewardRedemptionEntity> findByUserIdOrderByRedeemedAtDesc(UUID userId);

    java.util.Optional<RewardRedemptionEntity>
        findFirstByRewardIdAndUserIdOrderByRedeemedAtDesc(UUID rewardId, UUID userId);
}
