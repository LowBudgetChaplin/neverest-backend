package com.app.neverest.api.dto;

import java.util.List;

public record MeResponse(
        String subject,
        boolean authenticated,
        List<String> authorities
) {
}
