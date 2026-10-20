package fptu.pro192.foodstoremanagement.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Centralized stateless validator and normalizer for entity identifiers,
 * Unicode names and addresses, CSV formula injection guards, Vietnamese
 * mobile phone numbers, prices, integer quantities, and dd/MM/yyyy dates.
 */
public final class InputValidator {

    /**
     * Standard currency display symbol.
     */
    public static final String CURRENCY_UNIT = "VND";

    /**
     * Standard baseline retail categories.
     */
    public static final Set<String> DEFAULT_CATEGORIES = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList(
                    "Grains", "Noodles", "Meat", "Seafood", "Produce",
                    "Dairy", "Frozen", "Canned", "Beverages", "Condiments",
                    "Snacks", "Bakery")));

    /**
     * Standard baseline retail measurement units.
     */
    public static final Set<String> DEFAULT_UNITS = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList(
                    "Pack", "Box", "Bottle", "Can", "Bag", "Tray",
                    "Kg", "Gram", "Jar", "Carton")));

    /**
     * Standard console date display and input pattern (dd/MM/yyyy).
     */
    public static final String DISPLAY_DATE_PATTERN = "dd/MM/yyyy";

    /**
     * Standard console month-year input pattern (MM/yyyy).
     */
    public static final String DISPLAY_MONTH_PATTERN = "MM/yyyy";

    private static final DateTimeFormatter STRICT_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter STRICT_MONTH_FORMATTER = DateTimeFormatter
            .ofPattern("MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern PHONE_REGEX = Pattern.compile("^0[35789]\\d{8}$");

    private static final Pattern UNICODE_LETTER_CHECK = Pattern.compile(".*\\p{L}.*");

    private static final Pattern SAFE_TEXT_REGEX = Pattern
            .compile("^[\\p{L}\\p{M}\\p{N}\\s.,'\"()/\\-&]+$");

    private static final Pattern SINGLE_WORD_TOKEN_REGEX = Pattern
            .compile("^[\\p{L}\\p{N}\\-]+$");

    private InputValidator() {
        throw new AssertionError("InputValidator cannot be instantiated.");
    }

    /**
     * Validates and normalizes an entity identifier to the canonical 6-character
     * format consisting of the uppercase prefix followed by 5 digits (BR1, BR2,
     * BR6, BR8).
     * Accepts shorthand numeric input such as "1" or "P1" and pads it to "P00001".
     * Rejects identifiers whose numeric portion is outside 1..99999 or that use a
     * non-canonical digit count when a prefix is already supplied (unless shorthand
     * padding
     * is explicitly enabled).
     *
     * @param raw                   the raw identifier string
     * @param expectedPrefix        the required uppercase prefix ('P', 'B', 'C', or
     *                              'O')
     * @param allowShorthandPadding true to auto-pad shorthand inputs like "1" or
     *                              "P1"
     * @return the normalized 6-character identifier (for example, "P00001")
     * @throws IllegalArgumentException if raw is null, blank, or does not match the
     *                                  prefix and range
     */
    public static String normalizeId(String raw, char expectedPrefix, boolean allowShorthandPadding) {
        char prefix = Character.toUpperCase(expectedPrefix);
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(prefix + " ID cannot be empty.");
        }
        String trimmed = raw.trim().toUpperCase(Locale.ROOT);
        String digitsPart;

        if (trimmed.charAt(0) == prefix) {
            digitsPart = trimmed.substring(1);
            if (!allowShorthandPadding && digitsPart.length() != 5) {
                throw new IllegalArgumentException(
                        "Invalid ID format: '" + raw + "'. Expected " + prefix + " followed by 5 digits.");
            }
        } else if (allowShorthandPadding && Character.isDigit(trimmed.charAt(0))) {
            digitsPart = trimmed;
        } else {
            throw new IllegalArgumentException(
                    "Invalid ID prefix in '" + raw + "'. Expected prefix '" + prefix + "'.");
        }

        if (digitsPart.isEmpty() || digitsPart.length() > 5) {
            throw new IllegalArgumentException(
                    "Invalid ID '" + raw + "'. Numeric portion must be between 1 and 99999.");
        }
        for (int i = 0; i < digitsPart.length(); i++) {
            if (!Character.isDigit(digitsPart.charAt(i))) {
                throw new IllegalArgumentException(
                        "Invalid ID '" + raw + "'. Characters after '" + prefix + "' must be digits.");
            }
        }

        int number = Integer.parseInt(digitsPart);
        if (number < 1 || number > 99999) {
            throw new IllegalArgumentException(
                    "Invalid ID '" + raw + "'. ID number must be between 00001 and 99999.");
        }
        return String.format(Locale.US, "%c%05d", prefix, number);
    }

    /**
     * Validates a strict 6-character Product ID (P00001 through P99999) (BR1).
     *
     * @param raw the raw product ID string
     * @return the canonical 6-character product ID
     * @throws IllegalArgumentException if the ID format is invalid
     */
    public static String validateProductId(String raw) {
        return normalizeId(raw, 'P', false);
    }

    /**
     * Normalizes a user-entered Product ID (accepting shorthand like "1" or "P1" to
     * "P00001") (BR1).
     *
     * @param raw the raw product ID input
     * @return the canonical 6-character product ID
     * @throws IllegalArgumentException if the input cannot be normalized
     */
    public static String normalizeProductId(String raw) {
        return normalizeId(raw, 'P', true);
    }

    /**
     * Validates a strict 6-character Batch ID (B00001 through B99999) (BR6).
     *
     * @param raw the raw batch ID string
     * @return the canonical 6-character batch ID
     * @throws IllegalArgumentException if the ID format is invalid
     */
    public static String validateBatchId(String raw) {
        return normalizeId(raw, 'B', false);
    }

    /**
     * Normalizes a user-entered Batch ID (accepting shorthand like "1" or "B1" to
     * "B00001") (BR6).
     *
     * @param raw the raw batch ID input
     * @return the canonical 6-character batch ID
     * @throws IllegalArgumentException if the input cannot be normalized
     */
    public static String normalizeBatchId(String raw) {
        return normalizeId(raw, 'B', true);
    }

    /**
     * Validates a strict 6-character Customer ID (C00001 through C99999) (BR2).
     *
     * @param raw the raw customer ID string
     * @return the canonical 6-character customer ID
     * @throws IllegalArgumentException if the ID format is invalid
     */
    public static String validateCustomerId(String raw) {
        return normalizeId(raw, 'C', false);
    }

    /**
     * Normalizes a user-entered Customer ID (accepting shorthand like "1" or "C1"
     * to "C00001") (BR2).
     *
     * @param raw the raw customer ID input
     * @return the canonical 6-character customer ID
     * @throws IllegalArgumentException if the input cannot be normalized
     */
    public static String normalizeCustomerId(String raw) {
        return normalizeId(raw, 'C', true);
    }

    /**
     * Validates a strict 6-character Order ID (O00001 through O99999) (BR8).
     *
     * @param raw the raw order ID string
     * @return the canonical 6-character order ID
     * @throws IllegalArgumentException if the ID format is invalid
     */
    public static String validateOrderId(String raw) {
        return normalizeId(raw, 'O', false);
    }

    /**
     * Normalizes a user-entered Order ID (accepting shorthand like "1" or "O1" to
     * "O00001") (BR8).
     *
     * @param raw the raw order ID input
     * @return the canonical 6-character order ID
     * @throws IllegalArgumentException if the input cannot be normalized
     */
    public static String normalizeOrderId(String raw) {
        return normalizeId(raw, 'O', true);
    }

    /**
     * Validates and normalizes a human-readable text field such as a product name,
     * customer full name, or address (BR3). Enforces that the text is non-empty,
     * contains at least one Unicode letter (\p{L}), does not start with a CSV
     * formula trigger ('=', '+', '-', '@'), and contains only safe characters.
     *
     * @param raw       the raw text string
     * @param fieldName the descriptive name of the field for error reporting
     * @return the trimmed, normalized text string
     * @throws IllegalArgumentException if the text is blank, purely numeric, or
     *                                  unsafe
     */
    public static String normalizeText(String raw, String fieldName) {
        String label = (fieldName == null || fieldName.trim().isEmpty()) ? "Text field" : fieldName.trim();
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        String clean = raw.trim().replaceAll("\\s+", " ");
        char firstChar = clean.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@') {
            throw new IllegalArgumentException(
                    label + " cannot start with '" + firstChar + "' to prevent CSV formula injection.");
        }
        if (!UNICODE_LETTER_CHECK.matcher(clean).matches()) {
            throw new IllegalArgumentException(label + " must contain at least one letter.");
        }
        if (!SAFE_TEXT_REGEX.matcher(clean).matches()) {
            throw new IllegalArgumentException(label + " contains unsupported special characters.");
        }
        return clean;
    }

    /**
     * Validates a single-word category or measurement unit token (BR5).
     * Disallows slashes or multi-concept separators so each category and unit
     * represents a single atomic concept.
     *
     * @param raw       the raw category or unit string
     * @param fieldName the descriptive name of the field ("Category" or "Unit")
     * @return the trimmed, title-cased token
     * @throws IllegalArgumentException if the token is empty or contains invalid
     *                                  characters
     */
    public static String normalizeSingleWordToken(String raw, String fieldName) {
        String label = (fieldName == null || fieldName.trim().isEmpty()) ? "Token" : fieldName.trim();
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        String clean = raw.trim();
        if (!UNICODE_LETTER_CHECK.matcher(clean).matches()
                || !SINGLE_WORD_TOKEN_REGEX.matcher(clean).matches()) {
            throw new IllegalArgumentException(
                    label + " must be a single word containing only letters or hyphens (no spaces or slashes).");
        }
        if (clean.length() == 1) {
            return clean.toUpperCase(Locale.ROOT);
        }
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
    }

    /**
     * Normalizes and validates a Vietnamese mobile phone number into a 10-digit
     * string matching 0[35789]xxxxxxxx (BR25).
     *
     * @param raw the raw phone number string
     * @return the normalized 10-digit phone number
     * @throws IllegalArgumentException if the phone number is empty or invalid
     */
    public static String normalizePhone(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        String cleaned = raw.trim().replaceAll("[\\s.\\-()]", "");
        if (cleaned.startsWith("+84")) {
            cleaned = cleaned.substring(3);
            if (!cleaned.startsWith("0")) {
                cleaned = "0" + cleaned;
            }
        } else if (cleaned.startsWith("84") && (cleaned.length() == 11 || cleaned.length() == 12)) {
            cleaned = cleaned.substring(2);
            if (!cleaned.startsWith("0")) {
                cleaned = "0" + cleaned;
            }
        }
        if (!PHONE_REGEX.matcher(cleaned).matches()) {
            throw new IllegalArgumentException(
                    "Invalid Vietnamese mobile phone number: '" + raw
                            + "'. Expected 10 digits starting with 03, 05, 07, 08, or 09.");
        }
        return cleaned;
    }

    /**
     * Validates that a selling price is finite and strictly greater than zero
     * (BR4).
     *
     * @param price the unit selling price to validate
     * @return the validated price
     * @throws IllegalArgumentException if price is NaN, infinite, or less than or
     *                                  equal to zero
     */
    public static double validatePositivePrice(double price) {
        if (Double.isNaN(price) || Double.isInfinite(price) || price <= 0.0) {
            throw new IllegalArgumentException("Price must be a finite number greater than zero.");
        }
        return price;
    }

    /**
     * Validates that an integer quantity is strictly positive (BR6, BR9, BR26).
     *
     * @param quantity  the integer quantity to validate
     * @param fieldName the field label for error reporting
     * @return the validated positive integer
     * @throws IllegalArgumentException if quantity is less than or equal to zero
     */
    public static int validatePositiveQuantity(int quantity, String fieldName) {
        String label = (fieldName == null || fieldName.trim().isEmpty()) ? "Quantity" : fieldName.trim();
        if (quantity <= 0) {
            throw new IllegalArgumentException(label + " must be greater than zero.");
        }
        return quantity;
    }

    /**
     * Validates that an integer quantity is non-negative (BR6, BR26).
     *
     * @param quantity  the integer quantity to validate
     * @param fieldName the field label for error reporting
     * @return the validated non-negative integer
     * @throws IllegalArgumentException if quantity is negative
     */
    public static int validateNonNegativeQuantity(int quantity, String fieldName) {
        String label = (fieldName == null || fieldName.trim().isEmpty()) ? "Quantity" : fieldName.trim();
        if (quantity < 0) {
            throw new IllegalArgumentException(label + " cannot be negative.");
        }
        return quantity;
    }

    /**
     * Parses a strict calendar date in dd/MM/yyyy format (BR7).
     *
     * @param raw       the raw date string in dd/MM/yyyy format
     * @param fieldName the descriptive field name for error reporting
     * @return the parsed LocalDate
     * @throws IllegalArgumentException if the string is blank or not a valid
     *                                  calendar date
     */
    public static LocalDate parseDisplayDate(String raw, String fieldName) {
        String label = (fieldName == null || fieldName.trim().isEmpty()) ? "Date" : fieldName.trim();
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        try {
            return LocalDate.parse(raw.trim(), STRICT_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid " + label.toLowerCase(Locale.ROOT) + " '" + raw
                            + "'. Please use valid dd/MM/yyyy format.");
        }
    }

    /**
     * Parses a strict month and year in MM/yyyy format (BR21).
     *
     * @param raw the raw month-year string in MM/yyyy format
     * @return the parsed YearMonth
     * @throws IllegalArgumentException if the string is blank or not a valid
     *                                  MM/yyyy value
     */
    public static YearMonth parseDisplayMonth(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Month/Year cannot be empty.");
        }
        try {
            return YearMonth.parse(raw.trim(), STRICT_MONTH_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid month/year '" + raw + "'. Please use MM/yyyy format (for example, 10/2026).");
        }
    }

    /**
     * Formats a LocalDate into the standard console display format dd/MM/yyyy
     * (BR7).
     *
     * @param date the LocalDate to format
     * @return the formatted dd/MM/yyyy string, or "N/A" if date is null
     */
    public static String formatDisplayDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return date.format(STRICT_DATE_FORMATTER);
    }

    /**
     * Validates that a batch's production date is not after its expiration date
     * (BR7).
     *
     * @param productionDate the batch production date
     * @param expirationDate the batch expiration date
     * @throws IllegalArgumentException if either date is null or productionDate is
     *                                  after expirationDate
     */
    public static void validateDateOrder(LocalDate productionDate, LocalDate expirationDate) {
        if (productionDate == null) {
            throw new IllegalArgumentException("Production date cannot be null.");
        }
        if (expirationDate == null) {
            throw new IllegalArgumentException("Expiration date cannot be null.");
        }
        if (productionDate.isAfter(expirationDate)) {
            throw new IllegalArgumentException(
                    "Production date (" + formatDisplayDate(productionDate)
                            + ") cannot be after expiration date (" + formatDisplayDate(expirationDate) + ").");
        }
    }

    /**
     * Formats a numeric VND amount using Locale.US comma separators without
     * decimals.
     *
     * @param amount the monetary value in VND
     * @return the formatted currency string (for example, "165,000")
     */
    public static String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f", amount);
    }

    /**
     * Formats a numeric VND amount using Locale.US comma separators with the
     * standard currency unit suffix.
     *
     * @param amount the monetary value in VND
     * @return the formatted currency string with unit (for example, "165,000 VND")
     */
    public static String formatCurrencyWithUnit(double amount) {
        return String.format(Locale.US, "%,.0f %s", amount, CURRENCY_UNIT);
    }
}
