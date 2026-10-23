package fptu.pro192.foodstoremanagement.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Thread-safe Singleton manager providing a single shared Scanner bound to
 * standard input. Prevents multiple Scanner instances from competing for the
 * underlying System.in buffer or prematurely closing standard input.
 */
public final class ScannerManager {

    private static final ScannerManager INSTANCE = new ScannerManager();

    private Scanner scanner;

    private ScannerManager() {
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
    }

    /**
     * Returns the singleton ScannerManager instance.
     *
     * @return the shared ScannerManager instance
     */
    public static ScannerManager getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the shared Scanner instance wrapping standard input.
     *
     * @return the shared UTF-8 Scanner instance
     */
    public Scanner getScanner() {
        return this.scanner;
    }

    /**
     * Rebinds the underlying Scanner to a specified InputStream.
     * Used by automated integration tests to simulate console input.
     *
     * @param inputStream the non-null InputStream to read from
     */
    public void setInputStream(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null.");
        }
        this.scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
    }

}
