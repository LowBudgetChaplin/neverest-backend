package com.app.neverest.api.dto;

public record UpdateProfileRequest(
        String displayName,
        String phoneNumber,
        String avatarB64
) {
}
