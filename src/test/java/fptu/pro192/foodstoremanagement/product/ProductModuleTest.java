package fptu.pro192.foodstoremanagement.product;

import fptu.pro192.foodstoremanagement.model.*;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Verification test suite for Task #2 (Food Domain Models, Batch Aggregation, & ProductRepository).
 * Pure Java 8, zero third-party dependencies.
 */
public class ProductModuleTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   RUNNING TASK #2 PRODUCT MODULE VERIFICATION TESTS            ");
        System.out.println("================================================================");

        testConstructorInvariants();
        testPolymorphismAndHACCP();
        testBatchExpiryAndDeduction();
        testProductFactory();
        testFoodStockCalculationsAndFEFO();
        testProductRepositoryPersistenceAndAtomicSync();

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

    private static void testConstructorInvariants() {
        System.out.println(
                "\n--- 1. Testing Domain Invariants Validation (BR1, BR3, BR4, BR5, BR5.1, BR7, BR26) ---");

        // Valid product creation
        try {
            Food food = new DryFood("P00001", "Mi Hao Hao Tom Chua Cay", "DRY", "Goi", 8000.0);
            assertTrue("Valid product instantiated successfully", food != null);
        } catch (Exception e) {
            assertTrue("Valid product instantiation failed unexpectedly", false);
        }

        // BR1: Invalid product ID format
        assertThrows("Invalid ID 'P123' throws IllegalArgumentException", () -> {
            new DryFood("P123", "Mi Hao Hao", "DRY", "Goi", 8000.0);
        });
        assertThrows("Invalid ID 'C00001' (customer prefix) throws IllegalArgumentException",
                () -> {
                    new DryFood("C00001", "Mi Hao Hao", "DRY", "Goi", 8000.0);
                });

        // BR3: Empty name or category
        assertThrows("Empty name throws IllegalArgumentException", () -> {
            new DryFood("P00001", "   ", "DRY", "Goi", 8000.0);
        });
        assertThrows("Empty category throws IllegalArgumentException", () -> {
            new DryFood("P00001", "Mi Hao Hao", "   ", "Goi", 8000.0);
        });

        // BR4: Empty unit
        assertThrows("Empty unit throws IllegalArgumentException", () -> {
            new DryFood("P00001", "Mi Hao Hao", "DRY", "   ", 8000.0);
        });

        // BR5: Non-positive price
        assertThrows("Zero price throws IllegalArgumentException", () -> {
            new DryFood("P00001", "Mi Hao Hao", "DRY", "Goi", 0.0);
        });
        assertThrows("Negative price throws IllegalArgumentException", () -> {
            new DryFood("P00001", "Mi Hao Hao", "DRY", "Goi", -5000.0);
        });

        // BR5.1: Name cannot be purely numeric
        assertThrows("Numeric name '123456' throws IllegalArgumentException", () -> {
            new DryFood("P00001", "123456", "DRY", "Goi", 8000.0);
        });

        // BR7: Batch production date > expiration date
        assertThrows("Batch production date after expiry date throws IllegalArgumentException",
                () -> {
                    new Batch("B00001", "P00001", LocalDate.now(), LocalDate.now(),
                            LocalDate.now().minusDays(5), 10);
                });

        // BR26: Batch negative quantity
        assertThrows("Batch negative quantity throws IllegalArgumentException", () -> {
            new Batch("B00001", "P00001", LocalDate.now(), LocalDate.now(),
                    LocalDate.now().plusDays(10), -5);
        });
    }

    private static void testPolymorphismAndHACCP() {
        System.out
                .println("\n--- 2. Testing Polymorphism & HACCP Shelf-Life Warning Thresholds ---");

        Food frozen = new FrozenFood("P00001", "Ca Hoi Phi Le", "FROZEN", "Kg", 250000.0);
        Food chilled = new ChilledFood("P00002", "Sua Tuoi Thanh Trung", "CHILLED", "Hop", 35000.0);
        Food dry = new DryFood("P00003", "Gao ST25", "DRY", "Kg", 40000.0);

        assertEquals("FrozenFood warning days is 14", 14, frozen.getDaysBeforeExpiryWarning());
        assertEquals("ChilledFood warning days is 1 (BR20.1)", 1,
                chilled.getDaysBeforeExpiryWarning());
        assertEquals("DryFood warning days is 7 (BR20.2)", 7, dry.getDaysBeforeExpiryWarning());

        assertTrue("FrozenFood storage instructions mention frozen",
                frozen.getStorageInstructions().contains("frozen"));
        assertTrue("ChilledFood storage instructions mention refrigerated",
                chilled.getStorageInstructions().contains("refrigerated"));
        assertTrue("DryFood storage instructions mention cool, dry place",
                dry.getStorageInstructions().contains("cool, dry place"));
    }

    private static void testBatchExpiryAndDeduction() {
        System.out.println("\n--- 3. Testing Batch Expiration Logic & Stock Deduction ---");

        LocalDate today = LocalDate.of(2026, 10, 8);
        LocalDate pastDate = LocalDate.of(2026, 10, 5);
        LocalDate nearDate = LocalDate.of(2026, 10, 12);
        LocalDate farDate = LocalDate.of(2026, 11, 8);

        Batch expiredBatch = new Batch("B00001", "P00001", pastDate.minusDays(10),
                pastDate.minusDays(10), pastDate, 50);
        Batch nearBatch =
                new Batch("B00002", "P00001", today.minusDays(5), today.minusDays(5), nearDate, 30);
        Batch farBatch = new Batch("B00003", "P00001", today, today, farDate, 100);

        // BR11: isExpired
        assertTrue("Past batch is expired relative to today", expiredBatch.isExpired(today));
        assertTrue("Near batch is not expired relative to today", !nearBatch.isExpired(today));
        assertTrue("Far batch is not expired relative to today", !farBatch.isExpired(today));

        // BR20: isCloseToExpiry
        assertTrue("Expired batch is NOT close to expiry (already expired)",
                !expiredBatch.isCloseToExpiry(today, 7));
        assertTrue("Near batch (4 days remaining) IS close to expiry within 7 days",
                nearBatch.isCloseToExpiry(today, 7));
        assertTrue("Far batch (31 days remaining) is NOT close to expiry within 7 days",
                !farBatch.isCloseToExpiry(today, 7));

        // BR14, BR24: Deduct
        farBatch.deduct(25);
        assertEquals("Batch quantity after deducting 25 is 75", 75, farBatch.getQuantity());

        assertThrows("Deducting more than available throws IllegalArgumentException", () -> {
            farBatch.deduct(80);
        });
        assertThrows("Deducting zero or negative throws IllegalArgumentException", () -> {
            farBatch.deduct(0);
        });
    }

    private static void testProductFactory() {
        System.out.println("\n--- 4. Testing ProductFactory Factory Method Pattern ---");

        Food frozen = ProductFactory.create("FROZEN", "P00001", "Thit Bo Uc Dong Lanh", "FROZEN",
                "Kg", 180000.0, -25.0, -18.0, 85.0, 95.0, false);
        Food chilled = ProductFactory.create("CHILLED", "P00002", "Sua Chua Co Duong", "CHILLED",
                "Hop", 7000.0, 0.0, 4.0, 75.0, 85.0, false);
        Food dry = ProductFactory.create("DRY", "P00003", "Nuoc Mam Nam Ngu", "DRY", "Chai",
                32000.0, 15.0, 25.0, 30.0, 60.0, false);

        assertTrue("Factory produces FrozenFood", frozen instanceof FrozenFood);
        assertTrue("Factory produces ChilledFood", chilled instanceof ChilledFood);
        assertTrue("Factory produces DryFood", dry instanceof DryFood);

        assertThrows("Unknown type 'CANNED' throws IllegalArgumentException", () -> {
            ProductFactory.create("CANNED", "P00004", "Ca Hop", "CANNED", "Hop", 20000.0, 0.0, 0.0,
                    0.0, 0.0, false);
        });
    }

    private static void testFoodStockCalculationsAndFEFO() {
        System.out.println("\n--- 5. Testing Food Active Stock & FEFO Batch Sorting ---");

        LocalDate today = LocalDate.of(2026, 10, 8);
        Food food = new DryFood("P00001", "Mi Hao Hao", "DRY", "Goi", 8000.0);

        // Batches with different expiry dates:
        Batch bExpired = new Batch("B00001", "P00001", today.minusDays(20), today.minusDays(20),
                today.minusDays(1), 10);
        Batch bLate = new Batch("B00002", "P00001", today.minusDays(5), today.minusDays(5),
                today.plusDays(30), 40);
        Batch bEarly = new Batch("B00003", "P00001", today.minusDays(10), today.minusDays(10),
                today.plusDays(10), 20);

        food.addBatch(bExpired);
        food.addBatch(bLate);
        food.addBatch(bEarly);

        // Active total stock excludes expired batch (40 + 20 = 60)
        assertEquals("Total stock excludes expired batch", 60, food.getTotalStock(today));

        // FEFO active batches sorting: bEarly (10 days) before bLate (30 days)
        List<Batch> activeBatches = food.getActiveBatches(today);
        assertEquals("Active batches count is 2", 2, activeBatches.size());
        assertEquals("First batch in FEFO order is earliest expiring (B00003)", "B00003",
                activeBatches.get(0).getBatchId());
        assertEquals("Second batch in FEFO order is later expiring (B00002)", "B00002",
                activeBatches.get(1).getBatchId());
    }

    private static void testProductRepositoryPersistenceAndAtomicSync() {
        System.out.println("\n--- 6. Testing ProductRepository Persistence, Reload, and CRUD ---");

        String pPath = "test_products.csv";
        String bPath = "test_batches.csv";
        new File(pPath).delete();
        new File(bPath).delete();

        try {
            ProductRepository repo = new ProductRepository(pPath, bPath);

            Food f1 = new DryFood("P00001", "Mi Hao Hao", "DRY", "Goi", 8000.0);
            f1.addBatch(new Batch("B00001", "P00001", LocalDate.now(), LocalDate.now(),
                    LocalDate.now().plusDays(60), 100));
            f1.addBatch(new Batch("B00002", "P00001", LocalDate.now(), LocalDate.now(),
                    LocalDate.now().plusDays(90), 50));

            Food f2 = new ChilledFood("P00002", "Sua Tuoi 1L", "CHILLED", "Hop", 35000.0);
            f2.addBatch(new Batch("B00003", "P00002", LocalDate.now(), LocalDate.now(),
                    LocalDate.now().plusDays(10), 30));

            repo.save(f1);
            repo.save(f2);

            assertEquals("Repo contains 2 products", 2, repo.findAll().size());

            // Reload repository from CSV to verify two-phase persistence & batch linkage
            ProductRepository reloaded = new ProductRepository(pPath, bPath);
            assertEquals("Reloaded repo has 2 active products", 2, reloaded.findAll().size());

            Food reloadedF1 = reloaded.findById("P00001");
            assertTrue("Reloaded P00001 is found", reloadedF1 != null);
            assertEquals("Reloaded P00001 has 2 attached batches", 2,
                    reloadedF1.getBatches().size());
            assertEquals("Reloaded P00001 total stock is 150", 150, reloadedF1.getTotalStock());

            Food reloadedF2 = reloaded.findById("p00002"); // case-insensitive check
            assertTrue("Reloaded p00002 found with case-insensitivity", reloadedF2 != null);
            assertEquals("Reloaded P00002 has 1 attached batch", 1, reloadedF2.getBatches().size());

            // Soft-delete test (BR27)
            reloaded.delete("P00001");
            assertEquals("After delete, findAll returns 1 active product", 1,
                    reloaded.findAll().size());
            assertTrue("findById('P00001') returns null after soft delete",
                    reloaded.findById("P00001") == null);

            // Re-read from disk to ensure soft delete persisted
            ProductRepository reloadedAfterDelete = new ProductRepository(pPath, bPath);
            assertEquals("Reloaded after delete persists soft delete state", 1,
                    reloadedAfterDelete.findAll().size());

        } finally {
            new File(pPath).delete();
            new File(bPath).delete();
            new File(pPath + ".tmp").delete();
            new File(bPath + ".tmp").delete();
        }
    }

    private static void assertThrows(String testName, Runnable runnable) {
        try {
            runnable.run();
            System.err.printf("  [FAIL] %s: Expected exception but none was thrown\n", testName);
            totalFailed++;
        } catch (IllegalArgumentException e) {
            System.out.printf("  [PASS] %s\n", testName);
            totalPassed++;
        } catch (Exception e) {
            System.err.printf("  [FAIL] %s: Expected IllegalArgumentException but got %s\n",
                    testName, e.getClass().getSimpleName());
            totalFailed++;
        }
    }
}
