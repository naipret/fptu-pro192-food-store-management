package fptu.pro192.foodstoremanagement.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Stateless utility for interactive console input reading, range-checked
 * numeric prompts, date and month prompts, confirmation prompts, and screen
 * navigation pauses.
 */
public final class ConsoleUtil {

    private ConsoleUtil() {
        throw new AssertionError("ConsoleUtil cannot be instantiated.");
    }

    /**
     * Reads a trimmed line of text from standard input after printing a prompt.
     * Allows empty strings when the caller permits optional input.
     *
     * @param prompt the prompt message displayed to the user
     * @return the trimmed input string, or an empty string if no input is available
     */
    public static String readOptionalString(String prompt) {
        if (prompt != null && !prompt.isEmpty()) {
            System.out.print(prompt);
        }
        Scanner scanner = ScannerManager.getInstance().getScanner();
        if (!scanner.hasNextLine()) {
            throw new NoSuchElementException("End of input stream reached.");
        }
        return scanner.nextLine().trim();
    }

    /**
     * Repeatedly prompts the user until a non-empty string is entered.
     *
     * @param prompt the prompt message displayed to the user
     * @return a non-empty trimmed string entered by the user
     */
    public static String readNonEmptyString(String prompt) {
        while (true) {
            String input = readOptionalString(prompt);
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  [ERROR] Input cannot be empty. Please try again.");
        }
    }

    /**
     * Prompts the user for a non-empty string, returning null if 0 is entered to
     * cancel.
     *
     * @param prompt the prompt message displayed to the user
     * @return the non-empty trimmed string entered, or null if cancelled with 0
     */
    public static String readCancellableString(String prompt) {
        while (true) {
            String input = readOptionalString(prompt);
            if ("0".equals(input)) {
                return null;
            }
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  [ERROR] Input cannot be empty. Please try again.");
        }
    }

    /**
     * Prompts the user for a text field validated against Unicode and formula
     * rules,
     * returning null if 0 is entered to cancel.
     *
     * @param prompt    the prompt message displayed to the user
     * @param fieldName the label of the field for error reporting
     * @return the normalized text string, or null if cancelled with 0
     */
    public static String readValidatedText(String prompt, String fieldName) {
        while (true) {
            String raw = readCancellableString(prompt);
            if (raw == null) {
                return null;
            }
            try {
                return InputValidator.normalizeText(raw, fieldName);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for an optional text field, returning defaultValue when
     * Enter
     * is pressed, or null if 0 is entered to cancel.
     *
     * @param prompt       the prompt message displayed to the user
     * @param fieldName    the label of the field for error reporting
     * @param defaultValue the fallback value returned when input is empty
     * @return the normalized text, the defaultValue on Enter, or null if cancelled
     *         with 0
     */
    public static String readOptionalValidatedText(String prompt, String fieldName, String defaultValue) {
        while (true) {
            String raw = readOptionalString(prompt);
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return defaultValue;
            }
            try {
                return InputValidator.normalizeText(raw, fieldName);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for a customer full name validated in Title Case,
     * returning null if 0 is entered to cancel.
     *
     * @param prompt the prompt message displayed to the user
     * @return the normalized customer name, or null if cancelled with 0
     */
    public static String readValidatedCustomerName(String prompt) {
        while (true) {
            String raw = readCancellableString(prompt);
            if (raw == null) {
                return null;
            }
            try {
                return InputValidator.normalizeCustomerName(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for an optional customer full name, returning defaultValue
     * on Enter.
     *
     * @param prompt       the prompt message displayed to the user
     * @param defaultValue the fallback name returned on Enter
     * @return the normalized name, defaultValue on Enter, or null if cancelled with
     *         0
     */
    public static String readOptionalValidatedCustomerName(String prompt, String defaultValue) {
        while (true) {
            String raw = readOptionalString(prompt);
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return defaultValue;
            }
            try {
                return InputValidator.normalizeCustomerName(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for a product display name, returning null if 0 is entered
     * to cancel.
     *
     * @param prompt the prompt message displayed to the user
     * @return the normalized product name, or null if cancelled with 0
     */
    public static String readValidatedProductName(String prompt) {
        while (true) {
            String raw = readCancellableString(prompt);
            if (raw == null) {
                return null;
            }
            try {
                return InputValidator.normalizeProductName(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for an optional product display name, returning defaultValue
     * on Enter.
     *
     * @param prompt       the prompt message displayed to the user
     * @param defaultValue the fallback product name returned on Enter
     * @return the normalized product name, defaultValue on Enter, or null if
     *         cancelled with 0
     */
    public static String readOptionalValidatedProductName(String prompt, String defaultValue) {
        while (true) {
            String raw = readOptionalString(prompt);
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return defaultValue;
            }
            try {
                return InputValidator.normalizeProductName(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for a customer address, returning null if 0 is entered to
     * cancel.
     *
     * @param prompt the prompt message displayed to the user
     * @return the normalized address, or null if cancelled with 0
     */
    public static String readValidatedAddress(String prompt) {
        while (true) {
            String raw = readCancellableString(prompt);
            if (raw == null) {
                return null;
            }
            try {
                return InputValidator.normalizeAddress(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for an optional customer address, returning defaultValue on
     * Enter.
     *
     * @param prompt       the prompt message displayed to the user
     * @param defaultValue the fallback address returned on Enter
     * @return the normalized address, defaultValue on Enter, or null if cancelled
     *         with 0
     */
    public static String readOptionalValidatedAddress(String prompt, String defaultValue) {
        while (true) {
            String raw = readOptionalString(prompt);
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return defaultValue;
            }
            try {
                return InputValidator.normalizeAddress(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Repeatedly prompts the user until an integer within the inclusive range
     * from min to max is entered.
     *
     * @param prompt the prompt message displayed to the user
     * @param min    the minimum allowed integer value (inclusive)
     * @param max    the maximum allowed integer value (inclusive)
     * @return the validated integer within min and max
     * @throws IllegalArgumentException if min is greater than max
     */
    public static int readInt(String prompt, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException(
                    "Invalid range: min (" + min + ") cannot exceed max (" + max + ").");
        }
        while (true) {
            String raw = readOptionalString(prompt);
            try {
                int value = Integer.parseInt(raw);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [ERROR] Please enter a whole number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("  [ERROR] Invalid integer. Please enter a number between %d and %d.%n",
                        min, max);
            }
        }
    }

    /**
     * Prompts the user for an integer within min and max, returning defaultValue
     * on Enter, or null if 0 is entered to cancel.
     *
     * @param prompt       the prompt message displayed to the user
     * @param min          the minimum allowed value
     * @param max          the maximum allowed value
     * @param defaultValue the fallback integer returned on Enter
     * @return the integer entered, the defaultValue on Enter, or null if cancelled
     *         with 0
     */
    public static Integer readIntWithDefault(String prompt, int min, int max, Integer defaultValue) {
        while (true) {
            String raw = readOptionalString(prompt);
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return defaultValue;
            }
            try {
                int value = Integer.parseInt(raw);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [ERROR] Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("  [ERROR] Invalid integer. Please enter a number between %d and %d.%n",
                        min, max);
            }
        }
    }

    /**
     * Prompts the user for a strictly positive integer, returning null if 0 is
     * entered to cancel.
     *
     * @param prompt the prompt message displayed to the user
     * @return a positive integer, or null if cancelled with 0
     */
    public static Integer readPositiveInt(String prompt) {
        return readPositiveInt(prompt, Integer.MAX_VALUE);
    }

    /**
     * Prompts the user for a strictly positive integer, returning null if 0 is
     * entered to cancel.
     *
     * @param prompt            the prompt message displayed to the user
     * @param allowZeroToCancel true if entering 0 cancels the operation
     * @return a positive integer, or null if cancelled with 0
     */
    public static Integer readPositiveInt(String prompt, boolean allowZeroToCancel) {
        return readPositiveInt(prompt, Integer.MAX_VALUE);
    }

    /**
     * Prompts the user for a strictly positive integer up to max, returning null if
     * 0 is entered to cancel.
     *
     * @param prompt the prompt message displayed to the user
     * @param max    the maximum allowed positive integer
     * @return a positive integer between 1 and max, or null if cancelled with 0
     */
    public static Integer readPositiveInt(String prompt, int max) {
        while (true) {
            String raw = readCancellableString(prompt);
            if (raw == null) {
                return null;
            }
            try {
                int value = Integer.parseInt(raw);
                if (value >= 1 && value <= max) {
                    return value;
                }
                if (max == Integer.MAX_VALUE) {
                    System.out.println("  [ERROR] Value must be greater than zero.");
                } else {
                    System.out.printf("  [ERROR] Value must be between 1 and %d.%n", max);
                }
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid integer. Please enter a positive number.");
            }
        }
    }

    /**
     * Prompts the user for a positive selling price, returning null if 0 is entered
     * to cancel.
     *
     * @param prompt the prompt message displayed to the user
     * @return the positive double price, or null if cancelled with 0
     */
    public static Double readPositivePrice(String prompt) {
        while (true) {
            String raw = readCancellableString(prompt);
            if (raw == null) {
                return null;
            }
            try {
                double value = Double.parseDouble(raw);
                return InputValidator.validatePositivePrice(value);
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid numeric price. Please try again.");
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for an optional positive selling price, returning
     * defaultValue
     * on Enter, or null if 0 is entered to cancel.
     *
     * @param prompt       the prompt message displayed to the user
     * @param defaultValue the fallback price returned when input is empty
     * @return the positive price, the defaultValue on Enter, or null if cancelled
     *         with 0
     */
    public static Double readOptionalPositivePrice(String prompt, Double defaultValue) {
        while (true) {
            String raw = readOptionalString(prompt);
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return defaultValue;
            }
            try {
                double value = Double.parseDouble(raw);
                return InputValidator.validatePositivePrice(value);
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid numeric price. Please try again.");
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for a valid calendar date in dd/MM/yyyy format, returning
     * null
     * if 0 is entered to cancel.
     *
     * @param prompt the prompt message displayed to the user
     * @return the parsed LocalDate, or null if cancelled with 0
     */
    public static LocalDate readDate(String prompt) {
        while (true) {
            String raw = readCancellableString(prompt);
            if (raw == null) {
                return null;
            }
            try {
                return InputValidator.parseDisplayDate(raw, "Date");
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for an optional calendar date in dd/MM/yyyy format,
     * returning
     * defaultValue when Enter is pressed, or null if 0 is entered to cancel.
     *
     * @param prompt       the prompt message displayed to the user
     * @param defaultValue the fallback LocalDate returned on Enter
     * @return the parsed LocalDate, the defaultValue on Enter, or null if cancelled
     *         with 0
     */
    public static LocalDate readDateWithDefault(String prompt, LocalDate defaultValue) {
        while (true) {
            String raw = readOptionalString(prompt);
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return defaultValue;
            }
            try {
                return InputValidator.parseDisplayDate(raw, "Date");
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for an optional month and year in MM/yyyy format, returning
     * defaultValue when Enter is pressed, or null if 0 is entered to cancel.
     *
     * @param prompt       the prompt message displayed to the user
     * @param defaultValue the fallback YearMonth returned on Enter
     * @return the parsed YearMonth, the defaultValue on Enter, or null if cancelled
     *         with 0
     */
    public static YearMonth readMonthWithDefault(String prompt, YearMonth defaultValue) {
        while (true) {
            String raw = readOptionalString(prompt);
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return defaultValue;
            }
            try {
                return InputValidator.parseDisplayMonth(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for a Yes or No confirmation (Y/N). Entering N, NO, or 0
     * resolves to false, while Y or YES resolves to true.
     *
     * @param prompt the confirmation question displayed to the user
     * @return true if confirmed with Y/YES, false if cancelled with N/NO/0
     */
    public static boolean confirm(String prompt) {
        while (true) {
            String raw = readOptionalString(prompt + " [Y/N]: ");
            if ("Y".equalsIgnoreCase(raw) || "YES".equalsIgnoreCase(raw)) {
                return true;
            }
            if ("N".equalsIgnoreCase(raw) || "NO".equalsIgnoreCase(raw) || "0".equals(raw)) {
                return false;
            }
            System.out.println("  [ERROR] Please enter Y for Yes or N for No (or 0 to cancel).");
        }
    }

    /**
     * Prompts the user for a Yes or No confirmation with an Enter default value.
     *
     * @param prompt     the confirmation question displayed to the user
     * @param defaultYes true if Enter defaults to Yes, false if Enter defaults to
     *                   No
     * @return true for Yes, false for No
     */
    public static boolean confirmWithDefault(String prompt, boolean defaultYes) {
        String suffix = defaultYes ? " [Y/n]: " : " [y/N]: ";
        while (true) {
            String raw = readOptionalString(prompt + suffix);
            if (raw.isEmpty()) {
                return defaultYes;
            }
            if ("Y".equalsIgnoreCase(raw) || "YES".equalsIgnoreCase(raw)) {
                return true;
            }
            if ("N".equalsIgnoreCase(raw) || "NO".equalsIgnoreCase(raw) || "0".equals(raw)) {
                return false;
            }
            System.out.println("  [ERROR] Please enter Y for Yes or N for No (or 0 to cancel).");
        }
    }

    /**
     * Clears the terminal screen using native Windows command when running in a
     * Windows console, falling back to ANSI escape sequences for modern terminals
     * and the CheerpJ WebAssembly browser environment.
     */
    public static void clearScreen() {
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")
                    && System.console() != null) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.print("");
        }
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }

    /**
     * Pauses console execution until the user presses the Enter key.
     */
    public static void pressEnterToContinue() {
        System.out.print("\nPress [Enter] to continue...");
        Scanner scanner = ScannerManager.getInstance().getScanner();
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        System.out.println();
    }
}
