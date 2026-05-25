package com.app.neverest.api.dto;

public record RegisterRequest(
        String email,
        String password,
        String displayName
) {
}
