package com.app.neverest.persistence.repository;

import com.app.neverest.integration.AnnouncementChannel;
import com.app.neverest.integration.AnnouncementTaskStatus;
import com.app.neverest.persistence.entity.AnnouncementTaskEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnouncementTaskRepository extends JpaRepository<AnnouncementTaskEntity, UUID> {

    Optional<AnnouncementTaskEntity> findByEventIdAndChannel(UUID eventId, AnnouncementChannel channel);

    List<AnnouncementTaskEntity> findByEventIdOrderByChannelAsc(UUID eventId);

    @Query(
            "select t from AnnouncementTaskEntity t "
                    + "where t.status in :statuses and t.nextAttemptAt <= :now "
                    + "order by t.nextAttemptAt asc"
    )
    List<AnnouncementTaskEntity> findDueTasks(
            @Param("statuses") Collection<AnnouncementTaskStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
