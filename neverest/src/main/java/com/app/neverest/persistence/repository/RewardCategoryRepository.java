package com.app.neverest.persistence.repository;

import com.app.neverest.persistence.entity.RewardCategoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardCategoryRepository extends JpaRepository<RewardCategoryEntity, String> {

    List<RewardCategoryEntity> findAllByOrderBySortOrderAsc();
}
