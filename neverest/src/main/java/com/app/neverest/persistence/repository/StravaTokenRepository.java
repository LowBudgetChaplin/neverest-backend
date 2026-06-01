package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.StravaTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StravaTokenRepository extends JpaRepository<StravaTokenEntity, UUID> {
    Optional<StravaTokenEntity> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
