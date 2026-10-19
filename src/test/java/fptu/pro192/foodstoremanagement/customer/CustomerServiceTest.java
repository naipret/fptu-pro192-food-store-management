package fptu.pro192.foodstoremanagement.customer;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import fptu.pro192.foodstoremanagement.view.CustomerMenu;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Automated verification test suite for CustomerController business rules,
 * phone uniqueness, tier upgrades, soft deletion, search queries, and
 * CustomerMenu interactive flows (BR2, BR3, BR15, BR25, BR27).
 */
public class CustomerServiceTest {

    private CustomerServiceTest() {
    }

    /**
     * Executes all customer controller and menu verification tests.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if an unexpected I/O error occurs
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Running CustomerServiceTest...");
        testCustomerCrudDuplicateGuardsAndSearch();
        testCustomerMenuInteractiveFlow();
        System.out.println("CustomerServiceTest PASSED (All assertions succeeded).");
    }

    private static void testCustomerCrudDuplicateGuardsAndSearch() throws Exception {
        Path tempDir = Files.createTempDirectory("customer_service_test_");
        Path customersCsv = tempDir.resolve("customers.csv");

        final CustomerController controller = new CustomerController(customersCsv);
        controller.registerCustomer("C00001", "Nguyễn Văn An", "0901234567", "12 Lê Lợi, District 1", "Regular");
        controller.registerCustomer("C00002", "Trần Thị Bình", "0912345678", "34 Nguyễn Huệ, District 1", "VIP");

        assertThrows(new Runnable() {
            @Override
            public void run() {
                controller.registerCustomer("C00003", "Duplicate Phone User", "0901234567", "District 2", "Regular");
            }
        }, "Duplicate active phone number must be rejected (BR25)");

        Customer upgraded = controller.updateCustomer("C00001", null, null, "99 Pasteur, District 3", "VIP");
        assertEquals("VIP", upgraded.getCustomerType(), "Customer C00001 upgraded to VIP");
        assertEquals("99 Pasteur, District 3", upgraded.getAddress(), "Customer C00001 updated address");

        List<Customer> searchMatches = controller.search("Bình");
        assertEquals(1, searchMatches.size(), "Keyword search for Bình");
        assertEquals("C00002", searchMatches.get(0).getId(), "Matched customer C00002");

        controller.deleteCustomer("C00001");
        assertTrue(controller.findById("C00001") == null, "Soft-deleted customer hidden from active lookup");
        assertTrue(controller.findByIdIncludeDeleted("C00001") != null, "Soft-deleted customer retained for order history");

        Files.deleteIfExists(customersCsv);
        Files.deleteIfExists(tempDir);
    }

    private static void testCustomerMenuInteractiveFlow() throws Exception {
        Path tempDir = Files.createTempDirectory("customer_menu_test_");
        Path customersCsv = tempDir.resolve("customers.csv");

        CustomerController controller = new CustomerController(customersCsv);
        CustomerMenu customerMenu = new CustomerMenu(controller);

        String simulatedInput = "1\n\nLê Minh Khoa\n0987654321\n50 Điện Biên Phủ, District 3\n2\n\n4\n\n0\n";
        ScannerManager.getInstance().setInputStream(
                new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));
        customerMenu.display();

        Customer created = controller.findById("C00001");
        assertTrue(created != null, "Customer C00001 registered via CustomerMenu");
        assertEquals("Lê Minh Khoa", created.getFullName(), "Registered customer name");
        assertEquals("VIP", created.getCustomerType(), "Selected option 2 (VIP) in CustomerMenu");
        ScannerManager.getInstance().resetToStandardInput();

        Files.deleteIfExists(customersCsv);
        Files.deleteIfExists(tempDir);
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
