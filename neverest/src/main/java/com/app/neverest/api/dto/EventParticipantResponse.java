package com.app.neverest.api.dto;

import java.util.UUID;

public record EventParticipantResponse(
        UUID userId,
        String name,
        String avatarB64,
        boolean checkedIn
) {
}
