package com.app.neverest.api.dto;

public record ReviewChallengeSubmissionRequest(
        Boolean approved,
        String reviewerNote
) {
}
