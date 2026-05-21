package com.app.neverest.api.dto;

import java.util.UUID;

public record LeaderboardEntryResponse(
        UUID userId,
        String displayName,
        int points
) {
}
