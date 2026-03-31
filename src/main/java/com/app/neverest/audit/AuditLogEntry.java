package com.app.neverest.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogEntry(
        UUID id,
        Instant timestamp,
        String action,
        String actor,
        boolean success,
        String message,
        Map<String, String> metadata
) {
}
