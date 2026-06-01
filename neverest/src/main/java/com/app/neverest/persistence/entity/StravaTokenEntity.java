package com.app.neverest.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nev_strava_tokens")
public class StravaTokenEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "athlete_id", nullable = false)
    private long athleteId;

    @Column(name = "athlete_name", length = 200)
    private String athleteName;

    @Column(name = "athlete_city", length = 200)
    private String athleteCity;

    @Column(name = "access_token", nullable = false, length = 300)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, length = 300)
    private String refreshToken;

    @Column(name = "expires_at", nullable = false)
    private long expiresAt;

    @Column(name = "scope", length = 200)
    private String scope;

    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    protected StravaTokenEntity() {}

    public StravaTokenEntity(UUID id, UUID userId, long athleteId, String athleteName,
                              String athleteCity, String accessToken, String refreshToken,
                              long expiresAt, String scope) {
        this.id = id;
        this.userId = userId;
        this.athleteId = athleteId;
        this.athleteName = athleteName;
        this.athleteCity = athleteCity;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.scope = scope;
        this.connectedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public long getAthleteId() { return athleteId; }
    public String getAthleteName() { return athleteName; }
    public String getAthleteCity() { return athleteCity; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public long getExpiresAt() { return expiresAt; }
    public String getScope() { return scope; }
    public LocalDateTime getConnectedAt() { return connectedAt; }

    public void updateTokens(String accessToken, String refreshToken, long expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }
}
