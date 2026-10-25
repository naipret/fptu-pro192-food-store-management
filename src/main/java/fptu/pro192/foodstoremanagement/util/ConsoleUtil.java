package fptu.pro192.foodstoremanagement.util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Utility for interactive console input reading, range-checked
 * numeric prompts, dates, confirmation prompts, and screen clearing.
 */
public final class ConsoleUtil {

    private ConsoleUtil() {
        throw new AssertionError("ConsoleUtil cannot be instantiated.");
    }

    /**
     * Clears the terminal screen.
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
     * Reads a line of text, returning null if user enters 0 to cancel.
     *
     * @param prompt the prompt message
     * @return non-empty string, or null if cancelled with 0
     */
    public static String readString(String prompt) {
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
     * Reads a line of text with a default value when Enter is pressed.
     *
     * @param prompt       the prompt message
     * @param defaultValue the fallback value on Enter
     * @return the string entered, defaultValue on Enter, or null if cancelled with 0
     */
    public static String readString(String prompt, String defaultValue) {
        while (true) {
            String input = readOptionalString(prompt);
            if ("0".equals(input)) {
                return null;
            }
            if (input.isEmpty()) {
                return defaultValue;
            }
            return input;
        }
    }

    /**
     * Reads an optional line of text (can be empty).
     *
     * @param prompt the prompt message
     * @return trimmed input line
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
     * Reads a non-empty string.
     *
     * @param prompt the prompt message
     * @return non-empty string
     */
    public static String readNonEmptyString(String prompt) {
        return readString(prompt);
    }

    /**
     * Reads a string, returning null if 0 is entered.
     *
     * @param prompt the prompt message
     * @return entered string, or null if 0
     */
    public static String readCancellableString(String prompt) {
        return readString(prompt);
    }

    /**
     * Prompts for an integer within min and max.
     *
     * @param prompt the prompt message
     * @param min    minimum value
     * @param max    maximum value
     * @return validated integer
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String raw = readOptionalString(prompt);
            try {
                int value = Integer.parseInt(raw);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [ERROR] Please enter a whole number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("  [ERROR] Invalid integer. Please enter a number between %d and %d.%n", min, max);
            }
        }
    }

    /**
     * Prompts for an integer within min and max, returning defaultValue on Enter.
     *
     * @param prompt       the prompt message
     * @param min          minimum value
     * @param max          maximum value
     * @param defaultValue default value on Enter
     * @return entered integer, defaultValue on Enter, or null if cancelled with 0
     */
    public static Integer readInt(String prompt, int min, int max, Integer defaultValue) {
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
                System.out.printf("  [ERROR] Invalid integer. Please enter a number between %d and %d.%n", min, max);
            }
        }
    }

    public static Integer readIntWithDefault(String prompt, int min, int max, Integer defaultValue) {
        return readInt(prompt, min, max, defaultValue);
    }

    public static Integer readPositiveInt(String prompt) {
        return readPositiveInt(prompt, Integer.MAX_VALUE);
    }

    public static Integer readPositiveInt(String prompt, boolean allowZeroToCancel) {
        return readPositiveInt(prompt, Integer.MAX_VALUE);
    }

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
                System.out.printf("  [ERROR] Value must be between 1 and %d.%n", max);
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid integer. Please enter a positive number.");
            }
        }
    }

    /**
     * Prompts for a positive double price, returning null if 0 is entered.
     *
     * @param prompt the prompt message
     * @return positive double, or null if cancelled
     */
    public static Double readDouble(String prompt, double min) {
        while (true) {
            String raw = readCancellableString(prompt);
            if (raw == null) {
                return null;
            }
            try {
                double value = Double.parseDouble(raw);
                if (value > min) {
                    return value;
                }
                System.out.printf("  [ERROR] Value must be greater than %s.%n", min);
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid number. Please try again.");
            }
        }
    }

    /**
     * Prompts for a double with a default value on Enter.
     *
     * @param prompt       the prompt message
     * @param min          minimum value
     * @param defaultValue default value on Enter
     * @return double value, defaultValue on Enter, or null if cancelled
     */
    public static Double readDouble(String prompt, double min, Double defaultValue) {
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
                if (value > min) {
                    return value;
                }
                System.out.printf("  [ERROR] Value must be greater than %s.%n", min);
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid number. Please try again.");
            }
        }
    }

    public static Double readPositivePrice(String prompt) {
        return readDouble(prompt, 0.0);
    }

    public static Double readOptionalPositivePrice(String prompt, Double defaultValue) {
        return readDouble(prompt, 0.0, defaultValue);
    }

    /**
     * Prompts for a date in dd/MM/yyyy format with default on Enter.
     *
     * @param prompt       the prompt message
     * @param defaultValue default date on Enter
     * @return LocalDate, defaultValue on Enter, or null if cancelled with 0
     */
    public static LocalDate readDate(String prompt, LocalDate defaultValue) {
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

    public static LocalDate readDate(String prompt) {
        return readDate(prompt, null);
    }

    public static LocalDate readDateWithDefault(String prompt, LocalDate defaultValue) {
        return readDate(prompt, defaultValue);
    }

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
     * Prompts for Yes/No confirmation (Y/N).
     *
     * @param prompt confirmation question
     * @return true for Y, false for N/0
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

    // Temporary delegates to keep current views compiling until Commit 4
    public static String readValidatedText(String prompt, String fieldName) {
        String raw = readString(prompt);
        return raw == null ? null : InputValidator.normalizeText(raw, fieldName);
    }

    public static String readOptionalValidatedText(String prompt, String fieldName, String defaultValue) {
        String raw = readString(prompt, defaultValue);
        return raw == null ? null : InputValidator.normalizeText(raw, fieldName);
    }

    public static String readValidatedCustomerName(String prompt) {
        while (true) {
            String raw = readString(prompt);
            if (raw == null) return null;
            try {
                return InputValidator.normalizeCustomerName(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    public static String readOptionalValidatedCustomerName(String prompt, String defaultValue) {
        while (true) {
            String raw = readString(prompt, defaultValue);
            if (raw == null) return null;
            try {
                return InputValidator.normalizeCustomerName(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    public static String readValidatedProductName(String prompt) {
        while (true) {
            String raw = readString(prompt);
            if (raw == null) return null;
            try {
                return InputValidator.normalizeProductName(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    public static String readOptionalValidatedProductName(String prompt, String defaultValue) {
        while (true) {
            String raw = readString(prompt, defaultValue);
            if (raw == null) return null;
            try {
                return InputValidator.normalizeProductName(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    public static String readValidatedAddress(String prompt) {
        while (true) {
            String raw = readString(prompt);
            if (raw == null) return null;
            try {
                return InputValidator.normalizeAddress(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    public static String readOptionalValidatedAddress(String prompt, String defaultValue) {
        while (true) {
            String raw = readString(prompt, defaultValue);
            if (raw == null) return null;
            try {
                return InputValidator.normalizeAddress(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    public static void pressEnterToContinue() {
        System.out.print("\nPress [Enter] to continue...");
        Scanner scanner = ScannerManager.getInstance().getScanner();
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        System.out.println();
    }
}
