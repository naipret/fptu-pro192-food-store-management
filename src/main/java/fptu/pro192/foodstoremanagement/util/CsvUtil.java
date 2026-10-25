package fptu.pro192.foodstoremanagement.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Stateless utility for reading and atomically writing RFC 4180 CSV files.
 * Uses line-oriented buffered I/O, supports quoted fields containing commas
 * or escaped double quotes, strips optional UTF-8 BOM markers, and writes
 * via a temporary sibling file followed by an atomic file move.
 */
public final class CsvUtil {

    private static final char UTF8_BOM = '\uFEFF';

    private CsvUtil() {
        throw new AssertionError("CsvUtil cannot be instantiated.");
    }

    /**
     * Reads all non-empty rows from a CSV file at the specified Path.
     * Returns an empty list if the file does not exist.
     *
     * @param path the Path of the CSV file to read
     * @return a list of string arrays representing parsed CSV rows
     */
    public static List<String[]> readCsv(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("CSV path cannot be null.");
        }
        List<String[]> records = new ArrayList<>();
        if (!Files.exists(path)) {
            return records;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine && !line.isEmpty() && line.charAt(0) == UTF8_BOM) {
                    line = line.substring(1);
                }
                firstLine = false;
                if (line.trim().isEmpty()) {
                    continue;
                }
                records.add(splitCsvLine(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file: " + path, e);
        }
        return records;
    }

    /**
     * Safely writes all rows (including the header row at index 0) to the
     * target Path by writing to a temporary .tmp file first and replacing the
     * target.
     *
     * @param targetPath the destination CSV file Path
     * @param rows       the complete list of rows to write
     */
    public static void writeToFile(Path targetPath, List<String[]> rows) {
        if (targetPath == null) {
            throw new IllegalArgumentException("Target CSV path cannot be null.");
        }
        Path absoluteTarget = targetPath.toAbsolutePath();
        Path parentDir = absoluteTarget.getParent();
        Path tempPath = Paths.get(absoluteTarget.toString() + ".tmp");

        try {
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(
                    tempPath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                if (rows != null) {
                    for (String[] row : rows) {
                        if (row != null) {
                            writer.write(formatCsvRow(row));
                            writer.newLine();
                        }
                    }
                }
                writer.flush();
            }

            try {
                Files.move(tempPath, absoluteTarget,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tempPath, absoluteTarget, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
            }
            throw new RuntimeException("Failed to write CSV file: " + targetPath, e);
        }
    }

    /**
     * Legacy alias for writeToFile.
     *
     * @param targetPath the destination CSV file Path
     * @param rows       the complete list of rows to write
     */
    public static void writeAtomic(Path targetPath, List<String[]> rows) {
        writeToFile(targetPath, rows);
    }

    /**
     * Splits a single CSV line into an array of cell values, handling quoted
     * fields that contain commas or escaped double quotes.
     *
     * @param line the raw CSV line to split
     * @return an array of unescaped cell values
     */
    public static String[] splitCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int length = line.length();

        for (int i = 0; i < length; i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < length && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    cells.add(current.toString().trim());
                    current.setLength(0);
                } else {
                    current.append(ch);
                }
            }
        }
        cells.add(current.toString().trim());
        return cells.toArray(new String[0]);
    }

    /**
     * Formats an array of cell values into a comma-separated line, escaping
     * cells that contain commas, quotes, or line breaks.
     *
     * @param row the array of cell strings
     * @return the formatted CSV line
     */
    public static String formatCsvRow(String[] row) {
        if (row == null || row.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeCell(row[i]));
        }
        return sb.toString();
    }

    /**
     * Escapes a single CSV cell value according to RFC 4180 rules.
     *
     * @param cell the raw cell string, may be null
     * @return the escaped CSV cell representation
     */
    public static String escapeCell(String cell) {
        if (cell == null || cell.isEmpty()) {
            return "";
        }
        String sanitized = cell.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ');
        boolean requiresQuotes = sanitized.indexOf(',') >= 0 || sanitized.indexOf('"') >= 0;
        if (!requiresQuotes) {
            return sanitized;
        }
        return "\"" + sanitized.replace("\"", "\"\"") + "\"";
    }
}
