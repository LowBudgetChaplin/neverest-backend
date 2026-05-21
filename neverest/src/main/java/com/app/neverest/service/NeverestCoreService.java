package com.app.neverest.service;

import com.app.neverest.common.BadRequestException;
import com.app.neverest.common.ConflictException;
import com.app.neverest.common.NotFoundException;
import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.Challenge;
import com.app.neverest.domain.ChallengeFrequency;
import com.app.neverest.domain.ChallengeMode;
import com.app.neverest.domain.ChallengeSubmission;
import com.app.neverest.domain.ChallengeSubmissionStatus;
import com.app.neverest.domain.Event;
import com.app.neverest.domain.Reward;
import com.app.neverest.domain.RewardRedemption;
import com.app.neverest.domain.UserProfile;
import com.app.neverest.persistence.entity.ChallengeEntity;
import com.app.neverest.persistence.entity.ChallengeSubmissionEntity;
import com.app.neverest.persistence.entity.EventCheckInEntity;
import com.app.neverest.persistence.entity.EventEntity;
import com.app.neverest.persistence.entity.RewardEntity;
import com.app.neverest.persistence.entity.RewardRedemptionEntity;
import com.app.neverest.persistence.entity.UserEntity;
import com.app.neverest.persistence.repository.ChallengeRepository;
import com.app.neverest.persistence.repository.ChallengeSubmissionRepository;
import com.app.neverest.persistence.repository.EventCheckInRepository;
import com.app.neverest.persistence.repository.EventRepository;
import com.app.neverest.persistence.repository.RewardRedemptionRepository;
import com.app.neverest.persistence.repository.RewardRepository;
import com.app.neverest.persistence.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NeverestCoreService {
    private static final int DEFAULT_LEADERBOARD_LIMIT = 20;
    private static final int MAX_LEADERBOARD_LIMIT = 100;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventCheckInRepository eventCheckInRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeSubmissionRepository challengeSubmissionRepository;
    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;

    public NeverestCoreService(
            UserRepository userRepository,
            EventRepository eventRepository,
            EventCheckInRepository eventCheckInRepository,
            ChallengeRepository challengeRepository,
            ChallengeSubmissionRepository challengeSubmissionRepository,
            RewardRepository rewardRepository,
            RewardRedemptionRepository rewardRedemptionRepository
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.eventCheckInRepository = eventCheckInRepository;
        this.challengeRepository = challengeRepository;
        this.challengeSubmissionRepository = challengeSubmissionRepository;
        this.rewardRepository = rewardRepository;
        this.rewardRedemptionRepository = rewardRedemptionRepository;
    }

    @Transactional
    public UserProfile createUser(String displayName) {
        return createUser(displayName, null);
    }

    @Transactional
    public UserProfile createUser(String displayName, String authSubject) {
        String sanitizedDisplayName = requireNonBlank(displayName, "displayName");
        String normalizedAuthSubject = normalizeOptional(authSubject);

        if (normalizedAuthSubject != null && userRepository.existsByAuthSubject(normalizedAuthSubject)) {
            throw new ConflictException("This authenticated account already has a user profile.");
        }

        String qrCode = generateUniqueQrCode();
        UserEntity userEntity = new UserEntity(UUID.randomUUID(), sanitizedDisplayName, qrCode, normalizedAuthSubject);

        try {
            return toDomain(userRepository.save(userEntity));
        } catch (DataIntegrityViolationException exception) {
            if (normalizedAuthSubject != null && userRepository.existsByAuthSubject(normalizedAuthSubject)) {
                throw new ConflictException("This authenticated account already has a user profile.");
            }
            throw new ConflictException("Could not create user profile due to conflicting data.");
        }
    }

    @Transactional(readOnly = true)
    public List<UserProfile> getUsers() {
        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(UserEntity::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDomain)
                .toList();
    }

    @Transactional
    public Event createEvent(
            String title,
            ActivityType activityType,
            String location,
            LocalDateTime startsAt,
            Integer pointsReward
    ) {
        String sanitizedTitle = requireNonBlank(title, "title");
        String sanitizedLocation = requireNonBlank(location, "location");
        int validPointsReward = requirePositive(pointsReward, "pointsReward");

        if (activityType == null) {
            throw new BadRequestException("activityType is required.");
        }
        if (startsAt == null) {
            throw new BadRequestException("startsAt is required.");
        }

        EventEntity eventEntity = new EventEntity(
                UUID.randomUUID(),
                sanitizedTitle,
                activityType,
                sanitizedLocation,
                startsAt,
                validPointsReward
        );

        return toDomain(eventRepository.save(eventEntity));
    }

    @Transactional(readOnly = true)
    public Event getEventById(UUID eventId) {
        if (eventId == null) {
            throw new BadRequestException("eventId is required.");
        }

        return toDomain(getEventEntityByIdOrThrow(eventId));
    }

    @Transactional(readOnly = true)
    public List<Event> getEvents() {
        return eventRepository.findAllByOrderByStartsAtAsc()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional
    public CheckInResult checkInToEvent(UUID eventId, String userQrCode) {
        if (eventId == null) {
            throw new BadRequestException("eventId is required.");
        }
        String sanitizedUserQrCode = requireNonBlank(userQrCode, "userQrCode");

        EventEntity event = getEventEntityByIdOrThrow(eventId);
        UserEntity userByQr = userRepository.findByQrCode(sanitizedUserQrCode)
                .orElseThrow(() -> new NotFoundException("User for QR code not found."));
        UserEntity user = getUserEntityByIdForUpdateOrThrow(userByQr.getId());

        if (eventCheckInRepository.existsByEventIdAndUserId(eventId, user.getId())) {
            throw new ConflictException("User already checked in for this event.");
        }

        try {
            eventCheckInRepository.save(new EventCheckInEntity(UUID.randomUUID(), eventId, user.getId()));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("User already checked in for this event.");
        }

        user.awardPoints(event.getActivityType(), event.getPointsReward());
        UserEntity updatedUser = userRepository.save(user);

        return new CheckInResult(eventId, updatedUser.getId(), event.getPointsReward(), updatedUser.getTotalPoints());
    }

    @Transactional
    public Challenge createChallenge(
            String title,
            String description,
            ActivityType activityType,
            ChallengeMode mode,
            ChallengeFrequency frequency,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            Integer pointsReward,
            Double targetValue,
            String targetUnit
    ) {
        String sanitizedTitle = requireNonBlank(title, "title");
        String sanitizedDescription = requireNonBlank(description, "description");
        int validPointsReward = requirePositive(pointsReward, "pointsReward");

        if (activityType == null) {
            throw new BadRequestException("activityType is required.");
        }
        if (mode == null) {
            throw new BadRequestException("mode is required.");
        }
        if (frequency == null) {
            throw new BadRequestException("frequency is required.");
        }
        if (startsAt != null && endsAt != null && endsAt.isBefore(startsAt)) {
            throw new BadRequestException("endsAt must be greater than startsAt.");
        }

        Double normalizedTargetValue = null;
        String normalizedTargetUnit = null;
        if (mode == ChallengeMode.ONLINE) {
            normalizedTargetValue = requirePositiveDouble(targetValue, "targetValue");
            normalizedTargetUnit = requireNonBlank(targetUnit, "targetUnit");
        }

        ChallengeEntity challengeEntity = new ChallengeEntity(
                UUID.randomUUID(),
                sanitizedTitle,
                sanitizedDescription,
                activityType,
                mode,
                frequency,
                startsAt,
                endsAt,
                validPointsReward,
                normalizedTargetValue,
                normalizedTargetUnit
        );

        return toDomain(challengeRepository.save(challengeEntity));
    }

    @Transactional(readOnly = true)
    public List<Challenge> getChallenges() {
        return challengeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ChallengeEntity::getStartsAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toDomain)
                .toList();
    }

    @Transactional
    public ChallengeSubmission submitChallenge(
            UUID challengeId,
            UUID userId,
            String proofText,
            Double metricValue
    ) {
        if (challengeId == null) {
            throw new BadRequestException("challengeId is required.");
        }
        if (userId == null) {
            throw new BadRequestException("userId is required.");
        }

        ChallengeEntity challenge = getChallengeEntityByIdOrThrow(challengeId);
        UserEntity user = getUserEntityByIdForUpdateOrThrow(userId);
        validateChallengeWindow(challenge);

        String sanitizedProofText = proofText == null ? null : proofText.trim();
        Double normalizedMetricValue = metricValue;

        if (challenge.getMode() == ChallengeMode.ONLINE) {
            normalizedMetricValue = requirePositiveDouble(metricValue, "metricValue");
            if (normalizedMetricValue < challenge.getTargetValue()) {
                throw new BadRequestException(
                        "Challenge target not reached. Required " + challenge.getTargetValue() + " " + challenge.getTargetUnit() + "."
                );
            }
        } else {
            sanitizedProofText = requireNonBlank(sanitizedProofText, "proofText");
        }

        ChallengeSubmissionEntity submission = new ChallengeSubmissionEntity(
                UUID.randomUUID(),
                challengeId,
                userId,
                sanitizedProofText,
                normalizedMetricValue,
                LocalDateTime.now()
        );

        if (challenge.getMode() == ChallengeMode.ONLINE) {
            submission.approve(
                    challenge.getPointsReward(),
                    "Auto-approved based on submitted metric."
            );
        }

        try {
            challengeSubmissionRepository.save(submission);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("This user already submitted this challenge.");
        }

        if (challenge.getMode() == ChallengeMode.ONLINE) {
            user.awardPoints(challenge.getActivityType(), challenge.getPointsReward());
            userRepository.save(user);
        }

        return toDomain(submission);
    }

    @Transactional(readOnly = true)
    public List<ChallengeSubmission> getChallengeSubmissions(UUID challengeId, UUID userId) {
        if (challengeId == null) {
            throw new BadRequestException("challengeId is required.");
        }
        getChallengeEntityByIdOrThrow(challengeId);

        List<ChallengeSubmissionEntity> submissions = userId == null
                ? challengeSubmissionRepository.findByChallengeIdOrderBySubmittedAtDesc(challengeId)
                : challengeSubmissionRepository.findByChallengeIdAndUserIdOrderBySubmittedAtDesc(challengeId, userId);
        return submissions.stream().map(this::toDomain).toList();
    }

    @Transactional
    public ChallengeSubmission reviewChallengeSubmission(
            UUID challengeId,
            UUID submissionId,
            Boolean approved,
            String reviewerNote
    ) {
        if (challengeId == null) {
            throw new BadRequestException("challengeId is required.");
        }
        if (submissionId == null) {
            throw new BadRequestException("submissionId is required.");
        }
        if (approved == null) {
            throw new BadRequestException("approved is required.");
        }

        ChallengeEntity challenge = getChallengeEntityByIdOrThrow(challengeId);
        ChallengeSubmissionEntity submission = challengeSubmissionRepository
                .findByIdAndChallengeIdForUpdate(submissionId, challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge submission not found."));

        if (submission.getStatus() != ChallengeSubmissionStatus.PENDING) {
            throw new ConflictException("Only pending submissions can be reviewed.");
        }

        if (approved) {
            submission.approve(challenge.getPointsReward(), reviewerNote);
            UserEntity user = getUserEntityByIdForUpdateOrThrow(submission.getUserId());
            user.awardPoints(challenge.getActivityType(), challenge.getPointsReward());
            userRepository.save(user);
        } else {
            submission.reject(reviewerNote);
        }

        return toDomain(challengeSubmissionRepository.save(submission));
    }

    @Transactional
    public Reward createReward(
            String title,
            String partnerName,
            String description,
            Integer pointsCost,
            Integer stock
    ) {
        String sanitizedTitle = requireNonBlank(title, "title");
        String sanitizedPartnerName = requireNonBlank(partnerName, "partnerName");
        String sanitizedDescription = requireNonBlank(description, "description");
        int validPointsCost = requirePositive(pointsCost, "pointsCost");

        Integer normalizedStock = null;
        if (stock != null) {
            if (stock <= 0) {
                throw new BadRequestException("stock must be greater than 0.");
            }
            normalizedStock = stock;
        }

        RewardEntity rewardEntity = new RewardEntity(
                UUID.randomUUID(),
                sanitizedTitle,
                sanitizedPartnerName,
                sanitizedDescription,
                validPointsCost,
                normalizedStock
        );

        return toDomain(rewardRepository.save(rewardEntity));
    }

    @Transactional(readOnly = true)
    public List<Reward> getRewards(Boolean includeInactive) {
        boolean shouldIncludeInactive = includeInactive != null && includeInactive;
        List<RewardEntity> rewards = shouldIncludeInactive
                ? rewardRepository.findAllByOrderByTitleAsc()
                : rewardRepository.findByActiveTrueOrderByTitleAsc();

        return rewards
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional
    public RewardRedemption redeemReward(UUID rewardId, UUID userId) {
        if (rewardId == null) {
            throw new BadRequestException("rewardId is required.");
        }
        if (userId == null) {
            throw new BadRequestException("userId is required.");
        }

        RewardEntity reward = rewardRepository.findByIdForUpdate(rewardId)
                .orElseThrow(() -> new NotFoundException("Reward not found."));
        if (!reward.isActive()) {
            throw new ConflictException("Reward is not active.");
        }

        UserEntity user = getUserEntityByIdForUpdateOrThrow(userId);

        boolean stockConsumed = reward.consumeOneStock();
        if (!stockConsumed) {
            throw new ConflictException("Reward is out of stock.");
        }

        boolean spent = user.spendPoints(reward.getPointsCost());
        if (!spent) {
            reward.restoreOneStock();
            throw new ConflictException("Insufficient available points.");
        }

        rewardRepository.save(reward);
        userRepository.save(user);

        RewardRedemptionEntity savedRedemption = saveRedemptionWithUniqueCode(
                reward.getId(),
                user.getId(),
                reward.getTitle(),
                reward.getPointsCost(),
                user.getAvailablePoints()
        );

        return toDomain(savedRedemption);
    }

    @Transactional(readOnly = true)
    public List<RewardRedemption> getRewardRedemptions(UUID userId) {
        if (userId == null) {
            return rewardRedemptionRepository.findAllByOrderByRedeemedAtDesc()
                    .stream()
                    .map(this::toDomain)
                    .toList();
        }

        getUserEntityByIdOrThrow(userId);
        return rewardRedemptionRepository.findByUserIdOrderByRedeemedAtDesc(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserProfile getUserById(UUID userId) {
        if (userId == null) {
            throw new BadRequestException("userId is required.");
        }
        return toDomain(getUserEntityByIdOrThrow(userId));
    }

    @Transactional(readOnly = true)
    public UserProfile getUserByAuthSubject(String authSubject) {
        String normalizedAuthSubject = normalizeRequired(authSubject, "authSubject");
        UserEntity user = userRepository.findByAuthSubject(normalizedAuthSubject)
                .orElseThrow(() -> new NotFoundException("No user profile is linked to the authenticated account."));
        return toDomain(user);
    }

    @Transactional(readOnly = true)
    public UUID resolveUserIdForAction(String authSubject, UUID fallbackUserId) {
        String normalizedAuthSubject = normalizeOptional(authSubject);
        if (normalizedAuthSubject != null) {
            UserEntity user = userRepository.findByAuthSubject(normalizedAuthSubject)
                    .orElseThrow(() -> new NotFoundException("No user profile is linked to the authenticated account."));
            if (fallbackUserId != null && !fallbackUserId.equals(user.getId())) {
                throw new ConflictException("Authenticated user cannot perform actions for another user.");
            }
            return user.getId();
        }

        if (fallbackUserId == null) {
            throw new BadRequestException("userId is required.");
        }

        return fallbackUserId;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getGeneralLeaderboard(Integer limit) {
        int normalizedLimit = normalizeLimit(limit);
        return userRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparingInt(UserEntity::getTotalPoints)
                                .reversed()
                                .thenComparing(UserEntity::getDisplayName, String.CASE_INSENSITIVE_ORDER)
                )
                .limit(normalizedLimit)
                .map(user -> new LeaderboardEntry(user.getId(), user.getDisplayName(), user.getTotalPoints()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getActivityLeaderboard(ActivityType activityType, Integer limit) {
        if (activityType == null) {
            throw new BadRequestException("activityType is required.");
        }

        int normalizedLimit = normalizeLimit(limit);
        return userRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparingInt((UserEntity user) -> user.pointsFor(activityType))
                                .reversed()
                                .thenComparing(UserEntity::getDisplayName, String.CASE_INSENSITIVE_ORDER)
                )
                .limit(normalizedLimit)
                .map(
                        user -> new LeaderboardEntry(
                                user.getId(),
                                user.getDisplayName(),
                                user.pointsFor(activityType)
                        )
                )
                .toList();
    }

    private int normalizeLimit(Integer rawLimit) {
        if (rawLimit == null) {
            return DEFAULT_LEADERBOARD_LIMIT;
        }
        if (rawLimit <= 0 || rawLimit > MAX_LEADERBOARD_LIMIT) {
            throw new BadRequestException("limit must be between 1 and " + MAX_LEADERBOARD_LIMIT + ".");
        }
        return rawLimit;
    }

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required.");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required.");
        }
        return value.trim();
    }

    private int requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new BadRequestException(fieldName + " must be a positive integer.");
        }
        return value;
    }

    private double requirePositiveDouble(Double value, String fieldName) {
        if (value == null || value <= 0) {
            throw new BadRequestException(fieldName + " must be a positive number.");
        }
        return value;
    }

    private void validateChallengeWindow(ChallengeEntity challenge) {
        LocalDateTime now = LocalDateTime.now();
        if (challenge.getStartsAt() != null && now.isBefore(challenge.getStartsAt())) {
            throw new ConflictException("Challenge has not started yet.");
        }
        if (challenge.getEndsAt() != null && now.isAfter(challenge.getEndsAt())) {
            throw new ConflictException("Challenge is already closed.");
        }
    }

    private UserEntity getUserEntityByIdOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    private UserEntity getUserEntityByIdForUpdateOrThrow(UUID userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    private EventEntity getEventEntityByIdOrThrow(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found."));
    }

    private ChallengeEntity getChallengeEntityByIdOrThrow(UUID challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("Challenge not found."));
    }

    private String generateUniqueQrCode() {
        for (int i = 0; i < 20; i++) {
            String qrCode = "NEV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (userRepository.findByQrCode(qrCode).isEmpty()) {
                return qrCode;
            }
        }
        throw new ConflictException("Could not generate unique QR code.");
    }

    private String generateRedemptionCode() {
        return "BEN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
    }

    private RewardRedemptionEntity saveRedemptionWithUniqueCode(
            UUID rewardId,
            UUID userId,
            String rewardTitle,
            int pointsSpent,
            int availablePointsAfter
    ) {
        for (int attempt = 0; attempt < 8; attempt++) {
            RewardRedemptionEntity redemption = new RewardRedemptionEntity(
                    UUID.randomUUID(),
                    rewardId,
                    userId,
                    rewardTitle,
                    pointsSpent,
                    generateRedemptionCode(),
                    LocalDateTime.now(),
                    availablePointsAfter
            );
            try {
                return rewardRedemptionRepository.save(redemption);
            } catch (DataIntegrityViolationException exception) {
                if (attempt == 7) {
                    throw new ConflictException("Could not generate unique redemption code.");
                }
            }
        }

        throw new ConflictException("Could not generate unique redemption code.");
    }

    private UserProfile toDomain(UserEntity user) {
        return new UserProfile(
                user.getId(),
                user.getDisplayName(),
                user.getQrCode(),
                user.getAuthSubject(),
                user.getTotalPoints(),
                user.getAvailablePoints(),
                user.getPointsPadel(),
                user.getPointsMountain(),
                user.getPointsRunning()
        );
    }

    private Event toDomain(EventEntity event) {
        return new Event(
                event.getId(),
                event.getTitle(),
                event.getActivityType(),
                event.getLocation(),
                event.getStartsAt(),
                event.getPointsReward()
        );
    }

    private Challenge toDomain(ChallengeEntity challenge) {
        return new Challenge(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getActivityType(),
                challenge.getMode(),
                challenge.getFrequency(),
                challenge.getStartsAt(),
                challenge.getEndsAt(),
                challenge.getPointsReward(),
                challenge.getTargetValue(),
                challenge.getTargetUnit()
        );
    }

    private ChallengeSubmission toDomain(ChallengeSubmissionEntity submission) {
        return new ChallengeSubmission(
                submission.getId(),
                submission.getChallengeId(),
                submission.getUserId(),
                submission.getProofText(),
                submission.getMetricValue(),
                submission.getSubmittedAt(),
                submission.getStatus(),
                submission.getAwardedPoints(),
                submission.getReviewedAt(),
                submission.getReviewerNote()
        );
    }

    private Reward toDomain(RewardEntity reward) {
        return new Reward(
                reward.getId(),
                reward.getTitle(),
                reward.getPartnerName(),
                reward.getDescription(),
                reward.getPointsCost(),
                reward.getStock(),
                reward.isActive()
        );
    }

    private RewardRedemption toDomain(RewardRedemptionEntity redemption) {
        return new RewardRedemption(
                redemption.getId(),
                redemption.getRewardId(),
                redemption.getUserId(),
                redemption.getRewardTitle(),
                redemption.getPointsSpent(),
                redemption.getRedemptionCode(),
                redemption.getRedeemedAt(),
                redemption.getUserAvailablePointsAfterRedemption()
        );
    }

    public record CheckInResult(
            UUID eventId,
            UUID userId,
            int pointsAwarded,
            int updatedTotalPoints
    ) {
    }

    public record LeaderboardEntry(
            UUID userId,
            String displayName,
            int points
    ) {
    }
}
