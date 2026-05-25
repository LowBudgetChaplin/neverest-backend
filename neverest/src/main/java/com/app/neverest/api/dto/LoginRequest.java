package com.app.neverest.api.dto;

public record LoginRequest(
        String email,
        String password
) {
}
