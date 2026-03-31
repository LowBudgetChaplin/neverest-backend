package com.app.neverest.common;

import org.springframework.security.core.Authentication;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static String actor(Authentication authentication) {
        String subject = subjectOrNull(authentication);
        if (subject == null) {
            return "anonymous";
        }
        return subject;
    }

    public static String subjectOrNull(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }

        return name;
    }

    public static String requireSubject(Authentication authentication) {
        String subject = subjectOrNull(authentication);
        if (subject == null) {
            throw new BadRequestException("Authenticated user is required.");
        }
        return subject;
    }
}
