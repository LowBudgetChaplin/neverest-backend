package com.app.neverest.api;

import com.app.neverest.api.dto.ChallengeResponse;
import com.app.neverest.api.dto.CreateOfferRequest;
import com.app.neverest.api.dto.CreatePartnerChallengeRequest;
import com.app.neverest.api.dto.CreatePartnerRequest;
import com.app.neverest.api.dto.OfferResponse;
import com.app.neverest.persistence.entity.ChallengeEntity;
import com.app.neverest.audit.AuditLogService;
import com.app.neverest.common.AuthUtils;
import com.app.neverest.common.BadRequestException;
import com.app.neverest.common.ConflictException;
import com.app.neverest.persistence.entity.PartnerOfferEntity;
import com.app.neverest.persistence.entity.UserEntity;
import com.app.neverest.service.PartnerService;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class PartnerController {

    private final PartnerService partnerService;
    private final AuditLogService auditLogService;

    public PartnerController(PartnerService partnerService, AuditLogService auditLogService) {
        this.partnerService = partnerService;
        this.auditLogService = auditLogService;
    }

    // ── Admin creates a partner account ──────────────────────────────────────
    @PostMapping("/partners")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createPartner(@RequestBody CreatePartnerRequest request, Authentication authentication) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        try {
            UserEntity partner = partnerService.createPartner(
                    request.email(), request.password(), request.displayName(), request.brand());
            auditLogService.log(
                    "PARTNER_CREATED",
                    AuthUtils.actor(authentication),
                    true,
                    "Partner account created.",
                    Map.of("userId", partner.getId().toString(), "email", partner.getAuthSubject())
            );
            return Map.of(
                    "id", partner.getId().toString(),
                    "displayName", partner.getDisplayName(),
                    "email", partner.getAuthSubject(),
                    "role", partner.getRole()
            );
        } catch (ConflictException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }

    // ── Offers (advertising space) ───────────────────────────────────────────
    @GetMapping("/offers")
    public List<OfferResponse> getActiveOffers() {
        return partnerService.getActiveOffers().stream().map(this::toResponse).toList();
    }

    @GetMapping("/offers/mine")
    public List<OfferResponse> getMyOffers(Authentication authentication) {
        return partnerService.getMyOffers(AuthUtils.requireSubject(authentication))
                .stream().map(this::toResponse).toList();
    }

    @PostMapping("/offers")
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponse createOffer(@RequestBody CreateOfferRequest request, Authentication authentication) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        return toResponse(partnerService.createOffer(AuthUtils.requireSubject(authentication), request));
    }

    @PatchMapping("/offers/{offerId}")
    public OfferResponse updateOffer(
            @PathVariable UUID offerId,
            @RequestBody CreateOfferRequest request,
            Authentication authentication
    ) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        return toResponse(partnerService.updateOffer(AuthUtils.requireSubject(authentication), offerId, request));
    }

    @DeleteMapping("/offers/{offerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOffer(@PathVariable UUID offerId, Authentication authentication) {
        partnerService.deleteOffer(AuthUtils.requireSubject(authentication), offerId);
    }

    // ── Partner challenges ───────────────────────────────────────────────────
    @GetMapping("/partner-challenges/mine")
    public List<ChallengeResponse> getMyChallenges(Authentication authentication) {
        return partnerService.getMyChallenges(AuthUtils.requireSubject(authentication))
                .stream().map(this::toResponse).toList();
    }

    @PostMapping("/partner-challenges")
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeResponse createChallenge(
            @RequestBody CreatePartnerChallengeRequest request,
            Authentication authentication
    ) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        return toResponse(
                partnerService.createChallenge(AuthUtils.requireSubject(authentication), request));
    }

    @PatchMapping("/partner-challenges/{challengeId}")
    public ChallengeResponse updateChallenge(
            @PathVariable UUID challengeId,
            @RequestBody CreatePartnerChallengeRequest request,
            Authentication authentication
    ) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }
        return toResponse(partnerService.updateChallenge(
                AuthUtils.requireSubject(authentication), challengeId, request));
    }

    @DeleteMapping("/partner-challenges/{challengeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChallenge(@PathVariable UUID challengeId, Authentication authentication) {
        partnerService.deleteChallenge(AuthUtils.requireSubject(authentication), challengeId);
    }

    private ChallengeResponse toResponse(ChallengeEntity c) {
        return new ChallengeResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getActivityType(),
                c.getMode(),
                c.getFrequency(),
                c.getStartsAt(),
                c.getEndsAt(),
                c.getPointsReward(),
                c.getTargetValue(),
                c.getTargetUnit(),
                false,
                c.getOwnerUserId() == null ? null : c.getOwnerUserId().toString(),
                c.getRewardKind(),
                c.getRewardLabel(),
                c.getBrand()
        );
    }

    private OfferResponse toResponse(PartnerOfferEntity offer) {
        return new OfferResponse(
                offer.getId(),
                offer.getOwnerUserId(),
                offer.getBrand(),
                offer.getTitle(),
                offer.getDescription(),
                offer.getDiscountLabel(),
                offer.getImageB64(),
                offer.getLinkUrl(),
                offer.isActive(),
                offer.getValidFrom(),
                offer.getValidUntil()
        );
    }
}
