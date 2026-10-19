package fptu.pro192.foodstoremanagement.core;

import fptu.pro192.foodstoremanagement.Main;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.repository.OrderRepository;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;
import fptu.pro192.foodstoremanagement.service.CustomerService;
import fptu.pro192.foodstoremanagement.service.OrderService;
import fptu.pro192.foodstoremanagement.service.ProductService;
import fptu.pro192.foodstoremanagement.service.ReportService;
import fptu.pro192.foodstoremanagement.ui.CustomerMenu;
import fptu.pro192.foodstoremanagement.ui.MainMenu;
import fptu.pro192.foodstoremanagement.ui.ProductMenu;
import fptu.pro192.foodstoremanagement.ui.ReportMenu;
import fptu.pro192.foodstoremanagement.ui.SalesMenu;
import fptu.pro192.foodstoremanagement.util.ScannerManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Scanner;

/**
 * Verification test suite for Task #9 (MainMenu and App Wiring Integration).
 * Pure Java 8, zero third-party dependencies.
 */
public class MainMenuIntegrationTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("    RUNNING TASK #9 MAIN MENU & APP WIRING INTEGRATION TESTS    ");
        System.out.println("================================================================");

        testMainMenuInstantiationAndInvariants();
        testNullParameterValidations();
        testSubmenuRoutingAndOptionDispatch();
        testDisplayCleanExitOnOptionZero();
        testDisplayCleanExitOnOptionSix();
        testMainCompositionRootAndDataDirCreation();

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

    private static void assertThrows(String testName, Class<? extends Throwable> expectedType, String expectedMessageSubstring, Runnable runnable) {
        try {
            runnable.run();
            System.err.printf("  [FAIL] %s: Expected %s but no exception was thrown\n", testName, expectedType.getSimpleName());
            totalFailed++;
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                if (expectedMessageSubstring == null || (t.getMessage() != null && t.getMessage().contains(expectedMessageSubstring))) {
                    System.out.printf("  [PASS] %s\n", testName);
                    totalPassed++;
                } else {
                    System.err.printf("  [FAIL] %s: Expected message containing [%s] but got [%s]\n",
                            testName, expectedMessageSubstring, t.getMessage());
                    totalFailed++;
                }
            } else {
                System.err.printf("  [FAIL] %s: Expected %s but caught %s (%s)\n",
                        testName, expectedType.getSimpleName(), t.getClass().getSimpleName(), t.getMessage());
                totalFailed++;
            }
        }
    }

    private static void testMainMenuInstantiationAndInvariants() {
        System.out.println("\n--- 1. Testing MainMenu Instantiation & Invariants ---");

        String pPath = "test_main_p.csv";
        String bPath = "test_main_b.csv";
        String cPath = "test_main_c.csv";
        String oPath = "test_main_o.csv";
        String dPath = "test_main_d.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);

            ProductService pService = new ProductService(pRepo);
            CustomerService cService = new CustomerService(cRepo);
            OrderService oService = new OrderService(oRepo, pService, cService);
            ReportService rService = new ReportService(oRepo, pRepo, cRepo);

            ProductMenu pMenu = new ProductMenu(pService);
            CustomerMenu cMenu = new CustomerMenu(cService);
            SalesMenu sMenu = new SalesMenu(oService, pService, cService);
            ReportMenu rMenu = new ReportMenu(rService);

            MainMenu menu = new MainMenu(pMenu, cMenu, sMenu, rMenu);

            assertEquals("MainMenu title matches", "HỆ THỐNG QUẢN LÝ BÁN HÀNG THỰC PHẨM", menu.getTitle());
            assertEquals("ProductMenu reference matches", pMenu, menu.getProductMenu());
            assertEquals("CustomerMenu reference matches", cMenu, menu.getCustomerMenu());
            assertEquals("SalesMenu reference matches", sMenu, menu.getSalesMenu());
            assertEquals("ReportMenu reference matches", rMenu, menu.getReportMenu());
            assertTrue("Scanner is not null", menu.getScanner() != null);

            // Test default constructor
            MainMenu defaultMenu = new MainMenu();
            assertTrue("Default constructor productMenu instantiated", defaultMenu.getProductMenu() != null);
            assertTrue("Default constructor customerMenu instantiated", defaultMenu.getCustomerMenu() != null);
            assertTrue("Default constructor salesMenu instantiated", defaultMenu.getSalesMenu() != null);
            assertTrue("Default constructor reportMenu instantiated", defaultMenu.getReportMenu() != null);

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testNullParameterValidations() {
        System.out.println("\n--- 2. Testing MainMenu Null Validations ---");

        ProductMenu pMenu = new ProductMenu(new ProductService(new ProductRepository("temp_p.csv", "temp_b.csv")));
        CustomerMenu cMenu = new CustomerMenu(new CustomerService(new CustomerRepository("temp_c.csv")));
        SalesMenu sMenu = new SalesMenu(new OrderService());
        ReportMenu rMenu = new ReportMenu(new ReportService());

        assertThrows("Null ProductMenu throws IllegalArgumentException", IllegalArgumentException.class,
                "ProductMenu cannot be null", () -> new MainMenu(null, cMenu, sMenu, rMenu));

        assertThrows("Null CustomerMenu throws IllegalArgumentException", IllegalArgumentException.class,
                "CustomerMenu cannot be null", () -> new MainMenu(pMenu, null, sMenu, rMenu));

        assertThrows("Null SalesMenu throws IllegalArgumentException", IllegalArgumentException.class,
                "SalesMenu cannot be null", () -> new MainMenu(pMenu, cMenu, null, rMenu));

        assertThrows("Null ReportMenu throws IllegalArgumentException", IllegalArgumentException.class,
                "ReportMenu cannot be null", () -> new MainMenu(pMenu, cMenu, sMenu, null));

        cleanFiles("temp_p.csv", "temp_b.csv", "temp_c.csv");
    }

    private static class TrackingProductMenu extends ProductMenu {
        boolean displayCalled = false;

        TrackingProductMenu(ProductService service) {
            super(service);
        }

        @Override
        public void display() {
            displayCalled = true;
        }
    }

    private static class TrackingCustomerMenu extends CustomerMenu {
        boolean displayCalled = false;

        TrackingCustomerMenu(CustomerService service) {
            super(service);
        }

        @Override
        public void display() {
            displayCalled = true;
        }
    }

    private static class TrackingSalesMenu extends SalesMenu {
        boolean displayCalled = false;

        TrackingSalesMenu(OrderService service) {
            super(service);
        }

        @Override
        public void display() {
            displayCalled = true;
        }
    }

    private static class TrackingReportMenu extends ReportMenu {
        boolean displayCalled = false;

        TrackingReportMenu(ReportService service) {
            super(service);
        }

        @Override
        public void display() {
            displayCalled = true;
        }
    }

    private static void testSubmenuRoutingAndOptionDispatch() {
        System.out.println("\n--- 3. Testing Submenu Routing & Option Dispatch ---");

        TrackingProductMenu pMenu = new TrackingProductMenu(new ProductService(new ProductRepository("r_p.csv", "r_b.csv")));
        TrackingCustomerMenu cMenu = new TrackingCustomerMenu(new CustomerService(new CustomerRepository("r_c.csv")));
        TrackingSalesMenu sMenu = new TrackingSalesMenu(new OrderService());
        TrackingReportMenu rMenu = new TrackingReportMenu(new ReportService());

        MainMenu menu = new MainMenu(pMenu, cMenu, sMenu, rMenu);

        try {
            Method handleOption = MainMenu.class.getDeclaredMethod("handleOption", int.class);
            handleOption.setAccessible(true);

            // Choice 1 -> productMenu.display()
            boolean r1 = (boolean) handleOption.invoke(menu, 1);
            assertTrue("Choice 1 returns true", r1);
            assertTrue("Choice 1 routes to ProductMenu", pMenu.displayCalled);

            // Choice 2 -> customerMenu.display()
            boolean r2 = (boolean) handleOption.invoke(menu, 2);
            assertTrue("Choice 2 returns true", r2);
            assertTrue("Choice 2 routes to CustomerMenu", cMenu.displayCalled);

            // Choice 3 -> salesMenu.display()
            boolean r3 = (boolean) handleOption.invoke(menu, 3);
            assertTrue("Choice 3 returns true", r3);
            assertTrue("Choice 3 routes to SalesMenu", sMenu.displayCalled);

            // Choice 4 -> routes to inventory in ProductMenu
            pMenu.displayCalled = false;
            boolean r4 = (boolean) handleOption.invoke(menu, 4);
            assertTrue("Choice 4 returns true", r4);
            assertTrue("Choice 4 routes to inventory in ProductMenu", pMenu.displayCalled);

            // Choice 5 -> reportMenu.display()
            boolean r5 = (boolean) handleOption.invoke(menu, 5);
            assertTrue("Choice 5 returns true", r5);
            assertTrue("Choice 5 routes to ReportMenu", rMenu.displayCalled);

            // Choice 6 -> exit (returns false)
            boolean r6 = (boolean) handleOption.invoke(menu, 6);
            assertEquals("Choice 6 returns false for exit", false, r6);

            // Invalid choices -> return false
            boolean rInvalid = (boolean) handleOption.invoke(menu, 99);
            assertEquals("Invalid choice returns false", false, rInvalid);

        } catch (Exception e) {
            System.err.println("  [FAIL] testSubmenuRoutingAndOptionDispatch failed with reflection error: " + e.getMessage());
            totalFailed++;
        } finally {
            cleanFiles("r_p.csv", "r_b.csv", "r_c.csv");
        }
    }

    private static void testDisplayCleanExitOnOptionZero() {
        System.out.println("\n--- 4. Testing Display Lifecycle Clean Exit on Option [0] ---");

        InputStream originalIn = System.in;
        try {
            // Simulate user input "0\n"
            ByteArrayInputStream in = new ByteArrayInputStream("0\n".getBytes());
            System.setIn(in);
            resetScannerManager(new Scanner(in));

            MainMenu menu = new MainMenu();
            menu.display();
            assertTrue("MainMenu.display() exits cleanly on option 0 without throwing", true);

        } catch (Exception e) {
            System.err.println("  [FAIL] Option 0 exit failed: " + e.getMessage());
            totalFailed++;
        } finally {
            System.setIn(originalIn);
            resetScannerManager(new Scanner(originalIn));
        }
    }

    private static void testDisplayCleanExitOnOptionSix() {
        System.out.println("\n--- 5. Testing Display Lifecycle Clean Exit on Option [6] ---");

        InputStream originalIn = System.in;
        try {
            // Simulate user input "6\n"
            ByteArrayInputStream in = new ByteArrayInputStream("6\n".getBytes());
            System.setIn(in);
            resetScannerManager(new Scanner(in));

            MainMenu menu = new MainMenu();
            menu.display();
            assertTrue("MainMenu.display() exits cleanly on option 6 without throwing", true);

        } catch (Exception e) {
            System.err.println("  [FAIL] Option 6 exit failed: " + e.getMessage());
            totalFailed++;
        } finally {
            System.setIn(originalIn);
            resetScannerManager(new Scanner(originalIn));
        }
    }

    private static void testMainCompositionRootAndDataDirCreation() {
        System.out.println("\n--- 6. Testing Main.main() Composition Root & Data Directory ---");

        InputStream originalIn = System.in;
        try {
            // Simulate user entering 0 to exit Main.main() cleanly
            ByteArrayInputStream in = new ByteArrayInputStream("0\n".getBytes());
            System.setIn(in);
            resetScannerManager(new Scanner(in));

            Main.main(new String[0]);
            assertTrue("Main.main() executes and terminates cleanly with exit code flow", true);

            File dataDir = new File("data");
            assertTrue("Main ensures data directory exists", dataDir.exists() && dataDir.isDirectory());

        } catch (Exception e) {
            System.err.println("  [FAIL] Main.main() failed: " + e.getMessage());
            totalFailed++;
        } finally {
            System.setIn(originalIn);
            resetScannerManager(new Scanner(originalIn));
        }
    }

    private static void resetScannerManager(Scanner newScanner) {
        try {
            Field instanceField = ScannerManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            ScannerManager sm = ScannerManager.getInstance();

            Field scannerField = ScannerManager.class.getDeclaredField("scanner");
            scannerField.setAccessible(true);
            scannerField.set(sm, newScanner);
        } catch (Exception ignored) {
        }
    }

    private static void cleanFiles(String... paths) {
        for (String p : paths) {
            if (p != null) {
                File f = new File(p);
                if (f.exists()) {
                    f.delete();
                }
            }
        }
    }
}
