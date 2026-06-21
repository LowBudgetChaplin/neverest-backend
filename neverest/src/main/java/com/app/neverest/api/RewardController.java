package com.app.neverest.api;

import com.app.neverest.api.dto.CreateRewardRequest;
import com.app.neverest.api.dto.RedeemRewardRequest;
import com.app.neverest.api.dto.RewardRedemptionResponse;
import com.app.neverest.api.dto.RewardResponse;
import com.app.neverest.api.dto.UpdateRewardRequest;
import com.app.neverest.api.dto.ValidateRedemptionRequest;
import com.app.neverest.api.dto.ValidateRedemptionResponse;
import com.app.neverest.audit.AuditLogService;
import com.app.neverest.common.AuthUtils;
import com.app.neverest.common.BadRequestException;
import com.app.neverest.domain.Reward;
import com.app.neverest.domain.RewardRedemption;
import com.app.neverest.service.NeverestCoreService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/v1/rewards")
public class RewardController {

    private final NeverestCoreService coreService;
    private final AuditLogService auditLogService;

    public RewardController(NeverestCoreService coreService, AuditLogService auditLogService) {
        this.coreService = coreService;
        this.auditLogService = auditLogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RewardResponse createReward(@RequestBody CreateRewardRequest request, Authentication authentication) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        Reward reward = coreService.createReward(
                request.title(),
                request.partnerName(),
                request.description(),
                request.pointsCost(),
                request.stock(),
                request.rotationDays()
        );

        auditLogService.log(
                "REWARD_CREATED",
                AuthUtils.actor(authentication),
                true,
                "Reward created successfully.",
                Map.of(
                        "rewardId", reward.id().toString(),
                        "pointsCost", String.valueOf(reward.pointsCost())
                )
        );

        return toResponse(reward);
    }

    @PatchMapping("/{rewardId}")
    @ResponseStatus(HttpStatus.OK)
    public RewardResponse updateReward(
            @PathVariable UUID rewardId,
            @RequestBody UpdateRewardRequest request,
            Authentication authentication
    ) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        Reward reward = coreService.updateReward(
                rewardId,
                request.title(),
                request.partnerName(),
                request.description(),
                request.pointsCost(),
                request.stock(),
                Boolean.TRUE.equals(request.clearStock()),
                request.address(),
                request.imageB64(),
                Boolean.TRUE.equals(request.clearImage())
        );

        auditLogService.log(
                "REWARD_UPDATED",
                AuthUtils.actor(authentication),
                true,
                "Reward updated successfully.",
                Map.of("rewardId", reward.id().toString())
        );

        return toResponse(reward);
    }

    @GetMapping
    public List<RewardResponse> getRewards(
            @RequestParam(required = false) Boolean includeInactive,
            Authentication authentication
    ) {
        UUID userId = coreService.findUserIdByAuthSubjectOrNull(
                AuthUtils.subjectOrNull(authentication));
        var latest = coreService.getLatestRedemptionsByReward(userId);
        return coreService.getRewards(includeInactive)
                .stream()
                .map(reward -> toResponse(reward, latest.get(reward.id())))
                .toList();
    }

    @PostMapping("/{rewardId}/redeem")
    public RewardRedemptionResponse redeemReward(
            @PathVariable UUID rewardId,
            @RequestBody RedeemRewardRequest request,
            Authentication authentication
    ) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        UUID effectiveUserId = coreService.resolveUserIdForAction(
                AuthUtils.subjectOrNull(authentication),
                request.userId()
        );

        RewardRedemption redemption = coreService.redeemReward(rewardId, effectiveUserId);
        auditLogService.log(
                "REWARD_REDEEMED",
                AuthUtils.actor(authentication),
                true,
                "Reward redeemed successfully.",
                Map.of(
                        "rewardId", rewardId.toString(),
                        "redemptionId", redemption.id().toString(),
                        "userId", redemption.userId().toString(),
                        "pointsSpent", String.valueOf(redemption.pointsSpent())
                )
        );
        return toResponse(redemption);
    }

    @PostMapping("/{rewardId}/redeem/me")
    public RewardRedemptionResponse redeemRewardAsMe(
            @PathVariable UUID rewardId,
            Authentication authentication
    ) {
        String authSubject = AuthUtils.requireSubject(authentication);
        UUID userId = coreService.resolveUserIdForAction(authSubject, null);
        RewardRedemption redemption = coreService.redeemReward(rewardId, userId);

        auditLogService.log(
                "REWARD_REDEEMED",
                authSubject,
                true,
                "Reward redeemed from /me endpoint.",
                Map.of(
                        "rewardId", rewardId.toString(),
                        "redemptionId", redemption.id().toString(),
                        "userId", redemption.userId().toString(),
                        "pointsSpent", String.valueOf(redemption.pointsSpent())
                )
        );
        return toResponse(redemption);
    }

    @PostMapping("/redemptions/validate")
    public ValidateRedemptionResponse validateRedemption(
            @RequestBody ValidateRedemptionRequest request,
            Authentication authentication
    ) {
        if (request == null || request.code() == null || request.code().isBlank()) {
            throw new BadRequestException("code is required.");
        }
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        UUID scannerUserId = isAdmin
                ? null
                : coreService.resolveUserIdForAction(AuthUtils.requireSubject(authentication), null);

        NeverestCoreService.RedemptionValidationResult result =
                coreService.validateRedemptionCode(request.code(), scannerUserId, isAdmin);

        auditLogService.log(
                "REWARD_CODE_VALIDATED",
                AuthUtils.actor(authentication),
                result.valid(),
                result.status(),
                Map.of("code", request.code().trim(), "status", result.status())
        );

        return new ValidateRedemptionResponse(
                result.valid(),
                result.status(),
                result.rewardTitle(),
                result.userName(),
                result.code(),
                result.consumedAt()
        );
    }

    @GetMapping("/redemptions")
    public List<RewardRedemptionResponse> getRedemptions(@RequestParam(required = false) UUID userId) {
        return coreService.getRewardRedemptions(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/redemptions/me")
    public List<RewardRedemptionResponse> getMyRedemptions(Authentication authentication) {
        String authSubject = AuthUtils.requireSubject(authentication);
        UUID userId = coreService.resolveUserIdForAction(authSubject, null);
        return coreService.getRewardRedemptions(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RewardResponse toResponse(Reward reward) {
        return toResponse(reward, null);
    }

    private RewardResponse toResponse(Reward reward, RewardRedemption userRedemption) {
        String couponStatus = "AVAILABLE";
        String couponCode = null;
        java.time.LocalDateTime availableAgainAt = null;
        if (userRedemption != null) {
            final Integer rotation = reward.rotationDays();
            if (rotation == null) {
                couponStatus = "USED";
                couponCode = userRedemption.redemptionCode();
            } else {
                java.time.LocalDateTime windowEnd =
                        userRedemption.redeemedAt().plusDays(rotation);
                if (java.time.LocalDateTime.now().isBefore(windowEnd)) {
                    couponStatus = "USED";
                    couponCode = userRedemption.redemptionCode();
                    availableAgainAt = windowEnd;
                }
                // else: window passed → AVAILABLE again (new code on next redeem)
            }
        }
        return new RewardResponse(
                reward.id(),
                reward.title(),
                reward.partnerName(),
                reward.description(),
                reward.pointsCost(),
                reward.stock(),
                reward.active(),
                reward.address(),
                reward.imageB64(),
                reward.category(),
                reward.rotationDays(),
                couponStatus,
                couponCode,
                availableAgainAt
        );
    }

    private RewardRedemptionResponse toResponse(RewardRedemption redemption) {
        return new RewardRedemptionResponse(
                redemption.id(),
                redemption.rewardId(),
                redemption.userId(),
                redemption.rewardTitle(),
                redemption.pointsSpent(),
                redemption.redemptionCode(),
                redemption.redeemedAt(),
                redemption.userAvailablePointsAfterRedemption()
        );
    }
}
