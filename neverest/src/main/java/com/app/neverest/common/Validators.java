package com.app.neverest.common;

import java.util.regex.Pattern;

public final class Validators {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,15}$");

    private Validators() {
    }

    public static String requireValidEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("email is required.");
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new BadRequestException("email format is invalid.");
        }
        return trimmed;
    }

    public static String requireValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BadRequestException("phoneNumber is required.");
        }
        String normalized = phone.trim().replaceAll("[\\s()\\-]", "");
        if (!PHONE.matcher(normalized).matches()) {
            throw new BadRequestException("phoneNumber format is invalid.");
        }
        return normalized;
    }
}
