package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.ChallengeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<ChallengeEntity, UUID> {

    List<ChallengeEntity> findAllByOrderByStartsAtAsc();
}
