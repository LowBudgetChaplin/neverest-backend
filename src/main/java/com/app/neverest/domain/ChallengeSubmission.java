package com.app.neverest.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChallengeSubmission {

    private final UUID id;
    private final UUID challengeId;
    private final UUID userId;
    private final String proofText;
    private final Double metricValue;
    private final LocalDateTime submittedAt;
    private ChallengeSubmissionStatus status;
    private int awardedPoints;
    private LocalDateTime reviewedAt;
    private String reviewerNote;

    public ChallengeSubmission(
            UUID id,
            UUID challengeId,
            UUID userId,
            String proofText,
            Double metricValue,
            LocalDateTime submittedAt
    ) {
        this(
                id,
                challengeId,
                userId,
                proofText,
                metricValue,
                submittedAt,
                ChallengeSubmissionStatus.PENDING,
                0,
                null,
                null
        );
    }

    public ChallengeSubmission(
            UUID id,
            UUID challengeId,
            UUID userId,
            String proofText,
            Double metricValue,
            LocalDateTime submittedAt,
            ChallengeSubmissionStatus status,
            int awardedPoints,
            LocalDateTime reviewedAt,
            String reviewerNote
    ) {
        this.id = id;
        this.challengeId = challengeId;
        this.userId = userId;
        this.proofText = proofText;
        this.metricValue = metricValue;
        this.submittedAt = submittedAt;
        this.status = status;
        this.awardedPoints = awardedPoints;
        this.reviewedAt = reviewedAt;
        this.reviewerNote = reviewerNote;
    }

    public UUID id() {
        return id;
    }

    public UUID challengeId() {
        return challengeId;
    }

    public UUID userId() {
        return userId;
    }

    public String proofText() {
        return proofText;
    }

    public Double metricValue() {
        return metricValue;
    }

    public LocalDateTime submittedAt() {
        return submittedAt;
    }

    public ChallengeSubmissionStatus status() {
        return status;
    }

    public int awardedPoints() {
        return awardedPoints;
    }

    public LocalDateTime reviewedAt() {
        return reviewedAt;
    }

    public String reviewerNote() {
        return reviewerNote;
    }

    public void approve(int pointsAwarded, String note) {
        this.status = ChallengeSubmissionStatus.APPROVED;
        this.awardedPoints = pointsAwarded;
        this.reviewedAt = LocalDateTime.now();
        this.reviewerNote = note;
    }

    public void reject(String note) {
        this.status = ChallengeSubmissionStatus.REJECTED;
        this.awardedPoints = 0;
        this.reviewedAt = LocalDateTime.now();
        this.reviewerNote = note;
    }
}
