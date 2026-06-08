package com.app.neverest.api.dto;

import com.app.neverest.domain.ActivityType;
import java.util.Map;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String displayName,
        String qrCode,
        String authSubject,
        int totalPoints,
        int availablePoints,
        Map<ActivityType, Integer> pointsByActivity,
        String phoneNumber,
        String avatarB64
) {
}
