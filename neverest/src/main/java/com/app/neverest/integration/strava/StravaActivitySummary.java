package com.app.neverest.integration.strava;

public record StravaActivitySummary(
        long stravaId,
        String name,
        String type,
        double distanceMeters,
        int movingTimeSeconds,
        int elapsedTimeSeconds,
        String startDateLocal,
        double averageSpeedMs,
        double totalElevationGain,
        double[] startLatLng,
        double[] endLatLng
) {}
