package com.app.neverest.api;

import com.app.neverest.common.AuthUtils;
import com.app.neverest.common.BadRequestException;
import com.app.neverest.integration.strava.StravaChallengeVerification;
import com.app.neverest.integration.strava.StravaActivitySummary;
import com.app.neverest.integration.strava.StravaConnectionStatus;
import com.app.neverest.integration.strava.StravaService;
import com.app.neverest.service.NeverestCoreService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/strava")
public class StravaController {

    private final StravaService stravaService;
    private final NeverestCoreService coreService;

    public StravaController(StravaService stravaService, NeverestCoreService coreService) {
        this.stravaService = stravaService;
        this.coreService = coreService;
    }

    @GetMapping("/connect-url")
    public Map<String, String> getConnectUrl(Authentication authentication) {
        String subject = AuthUtils.requireSubject(authentication);
        UUID userId = coreService.resolveUserIdForAction(subject, null);
        String url = stravaService.buildAuthUrl(userId.toString());
        return Map.of("url", url);
    }

    @GetMapping("/callback")
    public RedirectView handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error
    ) {
        if (error != null || code == null) {
            return new RedirectView("neverest://strava?status=error&reason=" + (error != null ? error : "missing_code"));
        }
        try {
            stravaService.exchangeCodeAndStore(code, state);
            return new RedirectView("neverest://strava?status=connected");
        } catch (Exception e) {
            return new RedirectView("neverest://strava?status=error&reason=exchange_failed");
        }
    }

    @GetMapping("/status")
    public StravaConnectionStatus getStatus(Authentication authentication) {
        String subject = AuthUtils.requireSubject(authentication);
        UUID userId = coreService.resolveUserIdForAction(subject, null);
        return stravaService.getStatus(userId);
    }

    @GetMapping("/activities/recent")
    public List<StravaActivitySummary> getRecentActivities(
            Authentication authentication,
            @RequestParam(defaultValue = "5") int limit
    ) {
        String subject = AuthUtils.requireSubject(authentication);
        UUID userId = coreService.resolveUserIdForAction(subject, null);
        try {
            return stravaService.getRecentActivities(userId, Math.min(limit, 20));
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Failed to fetch Strava activities: " + e.getMessage());
        }
    }

    @GetMapping("/verify/challenge/{challengeId}")
    public StravaChallengeVerification verifyChallenge(
            @PathVariable UUID challengeId,
            Authentication authentication
    ) {
        String subject = AuthUtils.requireSubject(authentication);
        UUID userId = coreService.resolveUserIdForAction(subject, null);
        try {
            return stravaService.verifyChallenge(userId, challengeId, coreService);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Eroare la verificarea Strava: " + e.getMessage());
        }
    }

    @DeleteMapping("/disconnect")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect(Authentication authentication) {
        String subject = AuthUtils.requireSubject(authentication);
        UUID userId = coreService.resolveUserIdForAction(subject, null);
        stravaService.disconnect(userId);
    }
}
