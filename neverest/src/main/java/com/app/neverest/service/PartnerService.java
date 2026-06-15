package com.app.neverest.service;

import com.app.neverest.api.dto.CreateOfferRequest;
import com.app.neverest.api.dto.CreatePartnerChallengeRequest;
import com.app.neverest.common.BadRequestException;
import com.app.neverest.common.ConflictException;
import com.app.neverest.common.NotFoundException;
import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.ChallengeFrequency;
import com.app.neverest.domain.ChallengeMode;
import com.app.neverest.persistence.entity.ChallengeEntity;
import com.app.neverest.persistence.entity.PartnerOfferEntity;
import com.app.neverest.persistence.entity.UserEntity;
import com.app.neverest.persistence.repository.ChallengeRepository;
import com.app.neverest.persistence.repository.PartnerOfferRepository;
import com.app.neverest.persistence.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PartnerService {

    private final PartnerOfferRepository offerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChallengeRepository challengeRepository;

    public PartnerService(
            PartnerOfferRepository offerRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ChallengeRepository challengeRepository
    ) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.challengeRepository = challengeRepository;
    }

    // ── Admin: create a partner account ──────────────────────────────────────
    @Transactional
    public UserEntity createPartner(String email, String password, String displayName, String brand) {
        String normalizedEmail = requireNonBlank(email, "email").toLowerCase(Locale.ROOT);
        String rawPassword = requireNonBlank(password, "password");
        if (rawPassword.length() < 6) {
            throw new BadRequestException("password must contain at least 6 characters.");
        }
        if (userRepository.findByAuthSubjectIgnoreCase(normalizedEmail).isPresent()) {
            throw new ConflictException("An account with this email already exists.");
        }

        String effectiveName = (displayName == null || displayName.isBlank())
                ? (brand == null || brand.isBlank() ? normalizedEmail.split("@")[0] : brand.trim())
                : displayName.trim();

        UserEntity user = new UserEntity(
                UUID.randomUUID(),
                effectiveName,
                generateUniqueQrCode(),
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                "PARTNER"
        );
        return userRepository.save(user);
    }

    // ── Offers ───────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PartnerOfferEntity> getActiveOffers() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return offerRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                // visible only inside the scheduled window [validFrom, validUntil]
                .filter(o -> o.getValidFrom() == null || !now.isBefore(o.getValidFrom()))
                .filter(o -> o.getValidUntil() == null || !now.isAfter(o.getValidUntil()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PartnerOfferEntity> getMyOffers(String authSubject) {
        return offerRepository.findByOwnerUserIdOrderByCreatedAtDesc(requireUserId(authSubject));
    }

    @Transactional
    public PartnerOfferEntity createOffer(String authSubject, CreateOfferRequest request) {
        UUID ownerId = requireUserId(authSubject);
        PartnerOfferEntity offer = new PartnerOfferEntity(
                UUID.randomUUID(),
                ownerId,
                requireNonBlank(request.brand(), "brand"),
                requireNonBlank(request.title(), "title")
        );
        applyFields(offer, request);
        offer.setActive(request.active() == null || request.active());
        return offerRepository.save(offer);
    }

    @Transactional
    public PartnerOfferEntity updateOffer(String authSubject, UUID offerId, CreateOfferRequest request) {
        PartnerOfferEntity offer = requireOwnedOffer(authSubject, offerId);
        if (request.brand() != null && !request.brand().isBlank()) {
            offer.setBrand(request.brand().trim());
        }
        if (request.title() != null && !request.title().isBlank()) {
            offer.setTitle(request.title().trim());
        }
        applyFields(offer, request);
        if (request.active() != null) {
            offer.setActive(request.active());
        }
        return offerRepository.save(offer);
    }

    @Transactional
    public void deleteOffer(String authSubject, UUID offerId) {
        PartnerOfferEntity offer = requireOwnedOffer(authSubject, offerId);
        offerRepository.delete(offer);
    }

    // ── Partner challenges (benefit reward, no points) ───────────────────────
    @Transactional(readOnly = true)
    public List<ChallengeEntity> getMyChallenges(String authSubject) {
        return challengeRepository.findByOwnerUserIdOrderByCreatedAtDesc(requireUserId(authSubject));
    }

    @Transactional
    public ChallengeEntity createChallenge(String authSubject, CreatePartnerChallengeRequest request) {
        UUID ownerId = requireUserId(authSubject);
        if (request.activityType() == null) {
            throw new BadRequestException("activityType is required.");
        }
        ChallengeEntity challenge = new ChallengeEntity(
                UUID.randomUUID(),
                requireNonBlank(request.title(), "title"),
                requireNonBlank(request.description(), "description"),
                request.activityType(),
                ChallengeMode.ONLINE,
                ChallengeFrequency.MONTHLY,
                request.startsAt(),
                request.endsAt(),
                0, // partner challenges award a benefit, not points
                request.targetValue(),
                blankToNull(request.targetUnit())
        );
        challenge.setOwnerUserId(ownerId);
        challenge.setRewardKind(blankToNull(request.rewardKind()) == null
                ? "DISCOUNT" : request.rewardKind().trim().toUpperCase(Locale.ROOT));
        challenge.setRewardLabel(requireNonBlank(request.rewardLabel(), "rewardLabel"));
        challenge.setBrand(resolveBrand(ownerId, request.brand()));
        return challengeRepository.save(challenge);
    }

    @Transactional
    public ChallengeEntity updateChallenge(String authSubject, UUID challengeId,
                                           CreatePartnerChallengeRequest request) {
        ChallengeEntity challenge = requireOwnedChallenge(authSubject, challengeId);
        if (request.title() != null && !request.title().isBlank()) {
            challenge.setTitle(request.title().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            challenge.setDescription(request.description().trim());
        }
        if (request.activityType() != null) {
            challenge.setActivityType(request.activityType());
        }
        if (request.startsAt() != null) {
            challenge.setStartsAt(request.startsAt());
        }
        if (request.endsAt() != null) {
            challenge.setEndsAt(request.endsAt());
        }
        if (request.rewardKind() != null && !request.rewardKind().isBlank()) {
            challenge.setRewardKind(request.rewardKind().trim().toUpperCase(Locale.ROOT));
        }
        if (request.rewardLabel() != null && !request.rewardLabel().isBlank()) {
            challenge.setRewardLabel(request.rewardLabel().trim());
        }
        if (request.brand() != null && !request.brand().isBlank()) {
            challenge.setBrand(request.brand().trim());
        }
        return challengeRepository.save(challenge);
    }

    @Transactional
    public void deleteChallenge(String authSubject, UUID challengeId) {
        challengeRepository.delete(requireOwnedChallenge(authSubject, challengeId));
    }

    private ChallengeEntity requireOwnedChallenge(String authSubject, UUID challengeId) {
        UUID ownerId = requireUserId(authSubject);
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge not found."));
        if (challenge.getOwnerUserId() == null || !challenge.getOwnerUserId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only manage your own challenges.");
        }
        return challenge;
    }

    private String resolveBrand(UUID ownerId, String requestedBrand) {
        if (requestedBrand != null && !requestedBrand.isBlank()) {
            return requestedBrand.trim();
        }
        return userRepository.findById(ownerId)
                .map(UserEntity::getDisplayName)
                .orElse("Partner");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private void applyFields(PartnerOfferEntity offer, CreateOfferRequest request) {
        offer.setDescription(blankToNull(request.description()));
        offer.setDiscountLabel(blankToNull(request.discountLabel()));
        offer.setLinkUrl(blankToNull(request.linkUrl()));
        offer.setValidFrom(request.validFrom());
        offer.setValidUntil(request.validUntil());
        if (request.imageB64() != null && !request.imageB64().isBlank()) {
            offer.setImageB64(request.imageB64());
        }
    }

    private PartnerOfferEntity requireOwnedOffer(String authSubject, UUID offerId) {
        UUID ownerId = requireUserId(authSubject);
        PartnerOfferEntity offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found."));
        if (!offer.getOwnerUserId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your own offers.");
        }
        return offer;
    }

    private UUID requireUserId(String authSubject) {
        if (authSubject == null || authSubject.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        return userRepository.findByAuthSubjectIgnoreCase(authSubject.trim())
                .map(UserEntity::getId)
                .orElseThrow(() -> new NotFoundException("No user profile is linked to the authenticated account."));
    }

    private String generateUniqueQrCode() {
        for (int i = 0; i < 20; i++) {
            String qrCode = "NEV-P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (userRepository.findByQrCode(qrCode).isEmpty()) {
                return qrCode;
            }
        }
        throw new ConflictException("Could not generate unique QR code.");
    }

    private String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(field + " is required.");
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        return value.isBlank() ? null : value.trim();
    }
}
