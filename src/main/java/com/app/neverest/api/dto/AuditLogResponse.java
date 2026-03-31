package com.app.neverest.api.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        Instant timestamp,
        String action,
        String actor,
        boolean success,
        String message,
        Map<String, String> metadata
) {
}
