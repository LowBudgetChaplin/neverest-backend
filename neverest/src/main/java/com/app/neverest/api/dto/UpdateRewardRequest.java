package com.app.neverest.api.dto;

public record UpdateRewardRequest(
        String title,
        String partnerName,
        String description,
        Integer pointsCost,
        Integer stock,
        Boolean clearStock,
        String address,
        String imageB64,
        Boolean clearImage,
        String category
) {
}
