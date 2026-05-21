package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.RewardEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RewardRepository extends JpaRepository<RewardEntity, UUID> {

    List<RewardEntity> findAllByOrderByTitleAsc();

    List<RewardEntity> findByActiveTrueOrderByTitleAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RewardEntity r where r.id = :id")
    Optional<RewardEntity> findByIdForUpdate(@Param("id") UUID id);
}
