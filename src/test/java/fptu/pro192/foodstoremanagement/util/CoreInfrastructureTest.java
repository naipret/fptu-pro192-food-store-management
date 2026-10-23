package fptu.pro192.foodstoremanagement.util;

import fptu.pro192.foodstoremanagement.view.Menu;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated verification test suite for the utility and core menu
 * infrastructure: CsvUtil, InputValidator, TableFormatter, ScannerManager,
 * ConsoleUtil, and Menu (BR1, BR2, BR3, BR4, BR5, BR6, BR7, BR8, BR25, BR26).
 */
public class CoreInfrastructureTest {

    private CoreInfrastructureTest() {
    }

    /**
     * Executes all core infrastructure verification tests.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if an unexpected I/O error occurs
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Running CoreInfrastructureTest...");
        testIdNormalizationAndValidation();
        testTextAndTokenValidation();
        testPhoneDateAndNumericValidation();
        testTableFormatterAlignment();
        testCsvUtilAtomicReadWriteAndSanitization();
        testConsoleUtilAndMenuTemplate();
        System.out.println("CoreInfrastructureTest PASSED (All assertions succeeded).");
    }

    private static void testIdNormalizationAndValidation() {
        assertEquals("P00001", InputValidator.normalizeProductId("1"), "Shorthand product ID 1");
        assertEquals("P00012", InputValidator.normalizeProductId("p12"), "Shorthand product ID p12");
        assertEquals("P00001", InputValidator.validateProductId("P00001"), "Canonical product ID");
        assertEquals("B00005", InputValidator.normalizeBatchId("B5"), "Shorthand batch ID B5");
        assertEquals("C00099", InputValidator.normalizeCustomerId("99"), "Shorthand customer ID 99");
        assertEquals("O00100", InputValidator.normalizeOrderId("o100"), "Shorthand order ID o100");

        assertThrows(new Runnable() {
            @Override
            public void run() {
                InputValidator.validateProductId("P1");
            }
        }, "Strict validateProductId must reject unpadded P1");

        assertThrows(new Runnable() {
            @Override
            public void run() {
                InputValidator.normalizeProductId("P00000");
            }
        }, "Product ID 00000 must be rejected");
    }

    private static void testTextAndTokenValidation() {
        assertEquals("ST25 Fragrant Rice 5kg",
                InputValidator.normalizeText("  ST25   Fragrant Rice 5kg ", "Product name"),
                "Normalized English product name");
        assertEquals("Nguyễn Văn An",
                InputValidator.normalizeText("Nguyễn Văn An", "Full name"),
                "Unicode Vietnamese name");

        assertThrows(new Runnable() {
            @Override
            public void run() {
                InputValidator.normalizeText("=CMD|' /C calc'!A0", "Product name");
            }
        }, "CSV formula injection starting with '=' must be rejected");

        assertThrows(new Runnable() {
            @Override
            public void run() {
                InputValidator.normalizeText("123456", "Product name");
            }
        }, "Purely numeric name without letters must be rejected");

        assertEquals("Grains",
                InputValidator.normalizeSingleWordToken("grains", "Category"),
                "Single-word category token");

        assertThrows(new Runnable() {
            @Override
            public void run() {
                InputValidator.normalizeSingleWordToken("Grains/Noodles", "Category");
            }
        }, "Slash-separated category token must be rejected (BR5)");
    }

    private static void testPhoneDateAndNumericValidation() {
        assertEquals("0901234567", InputValidator.normalizePhone("090 123 4567"), "Phone with spaces");
        assertEquals("0912345678", InputValidator.normalizePhone("+84-912-345-678"), "Phone with +84");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                InputValidator.normalizePhone("0123456789");
            }
        }, "Invalid 01 prefix phone must be rejected");

        LocalDate date = InputValidator.parseDisplayDate("19/10/2026", "Import date");
        assertEquals(LocalDate.of(2026, 10, 19), date, "Parsed dd/MM/yyyy date");
        assertEquals("19/10/2026", InputValidator.formatDisplayDate(date), "Formatted dd/MM/yyyy date");

        assertThrows(new Runnable() {
            @Override
            public void run() {
                InputValidator.parseDisplayDate("31/02/2026", "Expiration date");
            }
        }, "Non-existent calendar date 31/02/2026 must be rejected");

        YearMonth ym = InputValidator.parseDisplayMonth("10/2026");
        assertEquals(YearMonth.of(2026, 10), ym, "Parsed MM/yyyy month");

        assertEquals(165000.0, InputValidator.validatePositivePrice(165000.0), "Valid positive price");
        assertEquals(10, InputValidator.validatePositiveQuantity(10, "Qty"), "Valid positive int qty");
        assertEquals("165,000", InputValidator.formatCurrency(165000.0), "Formatted currency");
    }

    private static void testTableFormatterAlignment() {
        assertEquals(80, TableFormatter.SCREEN_WIDTH, "80-column screen width constant");
        String padded = TableFormatter.fit("Rice", 10);
        assertEquals(10, padded.length(), "Padded width must be 10");
        assertEquals("Rice      ", padded, "Right-padded cell content");

        String truncated = TableFormatter.fit("Extra Long Product Name Beyond Width", 12);
        assertEquals(12, truncated.length(), "Truncated width must be 12");
        assertEquals("Extra Long..", truncated, "Truncated string must end with '..'");

        String centered = TableFormatter.center("TITLE", 11);
        assertEquals(11, centered.length(), "Centered string length");
    }

    private static void testCsvUtilAtomicReadWriteAndSanitization() throws Exception {
        Path tempDir = Files.createTempDirectory("foodstore_csv_test_");
        Path csvFile = tempDir.resolve("sample.csv");

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "id", "name", "address" });
        rows.add(new String[] { "C00001", "Trần Thị Bình", "123 Lê Lợi, District 1, HCMC" });
        rows.add(new String[] { "C00002", "Nguyễn Văn An", "Quote \"Test\" Inside" });

        CsvUtil.writeAtomic(csvFile, rows);
        assertTrue(Files.exists(csvFile), "CSV file must exist after atomic write");

        List<String[]> loaded = CsvUtil.readCsv(csvFile);
        assertEquals(3, loaded.size(), "Loaded row count including header");
        assertEquals("id", loaded.get(0)[0], "Header column 0");
        assertEquals("123 Lê Lợi, District 1, HCMC", loaded.get(1)[2], "Quoted comma field round-trip");
        assertEquals("Nguyễn Văn An", loaded.get(2)[1], "Unicode field round-trip");
        assertEquals("Quote \"Test\" Inside", loaded.get(2)[2], "Escaped double quotes round-trip");

        Files.deleteIfExists(csvFile);
        Files.deleteIfExists(tempDir);
    }

    private static void testConsoleUtilAndMenuTemplate() {
        String simulatedInput = "1\n0\n";
        ScannerManager.getInstance().setInputStream(
                new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));

        final int[] handledChoice = { 0 };
        Menu sampleMenu = new Menu("SAMPLE TEST MENU", new String[] { "Action One" }) {
            @Override
            protected void handleChoice(int choice) {
                handledChoice[0] = choice;
            }

            @Override
            protected boolean shouldPauseAfterAction() {
                return false;
            }
        };
        sampleMenu.display();
        assertEquals(1, handledChoice[0], "Menu must dispatch option 1 before exiting on 0");
        ScannerManager.getInstance().setInputStream(System.in);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Assertion failed [" + message + "]: expected <"
                    + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertThrows(Runnable action, String message) {
        try {
            action.run();
        } catch (IllegalArgumentException e) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException: " + message);
    }
}
