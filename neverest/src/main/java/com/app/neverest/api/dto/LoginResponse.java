package com.app.neverest.api.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
