package com.app.neverest.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/hello").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/strava/callback").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/events").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/events/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/*/check-ins").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/*/announcements/retry").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/challenges").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/challenges/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/challenges/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/challenges/*/submissions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/challenges/*/submissions/*/review").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/rewards/redemptions/validate").hasAnyRole("ADMIN", "PARTNER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/rewards").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/rewards/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/rewards/redemptions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/partners").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/admin/offers/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/offers/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/offers").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/offers/*").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/offers/*").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/offers/mine").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/partner-challenges/mine").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/partner-challenges").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/partner-challenges/*").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/partner-challenges/*").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/partner-rewards/mine").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/partner-rewards").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/partner-rewards/*").hasRole("PARTNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/partner-rewards/*").hasRole("PARTNER")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                ).oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${neverest.auth.jwt-secret}") String jwtSecret) {
        SecretKey secretKey = toSecretKey(jwtSecret);
        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(@Value("${neverest.auth.jwt-secret}") String jwtSecret) {
        SecretKey secretKey = toSecretKey(jwtSecret);
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
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

    private SecretKey toSecretKey(String rawSecret) {
        if (rawSecret == null || rawSecret.isBlank()) {
            throw new IllegalStateException("Set neverest.auth.jwt-secret with at least 32 characters.");
        }
        byte[] keyBytes = rawSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("neverest.auth.jwt-secret must have at least 32 characters.");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
}
