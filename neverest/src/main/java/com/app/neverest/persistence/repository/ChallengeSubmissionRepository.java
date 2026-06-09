package com.app.neverest.persistence.repository;

import com.app.neverest.domain.ChallengeSubmissionStatus;
import com.app.neverest.persistence.entity.ChallengeSubmissionEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeSubmissionRepository extends JpaRepository<ChallengeSubmissionEntity, UUID> {

    boolean existsByChallengeIdAndUserId(UUID challengeId, UUID userId);

    List<ChallengeSubmissionEntity> findByUserIdAndStatus(UUID userId, ChallengeSubmissionStatus status);

    List<ChallengeSubmissionEntity> findByChallengeIdOrderBySubmittedAtDesc(UUID challengeId);

    List<ChallengeSubmissionEntity> findByChallengeIdAndUserIdOrderBySubmittedAtDesc(UUID challengeId, UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ChallengeSubmissionEntity s where s.id = :id and s.challengeId = :challengeId")
    Optional<ChallengeSubmissionEntity> findByIdAndChallengeIdForUpdate(
            @Param("id") UUID id,
            @Param("challengeId") UUID challengeId
    );
}
