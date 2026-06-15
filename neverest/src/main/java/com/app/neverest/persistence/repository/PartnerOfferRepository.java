package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.PartnerOfferEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerOfferRepository extends JpaRepository<PartnerOfferEntity, UUID> {

    List<PartnerOfferEntity> findByActiveTrueOrderByCreatedAtDesc();

    List<PartnerOfferEntity> findByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId);
}
