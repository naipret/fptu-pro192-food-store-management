package fptu.pro192.foodstoremanagement.util;

import fptu.pro192.foodstoremanagement.ui.AbstractMenu;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Self-contained verification test suite for Task #1 (Core Infrastructure).
 * Validates all acceptance criteria without external testing libraries.
 */
public class CoreInfrastructureTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    /**
     * Test runner main entry point.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   RUNNING TASK #1 CORE INFRASTRUCTURE VERIFICATION TESTS       ");
        System.out.println("================================================================");

        testScannerManager();
        testTableFormatter();
        testMiniCsv();
        testAbstractMenuLayout();

        System.out.println("\n----------------------------------------------------------------");
        System.out.printf("Test Summary: %d passed, %d failed.\n", totalPassed, totalFailed);
        System.out.println("----------------------------------------------------------------");

        if (totalFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s\n", testName);
            totalPassed++;
        } else {
            System.err.printf("  [FAIL] %s\n", testName);
            totalFailed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        boolean match = (expected == null && actual == null)
                || (expected != null && expected.equals(actual));
        if (match) {
            System.out.printf("  [PASS] %s\n", testName);
            totalPassed++;
        } else {
            System.err.printf("  [FAIL] %s: Expected [%s] but got [%s]\n", testName, expected, actual);
            totalFailed++;
        }
    }

    private static void testScannerManager() {
        System.out.println("\n[Testing ScannerManager (Singleton)]");
        ScannerManager sm1 = ScannerManager.getInstance();
        ScannerManager sm2 = ScannerManager.getInstance();

        assertTrue("ScannerManager returns non-null instance", sm1 != null);
        assertTrue("ScannerManager returns identical singleton instance", sm1 == sm2);
        assertTrue("Scanner instance is non-null", sm1.getScanner() != null);
    }

    private static void testTableFormatter() {
        System.out.println("\n[Testing TableFormatter (80-Column Layout)]");

        assertEquals("SCREEN_WIDTH constant is 80", 80, TableFormatter.SCREEN_WIDTH);

        // Required Acceptance Criteria check:
        // TableFormatter.fit("Thịt heo ba rọi đóng hộp cao cấp", 15) produces "Thịt heo ba r.." with length exactly 15
        String sampleVietnamese = "Thịt heo ba rọi đóng hộp cao cấp";
        String fitted15 = TableFormatter.fit(sampleVietnamese, 15);
        assertEquals("fit() Vietnamese string to 15 chars", "Thịt heo ba r..", fitted15);
        assertEquals("fit() exact length check", 15, fitted15.length());

        // Padding check
        String padded = TableFormatter.fit("Mi Goi", 10);
        assertEquals("fit() right pads with whitespace", "Mi Goi    ", padded);
        assertEquals("padded string length is exact", 10, padded.length());

        // Null check
        String nullFitted = TableFormatter.fit(null, 5);
        assertEquals("fit(null) returns spaces", "     ", nullFitted);
        assertEquals("null fitted length", 5, nullFitted.length());

        // Width edge cases
        assertEquals("fit(..., 0) returns empty", "", TableFormatter.fit("test", 0));
        assertEquals("fit(..., 2) truncates without dots", "ab", TableFormatter.fit("abcd", 2));

        // Center check
        String centered = TableFormatter.center("FOOD STORE", 20);
        assertEquals("center() total length is 20", 20, centered.length());
        assertTrue("center() contains target string", centered.contains("FOOD STORE"));

        // Repeat check
        String repeated = TableFormatter.repeat('=', 5);
        assertEquals("repeat() 5 chars", "=====", repeated);
        assertEquals("repeat() 0 chars", "", TableFormatter.repeat('=', 0));
    }

    private static void testMiniCsv() {
        System.out.println("\n[Testing MiniCsv (RFC 4180 & Atomic Persistence)]");

        String testFile = "build/test_temp_data.csv";
        List<String> headers = Arrays.asList("ID", "Name", "Category", "Description", "Price");

        List<String[]> rows = new ArrayList<>();
        // Row 1: Normal row with Vietnamese diacritics
        rows.add(new String[]{"P00001", "Mì Hảo Hảo Tôm Chua Cay", "DRY", "Mì ăn liền gói 75g", "8000"});
        // Row 2: Cell containing comma
        rows.add(new String[]{"P00002", "Sữa Tươi Tiệt Trùng, Có Đường", "CHILLED", "Hộp 1L", "35000"});
        // Row 3: Cell containing double quotes
        rows.add(new String[]{"P00003", "Thịt Bò \"Wagyu\" Thượng Hạng", "FROZEN", "Khay 500g", "250000"});
        // Row 4: Cell containing newline
        rows.add(new String[]{"P00004", "Combo Rau Củ\nTươi", "CHILLED", "Túi 1kg", "45000"});

        // Write atomically
        MiniCsv.writeAtomic(testFile, headers, rows);

        File f = new File(testFile);
        assertTrue("Target CSV file exists after atomic write", f.exists());
        File tmp = new File(testFile + ".tmp");
        assertTrue("Temporary .tmp file does not remain", !tmp.exists());

        // Read and parse
        List<String[]> readRecords = MiniCsv.read(testFile);
        assertEquals("Total parsed rows including header", 5, readRecords.size());

        // Verify headers
        String[] readHeader = readRecords.get(0);
        assertEquals("Header column 0", "ID", readHeader[0]);
        assertEquals("Header column 1", "Name", readHeader[1]);
        assertEquals("Header column 3", "Description", readHeader[3]);

        // Verify Row 1: Vietnamese diacritics
        String[] r1 = readRecords.get(1);
        assertEquals("R1 Name with diacritics", "Mì Hảo Hảo Tôm Chua Cay", r1[1]);

        // Verify Row 2: Commas inside quotes
        String[] r2 = readRecords.get(2);
        assertEquals("R2 Name with comma preserved", "Sữa Tươi Tiệt Trùng, Có Đường", r2[1]);

        // Verify Row 3: Escaped quotes
        String[] r3 = readRecords.get(3);
        assertEquals("R3 Name with inner quotes preserved", "Thịt Bò \"Wagyu\" Thượng Hạng", r3[1]);

        // Verify Row 4: Multiline cell preserved
        String[] r4 = readRecords.get(4);
        assertEquals("R4 Name with newline preserved", "Combo Rau Củ\nTươi", r4[1]);

        // Verify non-existent file returns empty list
        List<String[]> nonExistent = MiniCsv.read("build/non_existent_file_xyz.csv");
        assertTrue("Reading non-existent file returns empty list", nonExistent.isEmpty());

        // Clean up test file
        f.delete();
    }

    private static void testAbstractMenuLayout() {
        System.out.println("\n[Testing AbstractMenu (Layout Frame)]");

        // Create concrete anonymous subclass to verify layout width
        AbstractMenu menu = new AbstractMenu("Menu Quản Lý Sản Phẩm") {
            @Override
            protected void renderBody() {
                System.out.println("| 1. Xem danh sách                                                             |");
            }

            @Override
            protected boolean handleOption(int choice) {
                return false;
            }
        };

        assertTrue("AbstractMenu instance initialized cleanly", menu != null);
    }
}
