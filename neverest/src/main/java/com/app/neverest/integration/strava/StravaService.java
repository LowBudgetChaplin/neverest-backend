package com.app.neverest.integration.strava;

import com.app.neverest.domain.Challenge;
import com.app.neverest.persistence.entity.StravaTokenEntity;
import com.app.neverest.persistence.entity.UserEntity;
import com.app.neverest.persistence.repository.StravaTokenRepository;
import com.app.neverest.persistence.repository.UserRepository;
import com.app.neverest.service.NeverestCoreService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StravaService {

    private static final Logger log = LoggerFactory.getLogger(StravaService.class);
    private static final String STRAVA_AUTH_URL = "https://www.strava.com/oauth/authorize";
    private static final String STRAVA_TOKEN_URL = "https://www.strava.com/oauth/token";
    private static final String STRAVA_API_BASE = "https://www.strava.com/api/v3";

    private final StravaTokenRepository stravaTokenRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${neverest.strava.client-id}")
    private String clientId;

    @Value("${neverest.strava.client-secret}")
    private String clientSecret;

    @Value("${neverest.strava.redirect-uri}")
    private String redirectUri;

    public StravaService(StravaTokenRepository stravaTokenRepository,
                         UserRepository userRepository,
                         ObjectMapper objectMapper) {
        this.stravaTokenRepository = stravaTokenRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String buildAuthUrl(String state) {
        return STRAVA_AUTH_URL
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&approval_prompt=force"
                + "&scope=" + URLEncoder.encode("read,activity:read_all", StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
    }

    @Transactional
    public StravaConnectionStatus exchangeCodeAndStore(String code, String state) throws IOException, InterruptedException {
        // Exchange code for tokens
        String body = "client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&grant_type=authorization_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STRAVA_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = objectMapper.readTree(response.body());

        if (response.statusCode() != 200) {
            log.error("Strava token exchange failed: {}", response.body());
            throw new RuntimeException("Strava token exchange failed: " + json.path("message").asText());
        }

        String accessToken = json.path("access_token").asText();
        String refreshToken = json.path("refresh_token").asText();
        long expiresAt = json.path("expires_at").asLong();
        String scope = json.path("scope").asText("activity:read_all");

        JsonNode athlete = json.path("athlete");
        long athleteId = athlete.path("id").asLong();
        String firstName = athlete.path("firstname").asText("");
        String lastName = athlete.path("lastname").asText("");
        String athleteName = (firstName + " " + lastName).trim();
        String athleteCity = athlete.path("city").asText("");

        // Find user by state (userId or email)
        Optional<UserEntity> userOpt = findUserByState(state);
        if (userOpt.isEmpty()) {
            log.warn("No user found for Strava state: {}", state);
            return new StravaConnectionStatus(false, null, null, false);
        }
        UUID userId = userOpt.get().getId();

        // Upsert token
        Optional<StravaTokenEntity> existing = stravaTokenRepository.findByUserId(userId);
        if (existing.isPresent()) {
            existing.get().updateTokens(accessToken, refreshToken, expiresAt);
            stravaTokenRepository.save(existing.get());
        } else {
            stravaTokenRepository.save(new StravaTokenEntity(
                    UUID.randomUUID(), userId, athleteId, athleteName,
                    athleteCity, accessToken, refreshToken, expiresAt, scope
            ));
        }

        return new StravaConnectionStatus(true, athleteName, athleteCity, false);
    }

    @Transactional(readOnly = true)
    public StravaConnectionStatus getStatus(UUID userId) {
        return stravaTokenRepository.findByUserId(userId)
                .map(t -> new StravaConnectionStatus(true, t.getAthleteName(), t.getAthleteCity(), isExpired(t)))
                .orElse(new StravaConnectionStatus(false, null, null, false));
    }

    @Transactional(readOnly = true)
    public List<StravaActivitySummary> getRecentActivities(UUID userId, int limit) throws IOException, InterruptedException {
        Optional<StravaTokenEntity> tokenOpt = stravaTokenRepository.findByUserId(userId);
        if (tokenOpt.isEmpty()) return List.of();

        StravaTokenEntity token = tokenOpt.get();
        String accessToken = ensureFreshToken(token);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STRAVA_API_BASE + "/athlete/activities?per_page=" + limit))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            log.error("Strava activities fetch failed: {}", response.body());
            return List.of();
        }

        JsonNode arr = objectMapper.readTree(response.body());
        List<StravaActivitySummary> result = new ArrayList<>();
        for (JsonNode node : arr) {
            result.add(new StravaActivitySummary(
                    node.path("id").asLong(),
                    node.path("name").asText("Activity"),
                    node.path("type").asText("Run"),
                    node.path("distance").asDouble(0),
                    node.path("moving_time").asInt(0),
                    node.path("elapsed_time").asInt(0),
                    node.path("start_date_local").asText(""),
                    node.path("average_speed").asDouble(0),
                    node.path("total_elevation_gain").asDouble(0),
                    parseLatLng(node.path("start_latlng")),
                    parseLatLng(node.path("end_latlng"))
            ));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public StravaChallengeVerification verifyChallenge(UUID userId, UUID challengeId,
                                                        NeverestCoreService coreService)
            throws IOException, InterruptedException {

        // Check Strava connection
        Optional<StravaTokenEntity> tokenOpt = stravaTokenRepository.findByUserId(userId);
        if (tokenOpt.isEmpty()) {
            return new StravaChallengeVerification(false, false,
                    "Strava nu este conectat. Conectează-ți contul Strava mai întâi.", List.of(), 0);
        }

        // Get challenge details
        Challenge challenge = coreService.getChallengeById(challengeId);
        if (challenge == null) {
            return new StravaChallengeVerification(true, false,
                    "Challenge-ul nu a fost găsit.", List.of(), 0);
        }

        double requiredKm = challenge.targetValue() != null ? challenge.targetValue() : 0;
        String activityType = mapActivityType(challenge.activityType().name());

        // Fetch recent activities (last 20 for better coverage)
        List<StravaActivitySummary> activities = getRecentActivities(userId, 20);

        // Find matching activities: same type + enough distance
        List<StravaActivitySummary> matching = activities.stream()
                .filter(a -> activityTypeMatches(a.type(), activityType))
                .filter(a -> a.distanceMeters() >= requiredKm * 1000 * 0.95) // 5% tolerance
                .toList();

        if (matching.isEmpty()) {
            String msg = requiredKm > 0
                    ? String.format("Nu s-a găsit nicio activitate Strava de tip %s cu minim %.1f km.", activityType, requiredKm)
                    : "Nu s-a găsit nicio activitate Strava potrivită.";
            return new StravaChallengeVerification(true, false, msg, List.of(), requiredKm);
        }

        String msg = String.format("✓ Găsit %d activit%s Strava care îndeplinesc cerința de %.1f km.",
                matching.size(), matching.size() == 1 ? "ate" : "ăți", requiredKm);
        return new StravaChallengeVerification(true, true, msg, matching, requiredKm);
    }

    private String mapActivityType(String neverestType) {
        return switch (neverestType.toUpperCase()) {
            case "RUNNING" -> "Run";
            case "MOUNTAIN" -> "Hike";
            case "PADEL" -> "Workout";
            default -> "Run";
        };
    }

    private boolean activityTypeMatches(String stravaType, String expected) {
        if (stravaType == null) return false;
        String s = stravaType.toLowerCase();
        return switch (expected.toLowerCase()) {
            case "run" -> s.contains("run") || s.contains("jog");
            case "hike" -> s.contains("hike") || s.contains("walk") || s.contains("trail");
            case "workout" -> true; // accept all
            default -> true;
        };
    }

    @Transactional
    public void saveTokenDirectly(UUID userId, long athleteId, String athleteName,
                                   String accessToken, String refreshToken, long expiresAt, String scope) {
        Optional<StravaTokenEntity> existing = stravaTokenRepository.findByUserId(userId);
        if (existing.isPresent()) {
            existing.get().updateTokens(accessToken, refreshToken, expiresAt);
            stravaTokenRepository.save(existing.get());
        } else {
            stravaTokenRepository.save(new StravaTokenEntity(
                    UUID.randomUUID(), userId, athleteId, athleteName, "",
                    accessToken, refreshToken, expiresAt, scope
            ));
        }
    }

    @Transactional
    public void disconnect(UUID userId) {
        stravaTokenRepository.deleteByUserId(userId);
    }

    private String ensureFreshToken(StravaTokenEntity token) throws IOException, InterruptedException {
        if (!isExpired(token)) {
            return token.getAccessToken();
        }
        // Refresh
        String body = "client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&refresh_token=" + token.getRefreshToken()
                + "&grant_type=refresh_token";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STRAVA_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode json = objectMapper.readTree(response.body());
        String newAccess = json.path("access_token").asText(token.getAccessToken());
        String newRefresh = json.path("refresh_token").asText(token.getRefreshToken());
        long newExpiry = json.path("expires_at").asLong(token.getExpiresAt());
        token.updateTokens(newAccess, newRefresh, newExpiry);
        stravaTokenRepository.save(token);
        return newAccess;
    }

    private boolean isExpired(StravaTokenEntity token) {
        return Instant.now().getEpochSecond() >= token.getExpiresAt() - 300;
    }

    private Optional<UserEntity> findUserByState(String state) {
        if (state == null || state.isBlank()) return Optional.empty();
        try {
            UUID userId = UUID.fromString(state);
            return userRepository.findById(userId);
        } catch (IllegalArgumentException e) {
            // state might be email
            return userRepository.findByAuthSubjectIgnoreCase(state);
        }
    }

    private double[] parseLatLng(JsonNode node) {
        if (node == null || !node.isArray() || node.size() < 2) return null;
        return new double[]{node.get(0).asDouble(), node.get(1).asDouble()};
    }
}
