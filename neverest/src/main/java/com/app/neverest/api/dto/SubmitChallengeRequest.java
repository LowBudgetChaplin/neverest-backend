package com.app.neverest.api.dto;

import java.util.UUID;

public record SubmitChallengeRequest(
        UUID userId,
        String proofText,
        Double metricValue
) {
}
