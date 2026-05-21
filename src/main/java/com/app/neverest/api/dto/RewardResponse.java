package com.app.neverest.api.dto;

import java.util.UUID;

public record RewardResponse(
        UUID id,
        String title,
        String partnerName,
        String description,
        int pointsCost,
        Integer stock,
        boolean active
) {
}
