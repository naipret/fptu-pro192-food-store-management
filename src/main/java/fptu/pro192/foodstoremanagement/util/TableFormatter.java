package fptu.pro192.foodstoremanagement.util;

/**
 * Stateless utility for 80-column ASCII terminal alignment, string truncation,
 * centering, and horizontal border generation.
 */
public final class TableFormatter {

    /**
     * Standard VT100 terminal width limit in columns.
     */
    public static final int SCREEN_WIDTH = 80;

    private static final String TRUNCATION_SUFFIX = "..";

    private TableFormatter() {
        throw new AssertionError("TableFormatter cannot be instantiated.");
    }

    /**
     * Fits a string into an exact column width by trimming surrounding spaces,
     * right-padding shorter values with spaces, or truncating longer values
     * with a two-dot suffix.
     *
     * @param text  the input text to format, may be null
     * @param width the exact target character width
     * @return a formatted string of length equal to width, or empty if width is
     *         non-positive
     */
    public static String fit(String text, int width) {
        if (width <= 0) {
            return "";
        }
        String clean = (text == null) ? "" : text.trim();
        clean = clean.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');

        if (clean.length() == width) {
            return clean;
        }
        if (clean.length() < width) {
            StringBuilder sb = new StringBuilder(width);
            sb.append(clean);
            while (sb.length() < width) {
                sb.append(' ');
            }
            return sb.toString();
        }
        if (width <= TRUNCATION_SUFFIX.length()) {
            return clean.substring(0, width);
        }
        return clean.substring(0, width - TRUNCATION_SUFFIX.length()) + TRUNCATION_SUFFIX;
    }

    /**
     * Right-aligns a string within an exact column width, truncating if needed.
     *
     * @param text  the input text to right-align, may be null
     * @param width the exact target character width
     * @return a right-aligned string of length equal to width
     */
    public static String fitRight(String text, int width) {
        if (width <= 0) {
            return "";
        }
        String clean = (text == null) ? "" : text.trim();
        if (clean.length() >= width) {
            return fit(clean, width);
        }
        StringBuilder sb = new StringBuilder(width);
        int padding = width - clean.length();
        for (int i = 0; i < padding; i++) {
            sb.append(' ');
        }
        sb.append(clean);
        return sb.toString();
    }

    /**
     * Centers a string within an exact column width using space padding.
     *
     * @param text  the input text to center, may be null
     * @param width the exact target character width
     * @return a centered string of length equal to width
     */
    public static String center(String text, int width) {
        if (width <= 0) {
            return "";
        }
        String clean = (text == null) ? "" : text.trim();
        if (clean.length() >= width) {
            return fit(clean, width);
        }
        int totalPadding = width - clean.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < leftPadding; i++) {
            sb.append(' ');
        }
        sb.append(clean);
        for (int i = 0; i < rightPadding; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Repeats an ASCII character a specified number of times.
     *
     * @param ch    the character to repeat
     * @param count the number of repetitions
     * @return a string containing ch repeated count times
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
