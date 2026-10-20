package fptu.pro192.foodstoremanagement.product;

import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Product;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated verification test suite for the Product and Batch domain models
 * and ProductController CSV persistence (BR1, BR3, BR4, BR5, BR6, BR7, BR11,
 * BR18, BR19, BR20, BR24, BR26).
 */
public class ProductModuleTest {

    private ProductModuleTest() {
    }

    /**
     * Executes all product and batch domain and persistence verification tests.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if an unexpected I/O error occurs
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Running ProductModuleTest...");
        testBatchExpirationAndNearExpiryRules();
        testProductFefoAndAvailableStockCalculation();
        testProductControllerCsvPersistenceAndCategories();
        System.out.println("ProductModuleTest PASSED (All assertions succeeded).");
    }

    private static void testBatchExpirationAndNearExpiryRules() {
        LocalDate now = LocalDate.now();
        Batch activeBatch = new Batch("B00001", "P00001",
                now,
                now.minusDays(15),
                now.plusDays(60),
                20);
        assertTrue(!activeBatch.isExpired(), "Batch expiring in 60 days is not expired");
        assertTrue(!activeBatch.isNearExpiry(7), "Batch expiring in 60 days is not near expiry");

        Batch nearBatch = new Batch("B00002", "P00001",
                now,
                now.minusDays(10),
                now.plusDays(3),
                10);
        assertTrue(!nearBatch.isExpired(), "Batch expiring in 3 days is not expired");
        assertTrue(nearBatch.isNearExpiry(7), "Batch expiring in 3 days is near expiry (BR20)");

        Batch expiredBatch = new Batch("B00003", "P00001",
                now.minusDays(20),
                now.minusDays(30),
                now.minusDays(1),
                15);
        assertTrue(expiredBatch.isExpired(), "Batch expiring yesterday is expired (BR11)");
        assertTrue(!expiredBatch.isNearExpiry(7), "Expired batch is not near-expiry");

        assertThrows(new Runnable() {
            @Override
            public void run() {
                new Batch("B00004", "P00001",
                        now,
                        now.plusDays(10),
                        now.plusDays(5),
                        5);
            }
        }, "Production date after expiration date must be rejected (BR7)");
    }

    private static void testProductFefoAndAvailableStockCalculation() {
        LocalDate now = LocalDate.now();
        Product milk = new Product("P00001", "Vinamilk Fresh Milk 1L", "Dairy", "Carton", 36000.0);

        Batch expiredBatch = new Batch("B00001", "P00001",
                now.minusDays(20),
                now.minusDays(25),
                now.minusDays(5),
                10);
        Batch laterBatch = new Batch("B00002", "P00001",
                now,
                now.minusDays(5),
                now.plusDays(30),
                15);
        Batch earlierBatch = new Batch("B00003", "P00001",
                now,
                now.minusDays(2),
                now.plusDays(10),
                8);

        milk.addBatch(expiredBatch);
        milk.addBatch(laterBatch);
        milk.addBatch(earlierBatch);

        assertEquals(33, milk.getTotalStock(), "Total physical stock includes expired batch");
        assertEquals(23, milk.getAvailableStock(), "Available stock excludes expired batch (BR11)");

        List<Batch> fefoOrder = milk.getActiveBatches();
        assertEquals(2, fefoOrder.size(), "Only 2 non-expired batches returned by getActiveBatches");
        assertEquals("B00003", fefoOrder.get(0).getBatchId(),
                "Earlier expiring batch B00003 placed first via FEFO (BR24)");
        assertEquals("B00002", fefoOrder.get(1).getBatchId(), "Later expiring batch B00002 placed second via FEFO");
    }

    private static void testProductControllerCsvPersistenceAndCategories() throws Exception {
        Path tempDir = Files.createTempDirectory("product_module_test_");
        Path productsCsv = tempDir.resolve("products.csv");
        Path batchesCsv = tempDir.resolve("batches.csv");

        ProductController controller = new ProductController(productsCsv, batchesCsv);
        assertTrue(controller.getCategories().contains("Grains"), "Default Set contains Grains (BR5)");
        assertTrue(controller.getUnits().contains("Bag"), "Default Set contains Bag (BR5)");

        String customCat = controller.registerCategory("Organic");
        assertEquals("Organic", customCat, "Registered custom single-word category");
        assertTrue(controller.getCategories().contains("Organic"), "Custom category added to Set");

        Product rice = new Product("P00001", "ST25 Fragrant Rice 5kg", "Grains", "Bag", 165000.0);
        Batch batch1 = new Batch("B00001", "P00001",
                LocalDate.now(),
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(120),
                30);
        controller.addProduct(rice);
        controller.addBatchToProduct("P00001", batch1);

        List<String> productLines = Files.readAllLines(productsCsv);
        List<String> batchLines = Files.readAllLines(batchesCsv);
        assertEquals("id,name,category,unit,price,isDeleted", productLines.get(0), "products.csv header at line 0");
        assertEquals("batchId,productId,importDate,productionDate,expirationDate,quantity",
                batchLines.get(0), "batches.csv header at line 0");

        ProductController reloaded = new ProductController(productsCsv, batchesCsv);
        Product loadedRice = reloaded.findById("P1");
        assertTrue(loadedRice != null, "Reloaded product found via shorthand P1");
        assertEquals("ST25 Fragrant Rice 5kg", loadedRice.getName(), "Reloaded product name");
        assertEquals(30, loadedRice.getAvailableStock(), "Reloaded batch stock joined to product");

        Files.deleteIfExists(productsCsv);
        Files.deleteIfExists(batchesCsv);
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
