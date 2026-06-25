package com.app.neverest.service;

import com.app.neverest.api.dto.CreateOfferRequest;
import com.app.neverest.api.dto.CreatePartnerChallengeRequest;
import com.app.neverest.api.dto.CreateRewardRequest;
import com.app.neverest.common.BadRequestException;
import com.app.neverest.common.ConflictException;
import com.app.neverest.common.NotFoundException;
import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.ChallengeFrequency;
import com.app.neverest.domain.ChallengeMode;
import com.app.neverest.persistence.entity.ChallengeEntity;
import com.app.neverest.persistence.entity.PartnerOfferEntity;
import com.app.neverest.persistence.entity.RewardEntity;
import com.app.neverest.persistence.entity.UserEntity;
import com.app.neverest.persistence.repository.ChallengeRepository;
import com.app.neverest.persistence.repository.PartnerOfferRepository;
import com.app.neverest.persistence.repository.RewardRepository;
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
    private final RewardRepository rewardRepository;

    public PartnerService(
            PartnerOfferRepository offerRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ChallengeRepository challengeRepository,
            RewardRepository rewardRepository
    ) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.challengeRepository = challengeRepository;
        this.rewardRepository = rewardRepository;
    }

    @Transactional
    public UserEntity createPartner(String email, String password, String displayName, String brand, String phoneNumber) {
        String normalizedEmail = com.app.neverest.common.Validators
                .requireValidEmail(email).toLowerCase(Locale.ROOT);
        String normalizedPhone = com.app.neverest.common.Validators.requireValidPhone(phoneNumber);
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
        user.setPhoneNumber(normalizedPhone);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<PartnerOfferEntity> getActiveOffers() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return offerRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
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
        return applyOfferUpdate(requireOwnedOffer(authSubject, offerId), request);
    }

    @Transactional
    public void deleteOffer(String authSubject, UUID offerId) {
        offerRepository.delete(requireOwnedOffer(authSubject, offerId));
    }

    @Transactional
    public PartnerOfferEntity adminUpdateOffer(UUID offerId, CreateOfferRequest request) {
        return applyOfferUpdate(getOfferOrThrow(offerId), request);
    }

    @Transactional
    public void adminDeleteOffer(UUID offerId) {
        offerRepository.delete(getOfferOrThrow(offerId));
    }

    private PartnerOfferEntity applyOfferUpdate(PartnerOfferEntity offer, CreateOfferRequest request) {
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
                0,
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

    @Transactional(readOnly = true)
    public List<RewardEntity> getMyRewards(String authSubject) {
        // Doar reward-urile active: stergerea face soft-delete (deactivate),
        // deci cele dezactivate nu trebuie sa mai apara in lista partenerului.
        return rewardRepository.findByOwnerUserIdAndActiveTrueOrderByTitleAsc(requireUserId(authSubject));
    }

    @Transactional
    public RewardEntity createReward(String authSubject, CreateRewardRequest request) {
        UUID ownerId = requireUserId(authSubject);
        RewardEntity reward = new RewardEntity(
                UUID.randomUUID(),
                requireNonBlank(request.title(), "title"),
                requireNonBlank(request.partnerName(), "partnerName"),
                requireNonBlank(request.description(), "description"),
                requirePositive(request.pointsCost(), "pointsCost"),
                normalizeStock(request.stock())
        );
        reward.setOwnerUserId(ownerId);
        if (request.rotationDays() != null && request.rotationDays() > 0) {
            reward.setRotationDays(request.rotationDays());
        }
        reward.setCategory(normalizeCategory(request.category()));
        return rewardRepository.save(reward);
    }

    @Transactional
    public RewardEntity updateReward(String authSubject, UUID rewardId, CreateRewardRequest request) {
        RewardEntity reward = requireOwnedReward(authSubject, rewardId);
        if (request.title() != null && !request.title().isBlank()) {
            reward.setTitle(request.title().trim());
        }
        if (request.partnerName() != null && !request.partnerName().isBlank()) {
            reward.setPartnerName(request.partnerName().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            reward.setDescription(request.description().trim());
        }
        if (request.pointsCost() != null) {
            reward.setPointsCost(requirePositive(request.pointsCost(), "pointsCost"));
        }
        if (request.stock() != null) {
            reward.setStock(normalizeStock(request.stock()));
        }
        if (request.rotationDays() != null) {
            reward.setRotationDays(request.rotationDays() > 0 ? request.rotationDays() : null);
        }
        if (request.category() != null) {
            reward.setCategory(normalizeCategory(request.category()));
        }
        return rewardRepository.save(reward);
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        return category.trim().toUpperCase(Locale.ROOT);
    }

    @Transactional
    public void deleteReward(String authSubject, UUID rewardId) {
        RewardEntity reward = requireOwnedReward(authSubject, rewardId);
        reward.deactivate();
        rewardRepository.save(reward);
    }

    private RewardEntity requireOwnedReward(String authSubject, UUID rewardId) {
        UUID ownerId = requireUserId(authSubject);
        RewardEntity reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new NotFoundException("Reward not found."));
        if (reward.getOwnerUserId() == null || !reward.getOwnerUserId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage your own rewards.");
        }
        return reward;
    }

    private int requirePositive(Integer value, String field) {
        if (value == null || value <= 0) {
            throw new BadRequestException(field + " must be a positive integer.");
        }
        return value;
    }

    private Integer normalizeStock(Integer stock) {
        if (stock == null) {
            return null;
        }
        if (stock <= 0) {
            throw new BadRequestException("stock must be greater than 0.");
        }
        return stock;
    }

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

    private PartnerOfferEntity getOfferOrThrow(UUID offerId) {
        return offerRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer not found."));
    }

    private PartnerOfferEntity requireOwnedOffer(String authSubject, UUID offerId) {
        UUID ownerId = requireUserId(authSubject);
        PartnerOfferEntity offer = getOfferOrThrow(offerId);
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
