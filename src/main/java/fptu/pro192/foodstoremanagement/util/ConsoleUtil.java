package fptu.pro192.foodstoremanagement.util;

import java.util.Scanner;

/**
 * Utility class for terminal screen manipulation and defensive console input reading.
 * <p>
 * Enforces business rule BR18 (All user inputs must be validated prior to persistence or business
 * rule processing) by validating user inputs defensively with Vietnamese prompts.
 */
public final class ConsoleUtil {

    /**
     * Private constructor to prevent instantiation.
     */
    private ConsoleUtil() {}

    /**
     * Clears the terminal screen.
     * <p>
     * Sends ANSI escape sequence {@code \033[H\033[2J} and flushes standard output. In non-ANSI
     * terminal environments where escape sequences fail or throw an exception, it falls back to
     * printing 50 blank lines to push prior content out of view.
     */
    public static void clearScreen() {
        try {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Pauses console execution until the user presses the [ENTER] key.
     * <p>
     * Displays a standardized Vietnamese acknowledgement prompt.
     */
    public static void pressEnterToContinue() {
        System.out.print("\nNhấn phím [ENTER] để tiếp tục...");
        try {
            Scanner scanner = ScannerManager.getInstance().getScanner();
            scanner.nextLine();
        } catch (Exception ignored) {
        }
    }

    /**
     * Prompts the user and reads an integer bounded within the inclusive range {@code [min, max]}.
     * <p>
     * Continuously prompts until the user enters a valid integer matching the specified range
     * constraints. Displays informative Vietnamese error messages on invalid formats or range
     * violations.
     *
     * @param prompt The display message prompting the user
     * @param min The minimum acceptable integer value (inclusive)
     * @param max The maximum acceptable integer value (inclusive)
     * @return The validated integer within {@code [min, max]}
     */
    public static int readInt(String prompt, int min, int max) {
        Scanner scanner = ScannerManager.getInstance().getScanner();
        while (true) {
            System.out.print(prompt);
            String input = "";
            try {
                input = scanner.nextLine();
            } catch (Exception e) {
                // If scanner closed or depleted, return min
                return min;
            }

            if (input == null) {
                System.out.println("Vui lòng nhập giá trị hợp lệ!");
                continue;
            }

            String trimmed = input.trim();
            if (trimmed.isEmpty()) {
                System.out.println("Giá trị không được để trống. Vui lòng nhập số!");
                continue;
            }

            try {
                int value = Integer.parseInt(trimmed);
                if (value < min || value > max) {
                    System.out.printf(
                            "Giá trị phải nằm trong khoảng [%d - %d]. Vui lòng nhập lại!\n", min,
                            max);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số nguyên hợp lệ!");
            }
        }
    }

    /**
     * Prompts the user and reads a line of text input with optional blank guarding.
     * <p>
     * If {@code allowEmpty} is false, continuously prompts the user until a non-blank string is
     * provided.
     *
     * @param prompt The display message prompting the user
     * @param allowEmpty Whether an empty or whitespace-only string is considered valid
     * @return The trimmed user input string
     */
    public static String readString(String prompt, boolean allowEmpty) {
        Scanner scanner = ScannerManager.getInstance().getScanner();
        while (true) {
            System.out.print(prompt);
            String input = "";
            try {
                input = scanner.nextLine();
            } catch (Exception e) {
                return "";
            }

            if (input == null) {
                if (allowEmpty) {
                    return "";
                }
                System.out.println("Dữ liệu không được để trống. Vui lòng nhập lại!");
                continue;
            }

            String trimmed = input.trim();
            if (!allowEmpty && trimmed.isEmpty()) {
                System.out.println("Dữ liệu không được để trống. Vui lòng nhập lại!");
                continue;
            }
            return trimmed;
        }
    }
}
