package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.NotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findTop100ByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndReadFalse(UUID userId);

    @Modifying
    @Query("update NotificationEntity n set n.read = true where n.userId = :userId and n.read = false")
    int markAllRead(@Param("userId") UUID userId);

    @Modifying
    @Query("delete from NotificationEntity n where n.challengeId = :challengeId")
    void deleteByChallengeId(@Param("challengeId") UUID challengeId);
}
