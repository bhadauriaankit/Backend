package com.ankit.elearning.util;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates that an email address:
 *  1. Matches a basic RFC-5321 pattern
 *  2. Is NOT from a known disposable/throwaway email provider
 */
public class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    /** Known disposable / temporary email domains to block. */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "guerrillamail.com", "tempmail.com", "throwaway.email",
            "yopmail.com", "sharklasers.com", "guerrillamail.info", "guerrillamail.biz",
            "guerrillamail.de", "guerrillamail.net", "guerrillamail.org",
            "spam4.me", "trashmail.com", "trashmail.me", "trashmail.net",
            "trashmail.at", "trashmail.io", "trashmail.xyz", "dispostable.com",
            "mailnull.com", "maildrop.cc", "fakeinbox.com", "temp-mail.org",
            "tempmail.net", "10minutemail.com", "10minutemail.net", "10mail.org",
            "getnada.com", "mailnesia.com", "spamgourmet.com", "spamgourmet.net",
            "wegwerfmail.de", "wegwerfmail.net", "wegwerfmail.org", "discard.email",
            "crazymailing.com", "inoutmail.eu", "inoutmail.net",
            "thrma.com", "throam.com", "filzmail.com", "spamtrap.ro"
    );

    /**
     * Returns true if the email is syntactically valid AND not from a disposable domain.
     * Never throws — returns false on any edge case.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        String trimmed = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) return false;
        int at = trimmed.lastIndexOf('@');
        if (at < 0) return false;
        String domain = trimmed.substring(at + 1);
        return !DISPOSABLE_DOMAINS.contains(domain);
    }
}