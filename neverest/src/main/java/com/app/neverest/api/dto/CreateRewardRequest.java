package com.app.neverest.api.dto;

public record CreateRewardRequest(
        String title,
        String partnerName,
        String description,
        Integer pointsCost,
        Integer stock,
        Integer rotationDays,
        String category
) {
}
