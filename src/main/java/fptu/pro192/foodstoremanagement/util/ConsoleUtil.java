package fptu.pro192.foodstoremanagement.util;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Stateless utility for interactive console input reading, range-checked
 * numeric
 * prompts, date prompts, confirmation prompts, and screen navigation pauses.
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
            return "";
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
     * Repeatedly prompts the user until an integer within the inclusive range
     * from min to max is entered (BR26).
     *
     * @param prompt the prompt message displayed to the user
     * @param min    the minimum allowed integer value (inclusive)
     * @param max    the maximum allowed integer value (inclusive)
     * @return the validated integer within [min, max]
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
     * Repeatedly prompts the user until a positive integer greater than zero
     * (or zero if allowZeroToCancel is true) is entered (BR6, BR9, BR26).
     *
     * @param prompt            the prompt message displayed to the user
     * @param allowZeroToCancel true if entering 0 is allowed to cancel the
     *                          operation
     * @return a positive integer, or 0 if cancellation is allowed and selected
     */
    public static int readPositiveInt(String prompt, boolean allowZeroToCancel) {
        int min = allowZeroToCancel ? 0 : 1;
        return readInt(prompt, min, Integer.MAX_VALUE);
    }

    /**
     * Repeatedly prompts the user until a decimal number greater than or equal
     * to min is entered (BR4).
     *
     * @param prompt          the prompt message displayed to the user
     * @param min             the minimum allowed value
     * @param strictlyGreater true if the value must be strictly greater than min
     * @return the validated double value
     */
    public static double readDouble(String prompt, double min, boolean strictlyGreater) {
        while (true) {
            String raw = readOptionalString(prompt);
            try {
                double value = Double.parseDouble(raw);
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    System.out.println("  [ERROR] Please enter a finite numeric value.");
                    continue;
                }
                if (strictlyGreater ? (value > min) : (value >= min)) {
                    return value;
                }
                String relation = strictlyGreater ? "greater than" : "at least";
                System.out.printf("  [ERROR] Value must be %s %s.%n", relation,
                        InputValidator.formatCurrency(min));
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid number format. Please try again.");
            }
        }
    }

    /**
     * Repeatedly prompts the user until a valid date in dd/MM/yyyy format is
     * entered (BR7).
     *
     * @param prompt the prompt message displayed to the user
     * @return the parsed LocalDate instance
     */
    public static LocalDate readDate(String prompt) {
        while (true) {
            String raw = readOptionalString(prompt);
            try {
                return InputValidator.parseDisplayDate(raw, "Date");
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for an optional date in dd/MM/yyyy format or allows
     * an empty input to skip filtering (BR7, BR18, BR21, BR22).
     *
     * @param prompt the prompt message displayed to the user
     * @return the parsed LocalDate instance, or null if the user entered empty input
     */
    public static LocalDate readOptionalDate(String prompt) {
        while (true) {
            String raw = readOptionalString(prompt);
            if (raw.isEmpty()) {
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
     * Prompts the user for a Yes or No confirmation (Y/N).
     *
     * @param prompt the confirmation question displayed to the user
     * @return true if the user confirms with Y or YES, false if N or NO
     */
    public static boolean confirm(String prompt) {
        while (true) {
            String raw = readOptionalString(prompt + " [Y/N]: ");
            if ("Y".equalsIgnoreCase(raw) || "YES".equalsIgnoreCase(raw)) {
                return true;
            }
            if ("N".equalsIgnoreCase(raw) || "NO".equalsIgnoreCase(raw)) {
                return false;
            }
            System.out.println("  [ERROR] Please enter Y for Yes or N for No.");
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
        } catch (Exception ignored) {
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
