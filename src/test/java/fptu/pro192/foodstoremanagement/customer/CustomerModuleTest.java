package fptu.pro192.foodstoremanagement.customer;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.CustomerFactory;
import fptu.pro192.foodstoremanagement.model.RegularCustomer;
import fptu.pro192.foodstoremanagement.model.VIPCustomer;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.util.InputValidator;

import java.io.File;
import java.util.List;

/**
 * Verification test suite for Task #3 (Customer Domain Models, Normalization, & Repository). Pure
 * Java 8, zero third-party dependencies.
 */
public class CustomerModuleTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   RUNNING TASK #3 CUSTOMER MODULE VERIFICATION TESTS           ");
        System.out.println("================================================================");

        testPhoneNormalizationValid();
        testPhoneNormalizationInvalid();
        testPolymorphicDiscountStrategy();
        testCustomerFactory();
        testRepositoryPersistenceAndCrud();

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
            System.err.printf("  [FAIL] %s: Expected [%s] but got [%s]\n", testName, expected,
                    actual);
            totalFailed++;
        }
    }

    private static void testPhoneNormalizationValid() {
        System.out.println("\n--- 1. Testing Vietnamese Phone Normalization (Valid Formats) ---");
        assertEquals("normalizePhone: +84372240629", "0372240629",
                InputValidator.normalizePhone("+84372240629"));
        assertEquals("normalizePhone: (+84) 37 224 0629", "0372240629",
                InputValidator.normalizePhone("(+84) 37 224 0629"));
        assertEquals("normalizePhone: 037.224.0629", "0372240629",
                InputValidator.normalizePhone("037.224.0629"));
        assertEquals("normalizePhone: +840372240629", "0372240629",
                InputValidator.normalizePhone("+840372240629"));
        assertEquals("normalizePhone: 840372240629", "0372240629",
                InputValidator.normalizePhone("840372240629"));
        assertEquals("normalizePhone: 0901234567", "0901234567",
                InputValidator.normalizePhone("0901234567"));
        assertEquals("normalPhoneNumbers alias backward compatibility", "0372240629",
                InputValidator.normalPhoneNumbers("+84372240629"));
    }

    private static void testPhoneNormalizationInvalid() {
        System.out.println("\n--- 2. Testing Vietnamese Phone Normalization (Invalid Formats) ---");
        boolean threwNull = false;
        try {
            InputValidator.normalizePhone(null);
        } catch (IllegalArgumentException e) {
            threwNull = true;
        }
        assertTrue("normalizePhone: null throws IllegalArgumentException", threwNull);

        boolean threwEmpty = false;
        try {
            InputValidator.normalizePhone("   ");
        } catch (IllegalArgumentException e) {
            threwEmpty = true;
        }
        assertTrue("normalizePhone: empty string throws IllegalArgumentException", threwEmpty);

        boolean threwShort = false;
        try {
            InputValidator.normalizePhone("12345");
        } catch (IllegalArgumentException e) {
            threwShort = true;
        }
        assertTrue("normalizePhone: 12345 throws IllegalArgumentException", threwShort);

        boolean threwInvalidPrefix = false;
        try {
            InputValidator.normalizePhone("01234567890");
        } catch (IllegalArgumentException e) {
            threwInvalidPrefix = true;
        }
        assertTrue("normalizePhone: 01234567890 throws IllegalArgumentException",
                threwInvalidPrefix);
    }

    private static void testPolymorphicDiscountStrategy() {
        System.out.println("\n--- 3. Testing Strategy Pattern Discount Calculations ---");
        Customer vip = new VIPCustomer("C00001", "Nguyen Van An", "0901234567", "Ho Chi Minh");
        Customer regular =
                new RegularCustomer("C00002", "Tran Thi Hoa", "0987654321", "Binh Duong");

        assertEquals("VIP discount rate is 10%", 0.10, vip.getDiscountRate());
        assertEquals("VIP discount on 100,000 subtotal is 10,000", 10000.0,
                vip.calculateDiscount(100000.0));

        assertEquals("Regular discount rate is 0%", 0.0, regular.getDiscountRate());
        assertEquals("Regular discount on 100,000 subtotal is 0", 0.0,
                regular.calculateDiscount(100000.0));
    }

    private static void testCustomerFactory() {
        System.out.println("\n--- 4. Testing Customer Factory ---");
        Customer c1 =
                CustomerFactory.create("VIP", "C00001", "Le Van VIP", "0901234567", "HCM", false);
        Customer c2 = CustomerFactory.create("regular", "C00002", "Tran Van Reg", "0987654321",
                "HN", false);

        assertTrue("Factory creates VIPCustomer when type is 'VIP'", c1 instanceof VIPCustomer);
        assertTrue("Factory creates RegularCustomer when type is 'regular'",
                c2 instanceof RegularCustomer);
    }

    private static void testRepositoryPersistenceAndCrud() {
        System.out.println("\n--- 5. Testing CustomerRepository Persistence and CRUD ---");
        String testFilePath = "data/test_customers.csv";
        File testFile = new File(testFilePath);
        if (testFile.exists()) {
            testFile.delete();
        }

        try {
            CustomerRepository repo = new CustomerRepository(testFilePath);

            Customer vip = new VIPCustomer("C00001", "Nguyen Van An", "0901234567", "Ho Chi Minh");
            Customer regular =
                    new RegularCustomer("C00002", "Tran Thi Hoa", "0372240629", "Binh Duong");

            repo.save(vip);
            repo.save(regular);

            assertEquals("findAll returns 2 active customers", 2, repo.findAll().size());
            assertEquals("findById('C00001') finds VIP customer", "Nguyen Van An",
                    repo.findById("C00001").getFullName());
            assertEquals("findById('c00001') case-insensitive lookup", "Nguyen Van An",
                    repo.findById("c00001").getFullName());

            Customer byPhone = repo.findByPhone("(+84) 37 224 0629");
            assertTrue("findByPhone matches formatted phone",
                    byPhone != null && byPhone.getId().equals("C00002"));

            // Test Soft Delete
            repo.delete("C00001");
            assertEquals("findAll returns 1 active customer after delete", 1,
                    repo.findAll().size());
            assertTrue("findById('C00001') returns null for soft-deleted customer",
                    repo.findById("C00001") == null);

            // Test CSV Persistence Reload
            CustomerRepository reloadedRepo = new CustomerRepository(testFilePath);
            assertEquals("Reloaded repository has 1 active customer", 1,
                    reloadedRepo.findAll().size());
            assertEquals("Reloaded active customer is C00002", "C00002",
                    reloadedRepo.findAll().get(0).getId());

        } finally {
            if (testFile.exists()) {
                testFile.delete();
            }
            File tmpFile = new File(testFilePath + ".tmp");
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }
    }
}
