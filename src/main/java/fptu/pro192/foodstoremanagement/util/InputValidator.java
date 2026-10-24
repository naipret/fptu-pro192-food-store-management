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

    private static final Pattern ASCII_LETTER_CHECK = Pattern.compile(".*[a-zA-Z].*");

    private static final Pattern CUSTOMER_NAME_REGEX = Pattern
            .compile("^[a-zA-Z\\s'-]+$");

    private static final Pattern PRODUCT_NAME_REGEX = Pattern
            .compile("^[a-zA-Z0-9\\s.,'\"()/\\-&]+$");

    private static final Pattern ADDRESS_REGEX = Pattern
            .compile("^[a-zA-Z0-9\\s.,'\"()/\\-&#]+$");

    private static final Pattern SAFE_TEXT_REGEX = Pattern
            .compile("^[a-zA-Z0-9\\s.,'\"()/\\-&]+$");

    private static final Pattern SINGLE_WORD_TOKEN_REGEX = Pattern
            .compile("^[a-zA-Z0-9\\-]+$");

    private InputValidator() {
        throw new AssertionError("InputValidator cannot be instantiated.");
    }

    private static String toTitleCaseWord(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }
        if (word.contains("-")) {
            String[] parts = word.split("-", -1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    sb.append('-');
                }
                sb.append(toTitleCaseWord(parts[i]));
            }
            return sb.toString();
        }
        if (word.length() == 1) {
            return word.toUpperCase(Locale.ROOT);
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * Validates and normalizes an entity identifier to the canonical 6-character
     * format consisting of the uppercase prefix followed by 5 digits (BR33).
     * Accepts shorthand numeric input such as "1" or "P1" and pads it to "P00001".
     * Rejects identifiers whose numeric portion is outside 1..99999 or that use a
     * non-canonical digit count when a prefix is already supplied (unless shorthand
     * padding is explicitly enabled).
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
     * Validates a strict 6-character Product ID (P00001 through P99999) (BR1,
     * BR33).
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
     * "P00001") (BR1, BR33).
     *
     * @param raw the raw product ID input
     * @return the canonical 6-character product ID
     * @throws IllegalArgumentException if the input cannot be normalized
     */
    public static String normalizeProductId(String raw) {
        return normalizeId(raw, 'P', true);
    }

    /**
     * Safely attempts to normalize a Product ID, returning null on failure (BR1,
     * BR33).
     *
     * @param raw the raw product ID input
     * @return the canonical 6-character product ID, or null if invalid
     */
    public static String tryNormalizeProductId(String raw) {
        try {
            return normalizeProductId(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validates a strict 6-character Batch ID (B00001 through B99999) (BR33).
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
     * "B00001") (BR33).
     *
     * @param raw the raw batch ID input
     * @return the canonical 6-character batch ID
     * @throws IllegalArgumentException if the input cannot be normalized
     */
    public static String normalizeBatchId(String raw) {
        return normalizeId(raw, 'B', true);
    }

    /**
     * Safely attempts to normalize a Batch ID, returning null on failure (BR33).
     *
     * @param raw the raw batch ID input
     * @return the canonical 6-character batch ID, or null if invalid
     */
    public static String tryNormalizeBatchId(String raw) {
        try {
            return normalizeBatchId(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validates a strict 6-character Customer ID (C00001 through C99999) (BR2,
     * BR33).
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
     * to "C00001") (BR2, BR33).
     *
     * @param raw the raw customer ID input
     * @return the canonical 6-character customer ID
     * @throws IllegalArgumentException if the input cannot be normalized
     */
    public static String normalizeCustomerId(String raw) {
        return normalizeId(raw, 'C', true);
    }

    /**
     * Safely attempts to normalize a Customer ID, returning null on failure (BR2,
     * BR33).
     *
     * @param raw the raw customer ID input
     * @return the canonical 6-character customer ID, or null if invalid
     */
    public static String tryNormalizeCustomerId(String raw) {
        try {
            return normalizeCustomerId(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validates a strict 6-character Order ID (O00001 through O99999) (BR33).
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
     * "O00001") (BR33).
     *
     * @param raw the raw order ID input
     * @return the canonical 6-character order ID
     * @throws IllegalArgumentException if the input cannot be normalized
     */
    public static String normalizeOrderId(String raw) {
        return normalizeId(raw, 'O', true);
    }

    /**
     * Safely attempts to normalize an Order ID, returning null on failure (BR33).
     *
     * @param raw the raw order ID input
     * @return the canonical 6-character order ID, or null if invalid
     */
    public static String tryNormalizeOrderId(String raw) {
        try {
            return normalizeOrderId(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validates and normalizes a human-readable text field such as a product name,
     * customer full name, or address (BR3, BR32). Enforces that the text is
     * non-empty, contains at least one ASCII letter, does not start with a CSV
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
        if (!ASCII_LETTER_CHECK.matcher(clean).matches()) {
            throw new IllegalArgumentException(label + " must contain at least one letter.");
        }
        if (!SAFE_TEXT_REGEX.matcher(clean).matches()) {
            throw new IllegalArgumentException(label + " contains unsupported special characters.");
        }
        return clean;
    }

    /**
     * Validates and normalizes a customer full name into Title Case format (BR2, BR32).
     * Enforces ASCII letters, hyphens, and apostrophes only. Rejects numbers and special
     * symbols. Converts all words to title case regardless of original casing.
     *
     * @param raw the raw customer name input
     * @return the normalized title-cased customer full name
     * @throws IllegalArgumentException if input is blank or violates name format rules
     */
    public static String normalizeCustomerName(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }
        String clean = raw.trim().replaceAll("\\s+", " ");
        char firstChar = clean.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@') {
            throw new IllegalArgumentException(
                    "Customer name cannot start with '" + firstChar + "' to prevent CSV formula injection.");
        }
        if (!ASCII_LETTER_CHECK.matcher(clean).matches()) {
            throw new IllegalArgumentException("Customer name must contain at least one ASCII letter.");
        }
        if (!CUSTOMER_NAME_REGEX.matcher(clean).matches()) {
            throw new IllegalArgumentException(
                    "Customer name contains invalid characters. Only ASCII letters, spaces, hyphens, and apostrophes are allowed.");
        }
        String[] words = clean.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(toTitleCaseWord(words[i]));
        }
        return sb.toString();
    }

    /**
     * Validates and normalizes a product name (BR3, BR32).
     * Enforces ASCII characters and at least one ASCII letter. Capitalizes the
     * initial character while preserving the user's casing for subsequent tokens
     * such as model numbers, sizes, and acronyms.
     *
     * @param raw the raw product name input
     * @return the normalized product name
     * @throws IllegalArgumentException if input is blank or violates product name rules
     */
    public static String normalizeProductName(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        String clean = raw.trim().replaceAll("\\s+", " ");
        char firstChar = clean.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@') {
            throw new IllegalArgumentException(
                    "Product name cannot start with '" + firstChar + "' to prevent CSV formula injection.");
        }
        if (!ASCII_LETTER_CHECK.matcher(clean).matches()) {
            throw new IllegalArgumentException("Product name must contain at least one ASCII letter.");
        }
        if (!PRODUCT_NAME_REGEX.matcher(clean).matches()) {
            throw new IllegalArgumentException("Product name contains unsupported characters.");
        }
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
    }

    /**
     * Validates and normalizes a customer physical address (BR2, BR32).
     * Replaces semicolons with commas, collapses redundant commas and whitespace,
     * checks for formula injection, and capitalizes the leading character while
     * preserving street numbers, alley slashes, and common abbreviations.
     *
     * @param raw the raw address input
     * @return the normalized address
     * @throws IllegalArgumentException if input is blank or violates address rules
     */
    public static String normalizeAddress(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty.");
        }
        String clean = raw.trim().replace(';', ',').replaceAll("\\s+", " ");
        clean = clean.replaceAll("\\s*,\\s*", ", ");
        clean = clean.replaceAll("(,\\s*)+", ", ").trim();
        if (clean.endsWith(",")) {
            clean = clean.substring(0, clean.length() - 1).trim();
        }
        char firstChar = clean.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@') {
            throw new IllegalArgumentException(
                    "Address cannot start with '" + firstChar + "' to prevent CSV formula injection.");
        }
        if (!ASCII_LETTER_CHECK.matcher(clean).matches()) {
            throw new IllegalArgumentException("Address must contain at least one ASCII letter.");
        }
        if (!ADDRESS_REGEX.matcher(clean).matches()) {
            throw new IllegalArgumentException("Address contains unsupported characters.");
        }
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
    }

    /**
     * Validates a single-word category or measurement unit token (BR3, BR4, BR31).
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
        if (!ASCII_LETTER_CHECK.matcher(clean).matches()
                || !SINGLE_WORD_TOKEN_REGEX.matcher(clean).matches()) {
            throw new IllegalArgumentException(
                    label + " must be a single word containing only letters or hyphens (no spaces or slashes).");
        }
        if (clean.length() == 1) {
            return clean.toUpperCase(Locale.ROOT);
        }
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1).toLowerCase(Locale.ROOT);
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
                            + "'.\n          Expected 10 digits starting with 03, 05, 07, 08, or 09.");
        }
        return cleaned;
    }

    /**
     * Safely attempts to normalize a phone number, returning null on failure
     * (BR25).
     *
     * @param raw the raw phone number string
     * @return the normalized 10-digit phone number, or null if invalid
     */
    public static String tryNormalizePhone(String raw) {
        try {
            return normalizePhone(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validates that a selling price is finite and strictly greater than zero
     * (BR5).
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
     * Validates that an integer quantity is strictly positive (BR9, BR26).
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
     * Parses a strict month and year in MM/yyyy format (BR21, BR22).
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
                    "Invalid month/year '" + raw + "'. Please use MM/yyyy format.");
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
     * Validates that batch dates adhere to business consistency rules (BR7):
     * production date must be on or before expiration date, on or before import date,
     * and import date must be on or before expiration date.
     *
     * @param productionDate the batch production date
     * @param expirationDate the batch expiration date
     * @param importDate     the batch import date
     * @throws IllegalArgumentException if any date is null or chronologically invalid
     */
    public static void validateBatchDates(LocalDate productionDate, LocalDate expirationDate, LocalDate importDate) {
        validateDateOrder(productionDate, expirationDate);
        if (importDate == null) {
            throw new IllegalArgumentException("Import date cannot be null.");
        }
        if (productionDate.isAfter(importDate)) {
            throw new IllegalArgumentException(
                    "Production date (" + formatDisplayDate(productionDate)
                            + ") cannot be after import date (" + formatDisplayDate(importDate) + ").");
        }
        if (importDate.isAfter(expirationDate)) {
            throw new IllegalArgumentException(
                    "Import date (" + formatDisplayDate(importDate)
                            + ") cannot be after expiration date (" + formatDisplayDate(expirationDate) + ").");
        }
    }

    /**
     * Formats a numeric amount using Locale.US comma separators without
     * decimals.
     *
     * @param amount the monetary value
     * @return the formatted currency string (for example, "165,000")
     */
    public static String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f", amount);
    }
}
