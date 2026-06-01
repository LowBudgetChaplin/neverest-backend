package com.app.neverest.integration.strava;

import java.util.List;

public record StravaChallengeVerification(
        boolean stravaConnected,
        boolean verified,
        String verificationMessage,
        List<StravaActivitySummary> matchingActivities,
        double requiredDistanceKm
) {}
