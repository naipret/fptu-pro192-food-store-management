package fptu.pro192.foodstoremanagement.product;

import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import fptu.pro192.foodstoremanagement.view.InventoryMenu;
import fptu.pro192.foodstoremanagement.view.ProductMenu;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated verification test suite for ProductController operations, search
 * queries, batch replenishment, inventory alerts, ProductMenu, and
 * InventoryMenu
 * (BR1, BR3, BR4, BR5, BR6, BR7, BR10, BR11, BR19, BR20, BR24, BR26, BR27).
 */
public class ProductServiceTest {

    private ProductServiceTest() {
    }

    /**
     * Executes all product controller and inventory view verification tests.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if an unexpected I/O error occurs
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Running ProductServiceTest...");
        testUpdateDeleteAndSearchQueries();
        testBatchReplenishAdjustAndAlerts();
        testProductMenuAndInventoryMenuFlows();
        System.out.println("ProductServiceTest PASSED (All assertions succeeded).");
    }

    private static void testUpdateDeleteAndSearchQueries() throws Exception {
        Path tempDir = Files.createTempDirectory("product_service_test_");
        Path productsCsv = tempDir.resolve("products.csv");
        Path batchesCsv = tempDir.resolve("batches.csv");

        ProductController controller = new ProductController(productsCsv, batchesCsv);
        LocalDate today = LocalDate.now();

        controller.addProduct(
                new Product("P00001", "Hao Hao Instant Noodles", "Noodles", "Pack", 4500.0),
                new Batch("B00001", "P00001", today, today.minusDays(5), today.plusDays(60), 50));
        controller.addProduct(
                new Product("P00002", "Australian Beef Sirloin", "Meat", "Tray", 145000.0),
                new Batch("B00002", "P00002", today, today.minusDays(2), today.plusDays(4), 3));

        controller.updateProduct("P00001", "Hao Hao Sour Spicy Noodles", null, null, 5000.0);
        Product updated = controller.findById("P00001");
        assertEquals("Hao Hao Sour Spicy Noodles", updated.getName(), "Updated product name");
        assertEquals(5000.0, updated.getPrice(), "Updated product price");

        List<Product> meatMatches = controller.searchByCategory("Meat");
        assertEquals(1, meatMatches.size(), "Category search for Meat");

        List<Product> expMatches = controller.searchByExpirationDate(today.plusDays(10));
        assertEquals(1, expMatches.size(), "Products with batch expiring within 10 days");
        assertEquals("P00002", expMatches.get(0).getId(), "P00002 expires within 4 days");

        assertTrue(controller.deleteProduct("P00002"), "Soft delete P00002");
        assertTrue(controller.findById("P00002") == null, "Soft-deleted product hidden from active lookup");
        assertTrue(controller.findByIdIncludeDeleted("P00002") != null, "Soft-deleted product retained in storage");

        Files.deleteIfExists(productsCsv);
        Files.deleteIfExists(batchesCsv);
        Files.deleteIfExists(tempDir);
    }

    private static void testBatchReplenishAdjustAndAlerts() throws Exception {
        Path tempDir = Files.createTempDirectory("product_alerts_test_");
        Path productsCsv = tempDir.resolve("products.csv");
        Path batchesCsv = tempDir.resolve("batches.csv");

        ProductController controller = new ProductController(productsCsv, batchesCsv);
        LocalDate today = LocalDate.now();

        controller.addProduct(
                new Product("P00001", "Dalat Fresh Lettuce", "Produce", "Bag", 22000.0),
                new Batch("B00001", "P00001", today, today.minusDays(2), today.plusDays(3), 4));
        controller.addBatchToProduct("P00001",
                new Batch("B00002", "P00001", today.minusDays(15), today.minusDays(20), today.minusDays(1), 6));

        List<Product> lowStock = controller.getLowStockProducts(5);
        assertEquals(1, lowStock.size(), "P00001 has 4 available units (<= 5)");

        List<Batch> nearExpiry = controller.getNearExpiryBatches(7);
        assertEquals(1, nearExpiry.size(), "B00001 expires in 3 days");

        List<Batch> expired = controller.getExpiredBatches();
        assertEquals(1, expired.size(), "B00002 expired yesterday");

        controller.replenishBatch("P00001", "B00001", 10);
        assertEquals(14, controller.findById("P00001").getAvailableStock(), "Stock after replenishing +10");

        controller.adjustBatchQuantity("P00001", "B00001", 8);
        assertEquals(8, controller.findById("P00001").getAvailableStock(), "Stock after adjusting to 8");

        Files.deleteIfExists(productsCsv);
        Files.deleteIfExists(batchesCsv);
        Files.deleteIfExists(tempDir);
    }

    private static void testProductMenuAndInventoryMenuFlows() throws Exception {
        Path tempDir = Files.createTempDirectory("product_menu_test_");
        Path productsCsv = tempDir.resolve("products.csv");
        Path batchesCsv = tempDir.resolve("batches.csv");

        ProductController controller = new ProductController(productsCsv, batchesCsv);
        ProductMenu productMenu = new ProductMenu(controller);
        InventoryMenu inventoryMenu = new InventoryMenu(controller);

        String productMenuInput = "3\n\nTH True Milk 1L\n6\n10\n38000\n1\n\n0\n";
        ScannerManager.getInstance().setInputStream(
                new ByteArrayInputStream(productMenuInput.getBytes(StandardCharsets.UTF_8)));
        productMenu.display();

        assertEquals(1, controller.getAllProducts().size(), "Product added via ProductMenu");
        assertEquals("TH True Milk 1L", controller.findById("P00001").getName(), "Product name from ProductMenu");

        String inventoryMenuInput = "1\nP00001\n\n25\n01/10/2026\n31/12/2026\n\n0\n";
        ScannerManager.getInstance().setInputStream(
                new ByteArrayInputStream(inventoryMenuInput.getBytes(StandardCharsets.UTF_8)));
        inventoryMenu.display();

        assertEquals(25, controller.findById("P00001").getTotalStock(), "Batch imported via InventoryMenu");
        ScannerManager.getInstance().resetToStandardInput();

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
}
