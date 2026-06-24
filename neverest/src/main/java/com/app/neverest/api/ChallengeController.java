package com.app.neverest.api;

import com.app.neverest.api.dto.ChallengeResponse;
import com.app.neverest.api.dto.ChallengeSubmissionResponse;
import com.app.neverest.api.dto.CreateChallengeRequest;
import com.app.neverest.api.dto.ReviewChallengeSubmissionRequest;
import com.app.neverest.api.dto.SubmitChallengeRequest;
import com.app.neverest.api.dto.UpdateChallengeRequest;
import com.app.neverest.audit.AuditLogService;
import com.app.neverest.common.AuthUtils;
import com.app.neverest.common.BadRequestException;
import com.app.neverest.domain.Challenge;
import com.app.neverest.domain.ChallengeSubmission;
import com.app.neverest.service.NeverestCoreService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {

    private final NeverestCoreService coreService;
    private final AuditLogService auditLogService;

    public ChallengeController(NeverestCoreService coreService, AuditLogService auditLogService) {
        this.coreService = coreService;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeResponse createChallenge(@RequestBody CreateChallengeRequest request, Authentication authentication) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        Challenge challenge = coreService.createChallenge(
                request.title(),
                request.description(),
                request.activityType(),
                request.mode(),
                request.frequency(),
                request.startsAt(),
                request.endsAt(),
                request.pointsReward(),
                request.targetValue(),
                request.targetUnit()
        );

        auditLogService.log(
                "CHALLENGE_CREATED",
                AuthUtils.actor(authentication),
                true,
                "Challenge created successfully.",
                Map.of(
                        "challengeId", challenge.id().toString(),
                        "mode", challenge.mode().name(),
                        "frequency", challenge.frequency().name()
                )
        );

        return toResponse(challenge);
    }

    @GetMapping
    public List<ChallengeResponse> getChallenges(Authentication authentication) {
        UUID userId = coreService.findUserIdByAuthSubjectOrNull(AuthUtils.subjectOrNull(authentication));
        Set<UUID> completedIds = coreService.getCompletedChallengeIds(userId);
        return coreService.getChallenges()
                .stream()
                .map(challenge -> toResponse(challenge, completedIds.contains(challenge.id())))
                .toList();
    }

    @PatchMapping("/{challengeId}")
    public ChallengeResponse updateChallenge(
            @PathVariable UUID challengeId,
            @RequestBody UpdateChallengeRequest request,
            Authentication authentication
    ) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        Challenge challenge = coreService.updateChallenge(
                challengeId,
                request.title(),
                request.description(),
                request.activityType(),
                request.pointsReward(),
                request.targetValue(),
                request.targetUnit(),
                request.startsAt(),
                request.endsAt()
        );
        auditLogService.log(
                "CHALLENGE_UPDATED",
                AuthUtils.actor(authentication),
                true,
                "Challenge updated.",
                Map.of("challengeId", challengeId.toString())
        );
        return toResponse(challenge);
    }

    @DeleteMapping("/{challengeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChallenge(@PathVariable UUID challengeId, Authentication authentication) {
        coreService.deleteChallenge(challengeId);
        auditLogService.log(
                "CHALLENGE_DELETED",
                AuthUtils.actor(authentication),
                true,
                "Challenge deleted.",
                Map.of("challengeId", challengeId.toString())
        );
    }

    @PostMapping("/{challengeId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeSubmissionResponse submitChallenge(
            @PathVariable UUID challengeId,
            @RequestBody SubmitChallengeRequest request,
            Authentication authentication
    ) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        UUID effectiveUserId = coreService.resolveUserIdForAction(
                AuthUtils.subjectOrNull(authentication),
                request.userId()
        );

        ChallengeSubmission submission = coreService.submitChallenge(
                challengeId,
                effectiveUserId,
                request.proofText(),
                request.metricValue()
        );

        auditLogService.log(
                "CHALLENGE_SUBMITTED",
                AuthUtils.actor(authentication),
                true,
                "Challenge submission created.",
                Map.of(
                        "challengeId", challengeId.toString(),
                        "submissionId", submission.id().toString(),
                        "userId", submission.userId().toString(),
                        "status", submission.status().name()
                )
        );

        return toResponse(submission);
    }

    @PostMapping("/{challengeId}/submissions/me")
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeSubmissionResponse submitChallengeAsMe(
            @PathVariable UUID challengeId,
            @RequestBody SubmitChallengeRequest request,
            Authentication authentication
    ) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        String authSubject = AuthUtils.requireSubject(authentication);
        UUID effectiveUserId = coreService.resolveUserIdForAction(authSubject, null);
        ChallengeSubmission submission = coreService.submitChallenge(
                challengeId,
                effectiveUserId,
                request.proofText(),
                request.metricValue()
        );

        auditLogService.log(
                "CHALLENGE_SUBMITTED",
                authSubject,
                true,
                "Challenge submission created from /me endpoint.",
                Map.of(
                        "challengeId", challengeId.toString(),
                        "submissionId", submission.id().toString(),
                        "userId", submission.userId().toString(),
                        "status", submission.status().name()
                )
        );

        return toResponse(submission);
    }

    @GetMapping("/{challengeId}/submissions")
    public List<ChallengeSubmissionResponse> getChallengeSubmissions(
            @PathVariable UUID challengeId,
            @RequestParam(required = false) UUID userId
    ) {
        return coreService.getChallengeSubmissions(challengeId, userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{challengeId}/submissions/me")
    public List<ChallengeSubmissionResponse> getMyChallengeSubmissions(
            @PathVariable UUID challengeId,
            Authentication authentication
    ) {
        String authSubject = AuthUtils.requireSubject(authentication);
        UUID userId = coreService.resolveUserIdForAction(authSubject, null);
        return coreService.getChallengeSubmissions(challengeId, userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/{challengeId}/submissions/{submissionId}/review")
    public ChallengeSubmissionResponse reviewChallengeSubmission(
            @PathVariable UUID challengeId,
            @PathVariable UUID submissionId,
            @RequestBody ReviewChallengeSubmissionRequest request,
            Authentication authentication
    ) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        ChallengeSubmission submission = coreService.reviewChallengeSubmission(
                challengeId,
                submissionId,
                request.approved(),
                request.reviewerNote()
        );

        auditLogService.log(
                "CHALLENGE_REVIEWED",
                AuthUtils.actor(authentication),
                true,
                "Challenge submission reviewed.",
                Map.of(
                        "challengeId", challengeId.toString(),
                        "submissionId", submission.id().toString(),
                        "approved", String.valueOf(Boolean.TRUE.equals(request.approved())),
                        "status", submission.status().name()
                )
        );

        return toResponse(submission);
    }

    private ChallengeResponse toResponse(Challenge challenge) {
        return toResponse(challenge, false);
    }

    private ChallengeResponse toResponse(Challenge challenge, boolean completed) {
        return new ChallengeResponse(
                challenge.id(),
                challenge.title(),
                challenge.description(),
                challenge.activityType(),
                challenge.mode(),
                challenge.frequency(),
                challenge.startsAt(),
                challenge.endsAt(),
                challenge.pointsReward(),
                challenge.targetValue(),
                challenge.targetUnit(),
                completed,
                challenge.ownerUserId() == null ? null : challenge.ownerUserId().toString(),
                challenge.rewardKind(),
                challenge.rewardLabel(),
                challenge.brand()
        );
    }

    private ChallengeSubmissionResponse toResponse(ChallengeSubmission submission) {
        return new ChallengeSubmissionResponse(
                submission.id(),
                submission.challengeId(),
                submission.userId(),
                submission.proofText(),
                submission.metricValue(),
                submission.status(),
                submission.awardedPoints(),
                submission.submittedAt(),
                submission.reviewedAt(),
                submission.reviewerNote()
        );
    }
}
