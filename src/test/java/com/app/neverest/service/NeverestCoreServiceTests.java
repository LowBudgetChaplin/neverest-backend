package com.app.neverest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.app.neverest.common.ConflictException;
import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.ChallengeFrequency;
import com.app.neverest.domain.ChallengeMode;
import com.app.neverest.domain.ChallengeSubmissionStatus;
import com.app.neverest.domain.UserProfile;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class NeverestCoreServiceTests {

    @Autowired
    private NeverestCoreService service;

    @Test
    void onlineChallengeIsAutoApprovedAndAwardsPoints() {
        UserProfile user = service.createUser("Andrei");

        var challenge = service.createChallenge(
                "Walk challenge",
                "7km in one session",
                ActivityType.RUNNING,
                ChallengeMode.ONLINE,
                ChallengeFrequency.WEEKLY,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(7),
                35,
                7.0,
                "km"
        );

        var submission = service.submitChallenge(challenge.id(), user.id(), null, 8.2);
        UserProfile updatedUser = service.getUserById(user.id());

        assertEquals(ChallengeSubmissionStatus.APPROVED, submission.status());
        assertEquals(35, submission.awardedPoints());
        assertEquals(35, updatedUser.totalPoints());
        assertEquals(35, updatedUser.availablePoints());
    }

    @Test
    void approvedOfflineSubmissionAwardsPoints() {
        UserProfile user = service.createUser("Maria");

        var challenge = service.createChallenge(
                "Mountain meetup",
                "Attend monthly group session",
                ActivityType.MOUNTAIN,
                ChallengeMode.OFFLINE,
                ChallengeFrequency.MONTHLY,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(28),
                50,
                null,
                null
        );

        var submission = service.submitChallenge(
                challenge.id(),
                user.id(),
                "Participare confirmata",
                null
        );

        assertEquals(ChallengeSubmissionStatus.PENDING, submission.status());
        service.reviewChallengeSubmission(challenge.id(), submission.id(), true, "Approved by admin");
        UserProfile updatedUser = service.getUserById(user.id());

        assertEquals(50, updatedUser.totalPoints());
        assertEquals(50, updatedUser.availablePoints());
    }

    @Test
    void redeemRewardConsumesAvailablePoints() {
        UserProfile user = service.createUser("Alex");

        var event = service.createEvent(
                "Padel training",
                ActivityType.PADEL,
                "Court 1",
                LocalDateTime.now().plusDays(1),
                100
        );
        service.checkInToEvent(event.id(), user.qrCode());

        var reward = service.createReward(
                "Cafe voucher",
                "Cafe Local",
                "One free drink",
                60,
                1
        );

        var redemption = service.redeemReward(reward.id(), user.id());
        UserProfile updatedUser = service.getUserById(user.id());
        assertEquals(60, redemption.pointsSpent());
        assertEquals(100, updatedUser.totalPoints());
        assertEquals(40, updatedUser.availablePoints());

        assertThrows(ConflictException.class, () -> service.redeemReward(reward.id(), user.id()));
    }

    @Test
    void authSubjectIsLinkedToSingleUserAndUsedForActionResolution() {
        UserProfile linkedUser = service.createUser("Ioana", "firebase-uid-1");

        assertThrows(ConflictException.class, () -> service.createUser("Duplicate", "firebase-uid-1"));
        assertEquals(linkedUser.id(), service.resolveUserIdForAction("firebase-uid-1", null));
        assertEquals(linkedUser.id(), service.resolveUserIdForAction("firebase-uid-1", linkedUser.id()));
        assertThrows(
                ConflictException.class,
                () -> service.resolveUserIdForAction("firebase-uid-1", java.util.UUID.randomUUID())
        );
    }
}
