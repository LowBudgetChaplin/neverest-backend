package com.app.neverest.api.dto;

import com.app.neverest.domain.ChallengeSubmissionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChallengeSubmissionResponse(
        UUID id,
        UUID challengeId,
        UUID userId,
        String proofText,
        Double metricValue,
        ChallengeSubmissionStatus status,
        int awardedPoints,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String reviewerNote
) {
}
