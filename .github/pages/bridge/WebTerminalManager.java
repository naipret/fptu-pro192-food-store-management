package web;

/**
 * Native bridge interface between the CheerpJ WebAssembly runtime and the
 * browser
 * xterm.js terminal emulator.
 *
 * Method implementations are supplied in JavaScript via the CheerpJ natives
 * initialization dictionary using standard JNI naming conventions:
 * Java_web_WebTerminalManager_readLine
 * Java_web_WebTerminalManager_print
 */
public final class WebTerminalManager {

    private WebTerminalManager() {
        throw new AssertionError("WebTerminalManager cannot be instantiated.");
    }

    /**
     * Reads a line of text entered by the user in the browser terminal emulator.
     * The JavaScript implementation returns a Promise that resolves when the user
     * presses the Enter key.
     *
     * @return the entered line of text, or null if the input stream has ended
     */
    public static native String readLine();

    /**
     * Sends a chunk of output text from the Java application to the browser
     * xterm.js terminal emulator.
     *
     * @param message the text chunk to render in the browser terminal
     */
    public static native void print(String message);

    /**
     * Retrieves dynamically generated seed CSV content from the browser runtime
     * based on the user's current local date.
     *
     * @param fileName the CSV file name (e.g., "products.csv")
     * @return the CSV file contents, or empty string if unavailable
     */
    public static native String getInitialCsv(String fileName);
}
