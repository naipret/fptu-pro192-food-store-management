package fptu.pro192.foodstoremanagement.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Centralized stateless validator and normalizer for entity identifiers,
 * names, addresses, phone numbers, prices, integer quantities, and dates.
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
                    "Gram", "Jar", "Carton")));

    /**
     * Standard console date display and input pattern (dd/MM/yyyy).
     */
    public static final String DISPLAY_DATE_PATTERN = "dd/MM/yyyy";

    /**
     * Standard console month-year input pattern (MM/yyyy).
     */
    public static final String DISPLAY_MONTH_PATTERN = "MM/yyyy";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern PHONE_REGEX = Pattern.compile("^0[35789]\\d{8}$");
    private static final Pattern ASCII_LETTER_CHECK = Pattern.compile(".*[a-zA-Z].*");
    private static final Pattern SINGLE_WORD_REGEX = Pattern.compile("^[a-zA-Z0-9\\-]+$");

    private InputValidator() {
        throw new AssertionError("InputValidator cannot be instantiated.");
    }

    /**
     * Validates and normalizes an entity identifier to canonical format (e.g.,
     * P00001).
     * Accepts shorthand such as "1" or "P1" and pads to 5 digits.
     *
     * @param raw            the raw ID string
     * @param expectedPrefix the required prefix ('P', 'B', 'C', or 'O')
     * @return the normalized 6-character identifier
     * @throws IllegalArgumentException if raw is empty or invalid
     */
    public static String normalizeId(String raw, char expectedPrefix) {
        char prefix = Character.toUpperCase(expectedPrefix);
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(prefix + " ID cannot be empty.");
        }
        String trimmed = raw.trim().toUpperCase(Locale.ROOT);
        String digits = trimmed.startsWith(String.valueOf(prefix)) ? trimmed.substring(1) : trimmed;
        try {
            int num = Integer.parseInt(digits);
            if (num <= 0 || num > 99999) {
                throw new IllegalArgumentException("ID number must be between 1 and 99999.");
            }
            return String.format(Locale.US, "%c%05d", prefix, num);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID format: '" + raw + "'. Expected prefix '" + prefix + "'.");
        }
    }

    /**
     * Safely attempts to normalize an entity identifier, returning null on failure.
     *
     * @param raw            the raw ID string
     * @param expectedPrefix the required prefix ('P', 'B', 'C', or 'O')
     * @return the normalized 6-character identifier, or null if invalid
     */
    public static String tryNormalizeId(String raw, char expectedPrefix) {
        try {
            return normalizeId(raw, expectedPrefix);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validates and normalizes a Product ID to canonical format (BR1, BR33).
     *
     * @param raw the raw ID string
     * @return the normalized Product ID
     * @throws IllegalArgumentException if raw is invalid
     */
    public static String validateProductId(String raw) {
        return normalizeId(raw, 'P');
    }

    /**
     * Safely normalizes a Product ID, returning null on failure (BR1, BR33).
     *
     * @param raw the raw ID string
     * @return the normalized Product ID, or null if invalid
     */
    public static String tryNormalizeProductId(String raw) {
        return tryNormalizeId(raw, 'P');
    }

    /**
     * Validates and normalizes a Batch ID to canonical format (BR33).
     *
     * @param raw the raw ID string
     * @return the normalized Batch ID
     * @throws IllegalArgumentException if raw is invalid
     */
    public static String validateBatchId(String raw) {
        return normalizeId(raw, 'B');
    }

    /**
     * Safely normalizes a Batch ID, returning null on failure (BR33).
     *
     * @param raw the raw ID string
     * @return the normalized Batch ID, or null if invalid
     */
    public static String tryNormalizeBatchId(String raw) {
        return tryNormalizeId(raw, 'B');
    }

    /**
     * Validates and normalizes a Customer ID to canonical format (BR2, BR33).
     *
     * @param raw the raw ID string
     * @return the normalized Customer ID
     * @throws IllegalArgumentException if raw is invalid
     */
    public static String validateCustomerId(String raw) {
        return normalizeId(raw, 'C');
    }

    /**
     * Safely normalizes a Customer ID, returning null on failure (BR2, BR33).
     *
     * @param raw the raw ID string
     * @return the normalized Customer ID, or null if invalid
     */
    public static String tryNormalizeCustomerId(String raw) {
        return tryNormalizeId(raw, 'C');
    }

    /**
     * Validates and normalizes an Order ID to canonical format (BR33).
     *
     * @param raw the raw ID string
     * @return the normalized Order ID
     * @throws IllegalArgumentException if raw is invalid
     */
    public static String validateOrderId(String raw) {
        return normalizeId(raw, 'O');
    }

    /**
     * Safely normalizes an Order ID, returning null on failure (BR33).
     *
     * @param raw the raw ID string
     * @return the normalized Order ID, or null if invalid
     */
    public static String tryNormalizeOrderId(String raw) {
        return tryNormalizeId(raw, 'O');
    }

    /**
     * Validates and normalizes general text.
     *
     * @param raw       the raw text
     * @param fieldName the field name for error reporting
     * @return the normalized string
     */
    public static String normalizeText(String raw, String fieldName) {
        String label = (fieldName == null || fieldName.trim().isEmpty()) ? "Text field" : fieldName.trim();
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        String clean = raw.trim().replaceAll("\\s+", " ");
        if (!ASCII_LETTER_CHECK.matcher(clean).matches()) {
            throw new IllegalArgumentException(label + " must contain at least one letter.");
        }
        return clean;
    }

    /**
     * Validates and normalizes customer full name to Title Case.
     *
     * @param raw the raw customer name
     * @return the normalized title-cased name
     */
    public static String normalizeCustomerName(String raw) {
        String clean = normalizeText(raw, "Customer name");
        if (!clean.matches("^[a-zA-Z]+(\\s[a-zA-Z]+)*$")) {
            throw new IllegalArgumentException("Customer name can only contain letters and spaces.");
        }
        String[] words = clean.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0)
                sb.append(' ');
            String word = words[i];
            if (word.length() == 1) {
                sb.append(word.toUpperCase(Locale.ROOT));
            } else {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return sb.toString();
    }

    /**
     * Validates and normalizes product name.
     *
     * @param raw the raw product name
     * @return the normalized product name
     */
    public static String normalizeProductName(String raw) {
        String clean = normalizeText(raw, "Product name");
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
    }

    /**
     * Validates and normalizes customer address.
     *
     * @param raw the raw address
     * @return the normalized address
     */
    public static String normalizeAddress(String raw) {
        String clean = normalizeText(raw, "Address").replace(';', ',');
        clean = clean.replaceAll("\\s*,\\s*", ", ");
        if (clean.endsWith(",")) {
            clean = clean.substring(0, clean.length() - 1).trim();
        }
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
    }

    /**
     * Validates a single-word token for category or measurement unit.
     *
     * @param raw       the raw token string
     * @param fieldName the field name ("Category" or "Unit")
     * @return the formatted token
     */
    public static String normalizeSingleWordToken(String raw, String fieldName) {
        String label = (fieldName == null || fieldName.trim().isEmpty()) ? "Token" : fieldName.trim();
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        String clean = raw.trim();
        if (!SINGLE_WORD_REGEX.matcher(clean).matches()) {
            throw new IllegalArgumentException(label + " must be a single word without spaces or special symbols.");
        }
        if (clean.length() == 1) {
            return clean.toUpperCase(Locale.ROOT);
        }
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes and validates a 10-digit Vietnamese mobile phone number.
     *
     * @param raw the raw phone string
     * @return the normalized 10-digit phone number
     */
    public static String normalizePhone(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        String cleaned = raw.trim().replaceAll("[\\s.\\-()]", "");
        if (cleaned.startsWith("+84")) {
            cleaned = "0" + cleaned.substring(3);
        } else if (cleaned.startsWith("84") && cleaned.length() >= 11) {
            cleaned = "0" + cleaned.substring(2);
        }
        if (!PHONE_REGEX.matcher(cleaned).matches()) {
            throw new IllegalArgumentException(
                    "Invalid phone number: '" + raw + "'. Expected 10 digits starting with 03, 05, 07, 08, or 09.");
        }
        return cleaned;
    }

    /**
     * Safely attempts to normalize a phone number, returning null on failure.
     *
     * @param raw the raw phone string
     * @return the normalized phone number, or null if invalid
     */
    public static String tryNormalizePhone(String raw) {
        try {
            return normalizePhone(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validates that price is positive (> 0).
     *
     * @param price the price value
     * @return the validated price
     */
    public static double validatePositivePrice(double price) {
        if (Double.isNaN(price) || Double.isInfinite(price) || price <= 0.0) {
            throw new IllegalArgumentException("Price must be a positive number greater than zero.");
        }
        return price;
    }

    /**
     * Validates that quantity is strictly positive (> 0).
     *
     * @param quantity  the quantity value
     * @param fieldName the field label
     * @return the validated quantity
     */
    public static int validatePositiveQuantity(int quantity, String fieldName) {
        String label = (fieldName == null || fieldName.trim().isEmpty()) ? "Quantity" : fieldName.trim();
        if (quantity <= 0) {
            throw new IllegalArgumentException(label + " must be greater than zero.");
        }
        return quantity;
    }

    /**
     * Validates that quantity is non-negative (>= 0).
     *
     * @param quantity  the quantity value
     * @param fieldName the field label
     * @return the validated quantity
     */
    public static int validateNonNegativeQuantity(int quantity, String fieldName) {
        String label = (fieldName == null || fieldName.trim().isEmpty()) ? "Quantity" : fieldName.trim();
        if (quantity < 0) {
            throw new IllegalArgumentException(label + " cannot be negative.");
        }
        return quantity;
    }

    /**
     * Parses a calendar date in dd/MM/yyyy format with shorthand support (BR7,
     * BR18).
     * Missing month or year fields default to the current month and year.
     *
     * @param raw       the raw date string
     * @param fieldName the field label
     * @return the parsed LocalDate
     * @throws IllegalArgumentException if raw is empty or represents an invalid
     *                                  date
     */
    public static LocalDate parseDisplayDate(String raw, String fieldName) {
        String label = (fieldName == null || fieldName.trim().isEmpty()) ? "Date" : fieldName.trim();
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        String[] parts = raw.trim().split("/", -1);
        if (parts.length == 0 || parts.length > 3
                || parts[0].length() > 2
                || (parts.length > 1 && parts[1].length() > 2)
                || (parts.length == 3 && parts[2].length() != 4)) {
            throw new IllegalArgumentException(
                    "Invalid " + label.toLowerCase(Locale.ROOT) + " '" + raw + "'. Please use dd/MM/yyyy format.");
        }
        LocalDate now = LocalDate.now();
        try {
            int day = Integer.parseInt(parts[0]);
            int month = (parts.length > 1) ? Integer.parseInt(parts[1]) : now.getMonthValue();
            int year = (parts.length > 2) ? Integer.parseInt(parts[2]) : now.getYear();
            return LocalDate.of(year, month, day);
        } catch (DateTimeException | NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid " + label.toLowerCase(Locale.ROOT) + " '" + raw + "'. Please use dd/MM/yyyy format.");
        }
    }

    /**
     * Parses a month and year in MM/yyyy format with shorthand support (BR18,
     * BR23).
     * Missing year field defaults to the current year.
     *
     * @param raw the raw month-year string
     * @return the parsed YearMonth
     * @throws IllegalArgumentException if raw is empty or represents an invalid
     *                                  month
     */
    public static YearMonth parseDisplayMonth(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Month/Year cannot be empty.");
        }
        String[] parts = raw.trim().split("/", -1);
        if (parts.length == 0 || parts.length > 2
                || parts[0].length() > 2
                || (parts.length == 2 && parts[1].length() != 4)) {
            throw new IllegalArgumentException("Invalid month/year '" + raw + "'. Please use MM/yyyy format.");
        }
        YearMonth now = YearMonth.now();
        try {
            int month = Integer.parseInt(parts[0]);
            int year = (parts.length > 1) ? Integer.parseInt(parts[1]) : now.getYear();
            return YearMonth.of(year, month);
        } catch (DateTimeException | NumberFormatException e) {
            throw new IllegalArgumentException("Invalid month/year '" + raw + "'. Please use MM/yyyy format.");
        }
    }

    /**
     * Formats a LocalDate into dd/MM/yyyy display string.
     *
     * @param date the LocalDate
     * @return the formatted string, or "N/A" if null
     */
    public static String formatDisplayDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Validates that production date is not after expiration date.
     *
     * @param productionDate the production date
     * @param expirationDate the expiration date
     */
    public static void validateDateOrder(LocalDate productionDate, LocalDate expirationDate) {
        if (productionDate == null) {
            throw new IllegalArgumentException("Production date cannot be null.");
        }
        if (expirationDate == null) {
            throw new IllegalArgumentException("Expiration date cannot be null.");
        }
        if (productionDate.isAfter(expirationDate)) {
            throw new IllegalArgumentException("Production date (" + formatDisplayDate(productionDate)
                    + ") cannot be after expiration date (" + formatDisplayDate(expirationDate) + ").");
        }
    }

    /**
     * Validates consistent batch dates: productionDate <= importDate <=
     * expirationDate.
     *
     * @param productionDate the production date
     * @param expirationDate the expiration date
     * @param importDate     the import date
     */
    public static void validateBatchDates(LocalDate productionDate, LocalDate expirationDate, LocalDate importDate) {
        validateDateOrder(productionDate, expirationDate);
        if (importDate == null) {
            throw new IllegalArgumentException("Import date cannot be null.");
        }
        if (productionDate.isAfter(importDate)) {
            throw new IllegalArgumentException("Production date (" + formatDisplayDate(productionDate)
                    + ") cannot be after import date (" + formatDisplayDate(importDate) + ").");
        }
        if (importDate.isAfter(expirationDate)) {
            throw new IllegalArgumentException("Import date (" + formatDisplayDate(importDate)
                    + ") cannot be after expiration date (" + formatDisplayDate(expirationDate) + ").");
        }
    }

    /**
     * Formats a currency amount with comma separators.
     *
     * @param amount the double amount
     * @return formatted currency string
     */
    public static String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f", amount);
    }
}
