package com.app.neverest.service;

import com.app.neverest.persistence.entity.NotificationEntity;
import com.app.neverest.persistence.entity.UserEntity;
import com.app.neverest.persistence.repository.NotificationRepository;
import com.app.neverest.persistence.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * In-app notifications. Backend-persisted so the unread badge survives app
 * restarts and so admins are reliably notified of pending submissions.
 *
 * Notification text is rendered in Romanian to match the rest of the backend
 * (e.g. Strava verification messages). If the app later needs per-locale text,
 * switch to sending {@code type} + params and translate on the client.
 */
@Service
public class NotificationService {

    public static final String TYPE_SUBMISSION_PENDING = "SUBMISSION_PENDING";
    public static final String TYPE_SUBMISSION_APPROVED = "SUBMISSION_APPROVED";
    public static final String TYPE_SUBMISSION_REJECTED = "SUBMISSION_REJECTED";
    public static final String TYPE_REWARD_REDEEMED = "REWARD_REDEEMED";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notifyUserSubmissionApproved(
            UUID userId, String challengeTitle, int points, UUID challengeId, UUID submissionId
    ) {
        create(
                userId,
                TYPE_SUBMISSION_APPROVED,
                "Provocare validată",
                "Felicitări! Provocarea „" + challengeTitle + "” a fost validată. +" + points + " puncte.",
                challengeId,
                submissionId
        );
    }

    @Transactional
    public void notifyUserSubmissionRejected(
            UUID userId, String challengeTitle, String reviewerNote, UUID challengeId, UUID submissionId
    ) {
        String reason = (reviewerNote == null || reviewerNote.isBlank())
                ? ""
                : " Motiv: " + reviewerNote.trim();
        create(
                userId,
                TYPE_SUBMISSION_REJECTED,
                "Provocare respinsă",
                "Trimiterea pentru „" + challengeTitle + "” nu a fost aprobată." + reason,
                challengeId,
                submissionId
        );
    }

    @Transactional
    public void notifyPartnerRedemption(
            UUID partnerUserId, String rewardTitle, String code, String userName
    ) {
        create(
                partnerUserId,
                TYPE_REWARD_REDEEMED,
                "Cod de reducere revendicat",
                userName + " a revendicat „" + rewardTitle + "”. Cod unic: " + code
                        + ". Scanează codul la prezentare pentru a-l valida.",
                null,
                null
        );
    }

    @Transactional
    public void notifyAdminsNewPendingSubmission(
            String challengeTitle, String submitterName, UUID challengeId, UUID submissionId
    ) {
        List<UserEntity> admins = userRepository.findByRole("ADMIN");
        for (UserEntity admin : admins) {
            create(
                    admin.getId(),
                    TYPE_SUBMISSION_PENDING,
                    "Trimitere nouă de validat",
                    submitterName + " a trimis o dovadă pentru „" + challengeTitle + "” care necesită revizuire.",
                    challengeId,
                    submissionId
            );
        }
    }

    private void create(UUID userId, String type, String title, String body, UUID challengeId, UUID submissionId) {
        if (userId == null) {
            return;
        }
        notificationRepository.save(new NotificationEntity(
                UUID.randomUUID(), userId, type, title, body, challengeId, submissionId, LocalDateTime.now()
        ));
    }

    @Transactional(readOnly = true)
    public List<NotificationEntity> list(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        return notificationRepository.findTop100ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        if (userId == null) {
            return 0;
        }
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        if (userId == null) {
            return;
        }
        notificationRepository.markAllRead(userId);
    }

    @Transactional
    public void deleteForChallenge(UUID challengeId) {
        if (challengeId == null) {
            return;
        }
        notificationRepository.deleteByChallengeId(challengeId);
    }
}
