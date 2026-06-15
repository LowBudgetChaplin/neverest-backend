package com.app.neverest.api.dto;

import java.time.LocalDateTime;

public record CreateOfferRequest(
        String brand,
        String title,
        String description,
        String discountLabel,
        String imageB64,
        String linkUrl,
        Boolean active,
        LocalDateTime validFrom,
        LocalDateTime validUntil
) {
}
