package fptu.pro192.foodstoremanagement.util;

/**
 * Utility class providing text formatting and layout helpers conforming to the rigid 80-column
 * terminal display specification (VT100 standard).
 */
public final class TableFormatter {

    /**
     * Standard screen width in characters (VT100 / IBM 80-column line limit).
     */
    public static final int SCREEN_WIDTH = 80;

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private TableFormatter() {}

    /**
     * Formats a given string to strictly fit within the target column width.
     * <p>
     * Behavior:
     * <ul>
     * <li>If {@code text} is null, it is treated as an empty string.</li>
     * <li>If {@code text.length() > width}, it is truncated to {@code width - 2} characters and
     * suffixed with {@code ".."}.</li>
     * <li>If {@code text.length() < width}, it is right-padded with whitespace to guarantee the
     * resulting string has an exact length of {@code width}.</li>
     * </ul>
     *
     * @param text The original text to format
     * @param width The target column character width (must be non-negative)
     * @return The formatted string guaranteed to have exact length of {@code width}
     */
    public static String fit(String text, int width) {
        if (width <= 0) {
            return "";
        }
        String safeText = (text == null) ? "" : text;
        int len = safeText.length();

        if (len > width) {
            if (width <= 2) {
                return safeText.substring(0, width);
            }
            return safeText.substring(0, width - 2) + "..";
        }

        if (len == width) {
            return safeText;
        }

        StringBuilder sb = new StringBuilder(width);
        sb.append(safeText);
        for (int i = len; i < width; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Centers text within a column of specified width, with equal flanking whitespace.
     *
     * @param text The text to center
     * @param width The total column width
     * @return The centered and padded string with exact length of {@code width}
     */
    public static String center(String text, int width) {
        if (width <= 0) {
            return "";
        }
        String safeText = (text == null) ? "" : text;
        if (safeText.length() >= width) {
            return fit(safeText, width);
        }
        int remaining = width - safeText.length();
        int leftPad = remaining / 2;
        int rightPad = remaining - leftPad;

        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < leftPad; i++) {
            sb.append(' ');
        }
        sb.append(safeText);
        for (int i = 0; i < rightPad; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Alias for {@link #center(String, int)} supporting alternative naming conventions.
     *
     * @param text The text to center
     * @param width The total column width
     * @return The centered and padded string
     */
    public static String centerText(String text, int width) {
        return center(text, width);
    }

    /**
     * Generates a string composed of a repeated character.
     *
     * @param ch The character to repeat
     * @param count The number of repetitions (must be non-negative)
     * @return The resulting repeated string, or empty string if {@code count <= 0}
     */
    public static String repeat(char ch, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
