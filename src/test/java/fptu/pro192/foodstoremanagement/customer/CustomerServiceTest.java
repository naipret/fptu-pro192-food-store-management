package fptu.pro192.foodstoremanagement.customer;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.VIPCustomer;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.service.CustomerService;

import java.io.File;
import java.util.List;

/**
 * Verification test suite for Task #6 (CustomerService).
 * Pure Java 8, zero third-party dependencies.
 */
public class CustomerServiceTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   RUNNING TASK #6 CUSTOMER SERVICE VERIFICATION TESTS          ");
        System.out.println("================================================================");

        testAddCustomerValidation();
        testPhoneNormalizationAndUniqueness();
        testSelectiveUpdate();
        testSoftDelete();
        testSearch();

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

    private static void testAddCustomerValidation() {
        System.out.println("\n--- 1. Testing Add Customer Validations (BR2, BR18) ---");
        String testPath = "data/test_service_customers.csv";
        cleanFile(testPath);

        try {
            CustomerRepository repo = new CustomerRepository(testPath);
            CustomerService service = new CustomerService(repo);

            // Valid add
            service.addCustomer("C00001", "Nguyen Van An", "0901234567", "Ho Chi Minh", "VIP");
            assertEquals("Customer added successfully", 1, service.getAllActive().size());
            Customer c = service.getAllActive().get(0);
            assertEquals("ID correctly set", "C00001", c.getId());
            assertEquals("FullName correctly set", "Nguyen Van An", c.getFullName());
            assertEquals("Phone correctly set", "0901234567", c.getPhone());
            assertEquals("Address correctly set", "Ho Chi Minh", c.getAddress());
            assertTrue("Type correctly set to VIP", c instanceof VIPCustomer);

            // Invalid ID regex
            boolean threwInvalidId = false;
            try {
                service.addCustomer("C123", "Invalid ID", "0901234568", "Hanoi", "Regular");
            } catch (IllegalArgumentException e) {
                threwInvalidId = true;
            }
            assertTrue("Rejects invalid ID regex (not Cxxxxx)", threwInvalidId);

            // Duplicate ID rejection (BR2)
            boolean threwDupId = false;
            try {
                service.addCustomer("C00001", "Duplicate ID", "0901234569", "Hanoi", "Regular");
            } catch (IllegalArgumentException e) {
                threwDupId = true;
            }
            assertTrue("Rejects duplicate customer ID (BR2)", threwDupId);

        } finally {
            cleanFile(testPath);
        }
    }

    private static void testPhoneNormalizationAndUniqueness() {
        System.out.println("\n--- 2. Testing Phone Normalization and Uniqueness (BR25) ---");
        String testPath = "data/test_service_customers.csv";
        cleanFile(testPath);

        try {
            CustomerRepository repo = new CustomerRepository(testPath);
            CustomerService service = new CustomerService(repo);

            // Add with formatted phone: (+84) 37 224 0629 -> normalized to 0372240629
            service.addCustomer("C00001", "Tran Thi Hoa", "(+84) 37 224 0629", "Da Nang", "Regular");
            assertEquals("Phone normalized to 0372240629", "0372240629",
                    service.getAllActive().get(0).getPhone());

            // Duplicate phone rejection (different format, same normalized number)
            boolean threwDupPhone = false;
            try {
                service.addCustomer("C00002", "Le Van B", "037.224.0629", "Hue", "VIP");
            } catch (IllegalArgumentException e) {
                threwDupPhone = true;
            }
            assertTrue("Rejects duplicate phone number in alternate format (BR25)", threwDupPhone);

        } finally {
            cleanFile(testPath);
        }
    }

    private static void testSelectiveUpdate() {
        System.out.println("\n--- 3. Testing Selective Update (Task B6) ---");
        String testPath = "data/test_service_customers.csv";
        cleanFile(testPath);

        try {
            CustomerRepository repo = new CustomerRepository(testPath);
            CustomerService service = new CustomerService(repo);

            service.addCustomer("C00001", "Nguyen Van An", "0901234567", "District 1", "Regular");
            service.addCustomer("C00002", "Le Thi B", "0987654321", "District 2", "VIP");

            // Selective update: only update address, blank phone preserves old phone
            service.updateCustomer("C00001", "   ", "District 7");
            Customer updated = repo.findById("C00001");
            assertEquals("Phone retained when blank", "0901234567", updated.getPhone());
            assertEquals("Address updated", "District 7", updated.getAddress());

            // Update phone with formatted string
            service.updateCustomer("C00001", "+84 901 234 567", "");
            assertEquals("Phone updated and normalized", "0901234567", repo.findById("C00001").getPhone());

            // Duplicate phone rejection on update
            boolean threwDupPhone = false;
            try {
                service.updateCustomer("C00001", "0987654321", "");
            } catch (IllegalArgumentException e) {
                threwDupPhone = true;
            }
            assertTrue("Rejects update to a phone number already used by C00002", threwDupPhone);

            // Update non-existent customer throws exception
            boolean threwNotFound = false;
            try {
                service.updateCustomer("C99999", "0912345678", "Nowhere");
            } catch (IllegalArgumentException e) {
                threwNotFound = true;
            }
            assertTrue("Updating non-existent ID throws IllegalArgumentException", threwNotFound);

        } finally {
            cleanFile(testPath);
        }
    }

    private static void testSoftDelete() {
        System.out.println("\n--- 4. Testing Soft Delete (BR27) ---");
        String testPath = "data/test_service_customers.csv";
        cleanFile(testPath);

        try {
            CustomerRepository repo = new CustomerRepository(testPath);
            CustomerService service = new CustomerService(repo);

            service.addCustomer("C00001", "Customer One", "0901234567", "Address 1", "Regular");
            service.addCustomer("C00002", "Customer Two", "0987654321", "Address 2", "VIP");

            assertEquals("Initially 2 active customers", 2, service.getAllActive().size());

            service.deleteCustomer("C00001");
            assertEquals("1 active customer remaining after soft delete", 1, service.getAllActive().size());
            assertEquals("Active customer is C00002", "C00002", service.getAllActive().get(0).getId());

            // Deleting non-existent ID throws exception
            boolean threwNotFound = false;
            try {
                service.deleteCustomer("C99999");
            } catch (IllegalArgumentException e) {
                threwNotFound = true;
            }
            assertTrue("Deleting non-existent ID throws IllegalArgumentException", threwNotFound);

            // Reload from CSV: soft deleted customer remains deleted
            CustomerRepository reloadedRepo = new CustomerRepository(testPath);
            CustomerService reloadedService = new CustomerService(reloadedRepo);
            assertEquals("Persisted CSV maintains 1 active customer", 1, reloadedService.getAllActive().size());

        } finally {
            cleanFile(testPath);
        }
    }

    private static void testSearch() {
        System.out.println("\n--- 5. Testing Search Functionality ---");
        String testPath = "data/test_service_customers.csv";
        cleanFile(testPath);

        try {
            CustomerRepository repo = new CustomerRepository(testPath);
            CustomerService service = new CustomerService(repo);

            service.addCustomer("C00001", "Nguyen Van An", "0901234567", "HCM", "VIP");
            service.addCustomer("C00002", "Tran Thi Hoa", "0372240629", "HN", "Regular");
            service.addCustomer("C00003", "Le Van An", "0911223344", "DN", "Regular");

            // Search by name substring
            List<Customer> nameMatches = service.search("Van");
            assertEquals("Search 'Van' matches 2 customers (Nguyen Van An, Le Van An)", 2, nameMatches.size());

            // Search by phone substring
            List<Customer> phoneMatches = service.search("2240629");
            assertEquals("Search '2240629' matches 1 customer", 1, phoneMatches.size());
            assertEquals("Matched customer is C00002", "C00002", phoneMatches.get(0).getId());

            // Null or blank keyword returns all active customers
            assertEquals("Search with null keyword returns all active", 3, service.search(null).size());
            assertEquals("Search with empty keyword returns all active", 3, service.search("   ").size());

            // No matches
            assertEquals("Search 'xyz' returns empty list", 0, service.search("xyz").size());

        } finally {
            cleanFile(testPath);
        }
    }

    private static void cleanFile(String path) {
        File f = new File(path);
        if (f.exists()) {
            f.delete();
        }
        File tmp = new File(path + ".tmp");
        if (tmp.exists()) {
            tmp.delete();
        }
    }
}
