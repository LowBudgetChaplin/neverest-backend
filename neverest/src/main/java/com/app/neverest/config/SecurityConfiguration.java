package com.app.neverest.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.convert.converter.Converter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String FIREBASE_JWK_SET_URI =
            "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";

    @Bean
    @ConditionalOnProperty(name = "neverest.auth.provider", havingValue = "none", matchIfMissing = true)
    public SecurityFilterChain openSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "neverest.auth.provider", havingValue = "firebase")
    public SecurityFilterChain firebaseSecurityFilterChain(
            HttpSecurity http,
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/hello").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/events").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/*/check-ins").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/*/announcements/retry").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/challenges").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/challenges/*/submissions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/challenges/*/submissions/*/review").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/rewards").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/rewards/redemptions").hasRole("ADMIN")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "neverest.auth.provider", havingValue = "firebase")
    public JwtDecoder jwtDecoder(@Value("${neverest.firebase.project-id:}") String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException("Set neverest.firebase.project-id when neverest.auth.provider=firebase.");
        }

        String issuer = "https://securetoken.google.com/" + projectId;
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(FIREBASE_JWK_SET_URI).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new FirebaseAudienceValidator(projectId);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    @Bean
    @ConditionalOnProperty(name = "neverest.auth.provider", havingValue = "firebase")
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Set<String> roles = extractRoles(jwt);
            if (roles.isEmpty()) {
                roles.add("USER");
            }

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            for (String role : roles) {
                if (role == null || role.isBlank()) {
                    continue;
                }

                String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
                if (!normalizedRole.startsWith("ROLE_")) {
                    normalizedRole = "ROLE_" + normalizedRole;
                }

                authorities.add(new SimpleGrantedAuthority(normalizedRole));
            }

            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }

    private Set<String> extractRoles(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        Object rolesClaim = jwt.getClaims().get("roles");
        if (rolesClaim instanceof Collection<?> collection) {
            for (Object role : collection) {
                if (role != null) {
                    roles.add(role.toString());
                }
            }
        } else if (rolesClaim instanceof String rolesAsString) {
            String[] splitRoles = rolesAsString.split(",");
            for (String role : splitRoles) {
                if (role != null && !role.isBlank()) {
                    roles.add(role);
                }
            }
        }

        Object roleClaim = jwt.getClaims().get("role");
        if (roleClaim instanceof String roleAsString && !roleAsString.isBlank()) {
            roles.add(roleAsString);
        }

        return roles;
    }

    private static final class FirebaseAudienceValidator implements OAuth2TokenValidator<Jwt> {

        private static final OAuth2Error ERROR = new OAuth2Error(
                "invalid_token",
                "Firebase token audience does not match configured project id.",
                null
        );

        private final String projectId;

        private FirebaseAudienceValidator(String projectId) {
            this.projectId = projectId;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            List<String> audience = jwt.getAudience();
            if (audience != null && audience.contains(projectId)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(ERROR);
        }
    }
}
