package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.EventCheckInEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventCheckInRepository extends JpaRepository<EventCheckInEntity, UUID> {

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    long countByEventId(UUID eventId);

    @Query("select e.userId from EventCheckInEntity e where e.eventId = :eventId")
    List<UUID> findUserIdsByEventId(@Param("eventId") UUID eventId);

    @Modifying
    @Query("delete from EventCheckInEntity e where e.eventId = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);
}
