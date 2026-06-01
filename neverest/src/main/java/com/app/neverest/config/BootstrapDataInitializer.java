package com.app.neverest.config;

import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.ChallengeFrequency;
import com.app.neverest.domain.ChallengeMode;
import com.app.neverest.integration.strava.StravaService;
import com.app.neverest.persistence.entity.ChallengeEntity;
import com.app.neverest.persistence.entity.EventEntity;
import com.app.neverest.persistence.entity.RewardEntity;
import com.app.neverest.persistence.entity.RewardRedemptionEntity;
import com.app.neverest.persistence.entity.UserEntity;
import com.app.neverest.persistence.repository.ChallengeRepository;
import com.app.neverest.persistence.repository.EventRepository;
import com.app.neverest.persistence.repository.RewardRedemptionRepository;
import com.app.neverest.persistence.repository.RewardRepository;
import com.app.neverest.persistence.repository.StravaTokenRepository;
import com.app.neverest.persistence.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapDataInitializer implements CommandLineRunner {

    private static final UUID REWARD_TINY_CUP_ID = UUID.fromString("cfdd50ad-928b-47a8-8bee-2ecb4f89baba");
    private static final UUID REWARD_CARTURESTI_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID REWARD_BAZAR_MUZICA_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID REWARD_MIBE_PRINT_ID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ChallengeRepository challengeRepository;
    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository redemptionRepository;
    private final StravaTokenRepository stravaTokenRepository;
    private final StravaService stravaService;
    private final PasswordEncoder passwordEncoder;

    public BootstrapDataInitializer(
            UserRepository userRepository,
            EventRepository eventRepository,
            ChallengeRepository challengeRepository,
            RewardRepository rewardRepository,
            RewardRedemptionRepository redemptionRepository,
            StravaTokenRepository stravaTokenRepository,
            StravaService stravaService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.challengeRepository = challengeRepository;
        this.rewardRepository = rewardRepository;
        this.redemptionRepository = redemptionRepository;
        this.stravaTokenRepository = stravaTokenRepository;
        this.stravaService = stravaService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedDomainData();
    }

    private void seedUsers() {
        upsertUser(
                "serbancorodescu14@gmail.com",
                "Serban Corodescu",
                "NEV-U-SERBAN",
                "USER",
                "123456",
                120
        );
        upsertUser(
                "neverest@gmail.com",
                "Neverest Admin",
                "NEV-U-ADMIN",
                "ADMIN",
                "123456",
                220
        );
    }

    private void upsertUser(
            String email,
            String displayName,
            String qrCode,
            String role,
            String rawPassword,
            int starterPoints
    ) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        UserEntity user = userRepository.findByAuthSubjectIgnoreCase(normalizedEmail)
                .orElseGet(() -> {
                    UserEntity created = new UserEntity(
                            UUID.randomUUID(),
                            displayName,
                            qrCode,
                            normalizedEmail
                    );
                    if (starterPoints > 0) {
                        created.awardPoints(ActivityType.RUNNING, starterPoints);
                    }
                    return created;
                });

        if (user.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        user.setRole(role);
        userRepository.save(user);
    }

    private void seedDomainData() {
        if (eventRepository.count() == 0) {
            eventRepository.save(new EventEntity(
                    UUID.randomUUID(),
                    "Neverest Community Run",
                    ActivityType.RUNNING,
                    "Parcul Herastrau, Bucuresti",
                    LocalDateTime.now().plusDays(2),
                    80
            ));
        }

        if (challengeRepository.count() == 0) {
            challengeRepository.save(new ChallengeEntity(
                    UUID.randomUUID(),
                    "7K Weekly Run",
                    "Alearga minimum 7 km in aceasta saptamana.",
                    ActivityType.RUNNING,
                    ChallengeMode.ONLINE,
                    ChallengeFrequency.WEEKLY,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(21),
                    120,
                    7.0,
                    "km"
            ));
        }

        upsertReward(REWARD_TINY_CUP_ID,
                "Cafea gratuita",
                "Tiny Cup Specialty Coffee",
                "Un voucher pentru o cafea gratuita.",
                100, 30,
                "Strada Geoagiu 6, București");

        upsertReward(REWARD_CARTURESTI_ID,
                "Reducere 15% carte",
                "Cărturești",
                "15% reducere la orice carte din librarie.",
                80, null,
                "Calea Victoriei 45, București");

        upsertReward(REWARD_BAZAR_MUZICA_ID,
                "Discount 10% vinyl",
                "Bazar de Muzică",
                "10% reducere la viniluri si accesorii muzicale.",
                60, null,
                "Strada Mendeleev 8, București");

        upsertReward(REWARD_MIBE_PRINT_ID,
                "Print gratuit A4",
                "Mibe Print",
                "Un print gratuit A4 color sau alb-negru.",
                50, 50,
                "Calea București 2bis, Balotești");

        seedTestRedemption();
        seedStravaToken();
    }

    private void upsertReward(UUID id, String title, String partnerName, String description,
                              int pointsCost, Integer stock, String address) {
        if (!rewardRepository.existsById(id)) {
            rewardRepository.save(new RewardEntity(id, title, partnerName, description, pointsCost, stock, address));
        } else {
            rewardRepository.findById(id).ifPresent(r -> {
                if (r.getAddress() == null && address != null) {
                    r.setAddress(address);
                    rewardRepository.save(r);
                }
            });
        }
    }

    private void seedStravaToken() {
        userRepository.findByAuthSubjectIgnoreCase("serbancorodescu14@gmail.com").ifPresent(user -> {
            if (!stravaTokenRepository.existsByUserId(user.getId())) {
                // expires_at: 2026-06-01T20:35:13Z = 1748813713
                stravaService.saveTokenDirectly(
                        user.getId(),
                        12345678L,
                        "Serban Corodescu",
                        "05f2f751bbe0cf8cf1831a42534849fe50c2d748",
                        "ed71d2693da590aad6ad35d4d74fc5eed9e22747",
                        1748813713L,
                        "activity:read_all,read"
                );
            }
        });
    }

    private void seedTestRedemption() {
        UUID testRedemptionId = UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890123");
        if (!redemptionRepository.existsById(testRedemptionId)) {
            userRepository.findByAuthSubjectIgnoreCase("serbancorodescu14@gmail.com").ifPresent(user -> {
                RewardRedemptionEntity redemption = new RewardRedemptionEntity(
                        testRedemptionId,
                        REWARD_TINY_CUP_ID,
                        user.getId(),
                        "Cafea gratuita",
                        100,
                        "NEV-TEST-2025-001",
                        LocalDateTime.now().minusDays(3),
                        20
                );
                redemptionRepository.save(redemption);
            });
        }
    }
}
