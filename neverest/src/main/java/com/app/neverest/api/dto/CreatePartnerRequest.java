package com.app.neverest.api.dto;

public record CreatePartnerRequest(
        String email,
        String password,
        String displayName,
        String brand,
        String phoneNumber
) {
}
