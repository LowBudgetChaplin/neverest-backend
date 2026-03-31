package com.app.neverest.persistence.entity;

import com.app.neverest.domain.ChallengeSubmissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "nev_challenge_submissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_nev_challenge_submissions_challenge_user",
                columnNames = {"challenge_id", "user_id"}
        )
)
public class ChallengeSubmissionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "proof_text", length = 1000)
    private String proofText;

    @Column(name = "metric_value")
    private Double metricValue;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ChallengeSubmissionStatus status;

    @Column(name = "awarded_points", nullable = false)
    private int awardedPoints;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewer_note", length = 600)
    private String reviewerNote;

    protected ChallengeSubmissionEntity() {
    }

    public ChallengeSubmissionEntity(
            UUID id,
            UUID challengeId,
            UUID userId,
            String proofText,
            Double metricValue,
            LocalDateTime submittedAt
    ) {
        this.id = id;
        this.challengeId = challengeId;
        this.userId = userId;
        this.proofText = proofText;
        this.metricValue = metricValue;
        this.submittedAt = submittedAt;
        this.status = ChallengeSubmissionStatus.PENDING;
        this.awardedPoints = 0;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChallengeId() {
        return challengeId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getProofText() {
        return proofText;
    }

    public Double getMetricValue() {
        return metricValue;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public ChallengeSubmissionStatus getStatus() {
        return status;
    }

    public int getAwardedPoints() {
        return awardedPoints;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getReviewerNote() {
        return reviewerNote;
    }

    public void approve(int pointsAwarded, String reviewerNote) {
        this.status = ChallengeSubmissionStatus.APPROVED;
        this.awardedPoints = pointsAwarded;
        this.reviewedAt = LocalDateTime.now();
        this.reviewerNote = reviewerNote;
    }

    public void reject(String reviewerNote) {
        this.status = ChallengeSubmissionStatus.REJECTED;
        this.awardedPoints = 0;
        this.reviewedAt = LocalDateTime.now();
        this.reviewerNote = reviewerNote;
    }
}
