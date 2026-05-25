package com.app.neverest.config;

import com.app.neverest.domain.ActivityType;
import com.app.neverest.domain.ChallengeFrequency;
import com.app.neverest.domain.ChallengeMode;
import com.app.neverest.persistence.entity.ChallengeEntity;
import com.app.neverest.persistence.entity.EventEntity;
import com.app.neverest.persistence.entity.RewardEntity;
import com.app.neverest.persistence.entity.UserEntity;
import com.app.neverest.persistence.repository.ChallengeRepository;
import com.app.neverest.persistence.repository.EventRepository;
import com.app.neverest.persistence.repository.RewardRepository;
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

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ChallengeRepository challengeRepository;
    private final RewardRepository rewardRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapDataInitializer(
            UserRepository userRepository,
            EventRepository eventRepository,
            ChallengeRepository challengeRepository,
            RewardRepository rewardRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.challengeRepository = challengeRepository;
        this.rewardRepository = rewardRepository;
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

        if (rewardRepository.count() == 0) {
            rewardRepository.save(new RewardEntity(
                    UUID.randomUUID(),
                    "Cafea gratuita",
                    "Neverest Cafe Partner",
                    "Un voucher pentru o cafea gratuita.",
                    100,
                    30
            ));
        }
    }
}
