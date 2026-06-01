package com.app.neverest.integration.strava;

public record StravaConnectionStatus(
        boolean connected,
        String athleteName,
        String athleteCity,
        boolean tokenExpired
) {}
