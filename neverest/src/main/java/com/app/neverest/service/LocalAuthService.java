package com.app.neverest.service;

import com.app.neverest.common.BadRequestException;
import com.app.neverest.common.ConflictException;
import com.app.neverest.persistence.entity.UserEntity;
import com.app.neverest.persistence.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocalAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long accessTokenTtlSeconds;

    public LocalAuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${neverest.auth.jwt-access-token-ttl-seconds:43200}") long accessTokenTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    @Transactional(readOnly = true)
    public LoginResult login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        String rawPassword = requireNonBlank(password, "password");

        UserEntity user = userRepository.findByAuthSubjectIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        String storedPasswordHash = user.getPasswordHash();
        if (storedPasswordHash == null || storedPasswordHash.isBlank()) {
            throw new BadCredentialsException("Invalid email or password.");
        }
        if (!passwordEncoder.matches(rawPassword, storedPasswordHash)) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        String normalizedRole = user.getRole() == null || user.getRole().isBlank()
                ? "USER"
                : user.getRole().trim().toUpperCase(Locale.ROOT);
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(accessTokenTtlSeconds, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(normalizedEmail)
                .claim("roles", List.of(normalizedRole))
                .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        return new LoginResult(accessToken, accessTokenTtlSeconds);
    }

    @Transactional
    public void register(
            String email,
            String password,
            String displayName,
            String phoneNumber,
            String avatarB64
    ) {
        String normalizedEmail = normalizeEmail(email);
        String rawPassword = requireNonBlank(password, "password");
        if (rawPassword.length() < 6) {
            throw new BadRequestException("password must contain at least 6 characters.");
        }
        // Phone number is mandatory at registration.
        String normalizedPhone = requireNonBlank(phoneNumber, "phoneNumber");

        if (userRepository.findByAuthSubjectIgnoreCase(normalizedEmail).isPresent()) {
            throw new ConflictException("An account with this email already exists.");
        }

        String effectiveDisplayName = normalizeDisplayName(displayName, normalizedEmail);
        String qrCode = generateUniqueQrCode();

        UserEntity user = new UserEntity(
                UUID.randomUUID(),
                effectiveDisplayName,
                qrCode,
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                "USER"
        );
        user.setPhoneNumber(normalizedPhone);
        if (avatarB64 != null && !avatarB64.isBlank()) {
            user.setAvatarB64(avatarB64);
        }

        userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        String value = requireNonBlank(email, "email");
        return value.toLowerCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String displayName, String email) {
        if (displayName != null && !displayName.isBlank()) {
            return displayName.trim();
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(0, atIndex);
        }
        return "Neverest User";
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

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required.");
        }
        return value.trim();
    }

    public record LoginResult(
            String accessToken,
            long expiresInSeconds
    ) {
    }
}
