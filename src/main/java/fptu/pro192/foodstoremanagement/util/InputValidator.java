package fptu.pro192.foodstoremanagement.util;

import java.time.LocalDate;

/**
 * Utility class for validating and normalizing user inputs according to business rules.
 */
public final class InputValidator {

    private InputValidator() {
        // Prevent instantiation
    }

    /**
     * Normalizes raw phone number strings to a standard 10-digit format starting with 0.
     *
     * BR25: Customer phone number inputs in formats such as +84372240629, +840372240629, (+84) 37
     * 224 0629, 037.224.0629, or 0372240629 must be normalized to standard 10-digit format starting
     * with 0 (e.g., 0372240629) before persistence.
     *
     * @param rawPhone The raw input phone string
     * @return Normalized 10-digit phone number string starting with 0
     * @throws IllegalArgumentException If phone number is null, empty, or format is invalid
     */
    public static String normalizePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }

        // Strip non-digit and non-plus characters
        String normalized = rawPhone.replaceAll("[^0-9+]", "");

        // Handle Vietnamese international prefixes (+840, 840, +84, 84)
        normalized = normalized.replaceFirst("^(\\+?840|\\+?84)", "0");

        // Validate strictly against standard 10-digit format starting with 0[3|5|7|8|9]
        if (!normalized.matches("^0[35789]\\d{8}$")) {
            throw new IllegalArgumentException(
                    "Invalid Vietnamese phone number format: " + rawPhone);
        }

        return normalized;
    }

    /**
     * Backward compatibility alias for {@link #normalizePhone(String)}.
     *
     * @param rawNumbers Raw phone input
     * @return Normalized phone number
     */
    public static String normalPhoneNumbers(String rawNumbers) {
        return normalizePhone(rawNumbers);
    }

    /**
     * BR7: Validates that production date is not after expiration date and not in the future.
     *
     * @param productionDate Production date
     * @param expirationDate Expiration date
     * @return true if dates are valid; false otherwise
     */
    public static boolean isValidDate(LocalDate productionDate, LocalDate expirationDate) {
        if (productionDate == null || expirationDate == null) {
            return false;
        }
        if (productionDate.isAfter(expirationDate)) {
            return false;
        }
        if (productionDate.isAfter(LocalDate.now())) {
            return false;
        }
        return true;
    }
}
