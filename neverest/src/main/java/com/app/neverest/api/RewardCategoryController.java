package com.app.neverest.api;

import com.app.neverest.api.dto.RewardCategoryResponse;
import com.app.neverest.persistence.repository.RewardCategoryRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reward-categories")
public class RewardCategoryController {

    private final RewardCategoryRepository repository;

    public RewardCategoryController(RewardCategoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<RewardCategoryResponse> getCategories() {
        return repository.findAllByOrderBySortOrderAsc().stream()
                .map(c -> new RewardCategoryResponse(c.getCode(), c.getLabelEn(), c.getLabelRo()))
                .toList();
    }
}
