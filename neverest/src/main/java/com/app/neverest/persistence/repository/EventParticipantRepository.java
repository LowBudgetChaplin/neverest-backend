package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.EventParticipantEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventParticipantRepository extends JpaRepository<EventParticipantEntity, UUID> {

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    long countByEventId(UUID eventId);

    List<EventParticipantEntity> findByEventIdOrderByJoinedAtAsc(UUID eventId);

    @Modifying
    @Query("delete from EventParticipantEntity e where e.eventId = :eventId and e.userId = :userId")
    void deleteByEventIdAndUserId(@Param("eventId") UUID eventId, @Param("userId") UUID userId);

    @Modifying
    @Query("delete from EventParticipantEntity e where e.eventId = :eventId")
    void deleteByEventId(@Param("eventId") UUID eventId);
}
