package fptu.pro192.foodstoremanagement.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure, zero-dependency CSV parser and serializer compliant with RFC 4180.
 * <p>
 * Supports quoted fields containing commas, double quotes, newlines, and UTF-8 characters. Provides
 * atomic file persistence via temporary files and {@link StandardCopyOption#ATOMIC_MOVE} to prevent
 * file corruption.
 */
public final class MiniCsv {

    /**
     * Standard RFC 4180 line delimiter (CRLF).
     */
    private static final String CRLF = "\r\n";

    /**
     * Private constructor to prevent instantiation.
     */
    private MiniCsv() {}

    /**
     * Reads and parses a CSV file into a list of string arrays.
     * <p>
     * Complies with RFC 4180 by correctly handling:
     * <ul>
     * <li>Commas within double quotes (e.g. {@code "item 1, part 2"})</li>
     * <li>Escaped double quotes (e.g. {@code "He said ""Hello"""})</li>
     * <li>Multiline text inside quoted cells</li>
     * <li>Standard UTF-8 character encoding with optional Byte Order Mark (BOM) stripping</li>
     * </ul>
     * If the specified file does not exist, an empty list is returned.
     *
     * @param filePath Path to the CSV file
     * @return List of parsed rows, where each row is an array of cell values
     * @throws RuntimeException If an unrecoverable I/O error occurs during reading
     */
    public static List<String[]> read(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return Collections.emptyList();
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return Collections.emptyList();
        }

        List<String[]> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            List<String> currentRow = new ArrayList<>();
            StringBuilder currentField = new StringBuilder();
            boolean inQuotes = false;
            boolean isFirstChar = true;
            int c;

            while ((c = reader.read()) != -1) {
                char ch = (char) c;

                if (isFirstChar) {
                    isFirstChar = false;
                    if (ch == '\uFEFF') {
                        continue;
                    }
                }

                if (inQuotes) {
                    if (ch == '"') {
                        reader.mark(1);
                        int next = reader.read();
                        if (next == '"') {
                            // Escaped quote: "" -> "
                            currentField.append('"');
                        } else {
                            // End of quoted block
                            inQuotes = false;
                            if (next != -1) {
                                reader.reset();
                            }
                        }
                    } else {
                        currentField.append(ch);
                    }
                } else {
                    if (ch == '"') {
                        inQuotes = true;
                    } else if (ch == ',') {
                        currentRow.add(currentField.toString());
                        currentField.setLength(0);
                    } else if (ch == '\r') {
                        reader.mark(1);
                        int next = reader.read();
                        if (next != '\n' && next != -1) {
                            reader.reset();
                        }
                        currentRow.add(currentField.toString());
                        currentField.setLength(0);
                        records.add(currentRow.toArray(new String[currentRow.size()]));
                        currentRow = new ArrayList<>();
                    } else if (ch == '\n') {
                        currentRow.add(currentField.toString());
                        currentField.setLength(0);
                        records.add(currentRow.toArray(new String[currentRow.size()]));
                        currentRow = new ArrayList<>();
                    } else {
                        currentField.append(ch);
                    }
                }
            }

            // Flush remaining field/row if stream ended without trailing newline
            if (currentField.length() > 0 || !currentRow.isEmpty()) {
                currentRow.add(currentField.toString());
                records.add(currentRow.toArray(new String[currentRow.size()]));
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file: " + filePath, e);
        }

        return records;
    }

    /**
     * Atomically writes headers and rows to a CSV file.
     * <p>
     * Two-phase atomic write strategy:
     * <ol>
     * <li>Writes content to {@code filePath + ".tmp"} using UTF-8 encoding.</li>
     * <li>Flushes and closes the stream completely.</li>
     * <li>Replaces target file atomically via {@link StandardCopyOption#ATOMIC_MOVE} with fallback
     * to {@link StandardCopyOption#REPLACE_EXISTING}.</li>
     * </ol>
     *
     * @param filePath The target CSV destination file path
     * @param headers Optional list of header column names (may be null or empty)
     * @param rows List of data rows to persist
     * @throws RuntimeException If an I/O error occurs during write or file replacement
     */
    public static void writeAtomic(String filePath, List<String> headers, List<String[]> rows) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        File targetFile = new File(filePath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        File tempFile = new File(filePath + ".tmp");

        try {
            try (BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile),
                            StandardCharsets.UTF_8))) {

                // Write header row if provided
                if (headers != null && !headers.isEmpty()) {
                    writeRow(writer, headers.toArray(new String[headers.size()]));
                }

                // Write data rows
                if (rows != null) {
                    for (String[] row : rows) {
                        writeRow(writer, row);
                    }
                }
                writer.flush();
            }

            // Perform atomic move to replace destination file
            Path tempPath = tempFile.toPath();
            Path targetPath = targetFile.toPath();

            try {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                // Filesystem does not support atomic move; fallback to replace existing
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw new RuntimeException("Failed to atomically write CSV file: " + filePath, e);
        }
    }

    /**
     * Atomically writes data rows without a separate header list.
     *
     * @param filePath The target CSV file path
     * @param rows List of rows to persist
     * @throws RuntimeException If an I/O error occurs during write
     */
    public static void writeAtomic(String filePath, List<String[]> rows) {
        writeAtomic(filePath, null, rows);
    }

    /**
     * Writes a single CSV row to the buffered writer with RFC 4180 compliant escaping.
     *
     * @param writer Output buffered writer
     * @param row Array of cell values
     * @throws IOException If write fails
     */
    private static void writeRow(BufferedWriter writer, String[] row) throws IOException {
        if (row == null) {
            writer.write(CRLF);
            return;
        }

        for (int i = 0; i < row.length; i++) {
            if (i > 0) {
                writer.write(',');
            }
            writer.write(escapeCell(row[i]));
        }
        writer.write(CRLF);
    }

    /**
     * Escapes a single cell value according to RFC 4180 rules.
     *
     * @param cell Raw cell string
     * @return Escaped cell string, quoted if it contains comma, quote, or newline
     */
    private static String escapeCell(String cell) {
        if (cell == null) {
            return "";
        }
        boolean containsComma = cell.indexOf(',') >= 0;
        boolean containsQuote = cell.indexOf('"') >= 0;
        boolean containsNewline = cell.indexOf('\n') >= 0 || cell.indexOf('\r') >= 0;

        if (containsComma || containsQuote || containsNewline) {
            String escaped = cell.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return cell;
    }
}
