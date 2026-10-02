package fptu.pro192.foodstoremanagement.util;

import java.util.Scanner;

/**
 * Singleton manager for the application standard input {@link Scanner}.
 * <p>
 * Centralizes standard input stream reading from {@code System.in} to prevent premature stream
 * closures or descriptor exhaustion caused by creating and closing multiple {@link Scanner}
 * instances throughout the application lifecycle.
 */
public class ScannerManager {

    private static ScannerManager instance;
    private final Scanner scanner;

    /**
     * Private constructor initializing the persistent {@link Scanner} bound to {@code System.in}.
     */
    private ScannerManager() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Retrieves the thread-safe singleton instance of {@link ScannerManager}.
     *
     * @return The single application-wide instance of {@link ScannerManager}
     */
    public static synchronized ScannerManager getInstance() {
        if (instance == null) {
            instance = new ScannerManager();
        }
        return instance;
    }

    /**
     * Returns the managed {@link Scanner} instance wrapping {@code System.in}.
     *
     * @return The active {@link Scanner} reference
     */
    public Scanner getScanner() {
        return scanner;
    }
}
