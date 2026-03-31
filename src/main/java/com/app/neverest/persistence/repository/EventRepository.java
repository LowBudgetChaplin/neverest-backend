package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.EventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    List<EventEntity> findAllByOrderByStartsAtAsc();
}
