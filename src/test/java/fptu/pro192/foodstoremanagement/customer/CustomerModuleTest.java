package fptu.pro192.foodstoremanagement.customer;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.RegularCustomer;
import fptu.pro192.foodstoremanagement.model.VIPCustomer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Automated verification test suite for Customer, RegularCustomer, VIPCustomer,
 * and CustomerController CSV persistence (BR2, BR3, BR15, BR25).
 */
public class CustomerModuleTest {

    private CustomerModuleTest() {
    }

    /**
     * Executes all customer domain and persistence verification tests.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if an unexpected I/O error occurs
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Running CustomerModuleTest...");
        testPolymorphicDiscountAndInvariants();
        testCustomerControllerCsvPersistence();
        System.out.println("CustomerModuleTest PASSED (All assertions succeeded).");
    }

    private static void testPolymorphicDiscountAndInvariants() {
        Customer regular = new RegularCustomer("C00001", "Nguyễn Văn An", "0901234567", "12 Lê Lợi, District 1");
        Customer vip = new VIPCustomer("C00002", "Trần Thị Bình", "0912345678", "45 Nguyễn Huệ, District 1");

        assertEquals("Regular", regular.getCustomerType(), "RegularCustomer tier label");
        assertEquals("VIP", vip.getCustomerType(), "VIPCustomer tier label");
        assertEquals(0.0, regular.calculateDiscount(200000.0), "Regular customer receives 0% discount (BR15)");
        assertEquals(20000.0, vip.calculateDiscount(200000.0), "VIP customer receives 10% discount (BR15)");

        assertThrows(new Runnable() {
            @Override
            public void run() {
                new RegularCustomer("C00003", "12345", "0901234567", "District 3");
            }
        }, "Customer name without letters must be rejected (BR3)");

        assertThrows(new Runnable() {
            @Override
            public void run() {
                new VIPCustomer("C00004", "Lê Hoàng", "0123456789", "District 5");
            }
        }, "Invalid phone number must be rejected (BR25)");
    }

    private static void testCustomerControllerCsvPersistence() throws Exception {
        Path tempDir = Files.createTempDirectory("customer_module_test_");
        Path customersCsv = tempDir.resolve("customers.csv");

        CustomerController controller = new CustomerController(customersCsv);
        controller.registerCustomer("C00001", "Phạm Minh Tuấn", "0901112222", "10 Hai Bà Trưng, District 1", "Regular");
        controller.registerCustomer("C00002", "Võ Thị Mai", "0913334444", "88 Đồng Khởi, District 1", "VIP");

        List<String> lines = Files.readAllLines(customersCsv);
        assertEquals("type,id,fullName,phone,address,isDeleted",
                lines.get(0), "customers.csv header at line 0");

        CustomerController reloaded = new CustomerController(customersCsv);
        assertEquals(2, reloaded.getAllCustomers().size(), "Reloaded customer count");
        Customer loadedVip = reloaded.findById("C2");
        assertTrue(loadedVip instanceof VIPCustomer, "Reloaded C00002 instantiated as VIPCustomer");
        assertEquals(15000.0, loadedVip.calculateDiscount(150000.0), "Reloaded VIP discount calculation");

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
