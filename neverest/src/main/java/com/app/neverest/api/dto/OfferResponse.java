package com.app.neverest.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OfferResponse(
        UUID id,
        UUID ownerUserId,
        String brand,
        String title,
        String description,
        String discountLabel,
        String imageB64,
        String linkUrl,
        boolean active,
        LocalDateTime validFrom,
        LocalDateTime validUntil
) {
}
