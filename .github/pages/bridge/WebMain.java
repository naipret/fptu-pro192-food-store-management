package web;

import fptu.pro192.foodstoremanagement.Main;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Web bootstrap entry point designed for the CheerpJ WebAssembly browser
 * environment.
 * Redirects standard input and output streams to WebTerminalManager, and delegates
 * application startup to Main.
 */
public final class WebMain {

    private WebMain() {
    }

    /**
     * Initializes the web terminal stream bridges and launches the main application loop.
     *
     * @param args command-line arguments forwarded to Main
     */
    public static void main(String[] args) {
        try {
            configureStandardOutput();
            configureStandardInput();
            Main.main(args);
        } catch (Exception e) {
            WebTerminalManager.print("[ERROR] Web bootstrap initialization error: " + e.getMessage() + "\n");
        }
    }

    private static void configureStandardOutput() throws IOException {
        PrintStream webOut = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                WebTerminalManager.print(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                if (b != null && len > 0) {
                    WebTerminalManager.print(new String(b, off, len, StandardCharsets.UTF_8));
                }
            }
        }, true, StandardCharsets.UTF_8.name());

        System.setOut(webOut);
        System.setErr(webOut);
    }

    private static void configureStandardInput() {
        InputStream webIn = new InputStream() {
            private ByteArrayInputStream buffer = new ByteArrayInputStream(new byte[0]);

            @Override
            public int read() throws IOException {
                while (buffer.available() == 0) {
                    String line = WebTerminalManager.readLine();
                    if (line == null) {
                        return -1;
                    }
                    buffer = new ByteArrayInputStream((line + "\n").getBytes(StandardCharsets.UTF_8));
                }
                return buffer.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (b == null) {
                    throw new NullPointerException("Buffer cannot be null.");
                }
                if (off < 0 || len < 0 || len > b.length - off) {
                    throw new IndexOutOfBoundsException("Invalid offset or length.");
                }
                if (len == 0) {
                    return 0;
                }

                int first = read();
                if (first == -1) {
                    return -1;
                }
                b[off] = (byte) first;

                int bytesRead = 1;
                while (bytesRead < len && buffer.available() > 0) {
                    int next = read();
                    if (next == -1) {
                        break;
                    }
                    b[off + bytesRead] = (byte) next;
                    bytesRead++;
                }
                return bytesRead;
            }
        };

        ScannerManager.getInstance().setInputStream(webIn);
    }
}
