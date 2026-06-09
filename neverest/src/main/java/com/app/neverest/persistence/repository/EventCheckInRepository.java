package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.EventCheckInEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventCheckInRepository extends JpaRepository<EventCheckInEntity, UUID> {

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    long countByEventId(UUID eventId);
}
