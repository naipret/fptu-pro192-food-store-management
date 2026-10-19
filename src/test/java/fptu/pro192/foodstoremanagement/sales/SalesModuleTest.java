package fptu.pro192.foodstoremanagement.sales;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.controller.OrderController;
import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import fptu.pro192.foodstoremanagement.view.SalesMenu;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Automated verification test suite for SalesMenu interactive checkout
 * workflows: VIP customer checkout, walk-in guest checkout, draft cart
 * cancellation, and invoice inspection (BR8, BR9, BR10, BR11, BR12, BR13,
 * BR14, BR15, BR16, BR17, BR18, BR24, BR26).
 */
public class SalesModuleTest {

    private SalesModuleTest() {
    }

    /**
     * Executes all sales menu and checkout workflow verification tests.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if an unexpected error occurs
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Running SalesModuleTest...");
        testInteractiveVipAndGuestCheckoutAndCancelFlows();
        System.out.println("SalesModuleTest PASSED (All assertions succeeded).");
    }

    private static void testInteractiveVipAndGuestCheckoutAndCancelFlows() throws Exception {
        Path tempDir = Files.createTempDirectory("sales_module_test_");
        Path productsCsv = tempDir.resolve("products.csv");
        Path batchesCsv = tempDir.resolve("batches.csv");
        Path customersCsv = tempDir.resolve("customers.csv");
        Path ordersCsv = tempDir.resolve("orders.csv");
        Path detailsCsv = tempDir.resolve("order_details.csv");

        ProductController productController = new ProductController(productsCsv, batchesCsv);
        CustomerController customerController = new CustomerController(customersCsv);
        OrderController orderController = new OrderController(
                ordersCsv, detailsCsv, productController, customerController);

        LocalDate today = LocalDate.now();
        productController.addProduct(
                new Product("P00001", "ST25 Fragrant Rice 5kg", "Grains", "Bag", 150000.0),
                new Batch("B00001", "P00001", today, today.minusDays(10), today.plusDays(60), 20));
        customerController.registerCustomer(
                "C00001", "Trần Thị Bình", "0912345678", "45 Nguyễn Huệ, District 1", "VIP");

        SalesMenu salesMenu = new SalesMenu(orderController, productController, customerController);

        String simulatedInput = "1\nC00001\n1\nP00001\n2\n2\n3\nY\n\n"
                + "1\nGUEST\n1\nP00001\n5\n0\n\n"
                + "2\n\n"
                + "3\nO00001\n\n"
                + "4\nC00001\n\n"
                + "0\n";
        ScannerManager.getInstance().setInputStream(
                new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));
        salesMenu.display();
        ScannerManager.getInstance().resetToStandardInput();

        assertEquals(1, orderController.getAllOrders().size(),
                "Only finalized VIP order persisted; cancelled guest draft discarded (BR14)");
        Order finalized = orderController.findById("O00001");
        assertTrue(finalized != null, "Order O00001 finalized");
        assertEquals(300000.0, finalized.getTotalAmount(), "Gross amount 2 * 150,000");
        assertEquals(30000.0, finalized.getDiscount(), "VIP 10% discount");
        assertEquals(270000.0, finalized.getFinalAmount(), "Final amount 270,000");
        assertEquals(18, productController.findById("P00001").getAvailableStock(today),
                "Stock reduced by 2 from finalized order, unaffected by cancelled guest cart");

        Files.deleteIfExists(productsCsv);
        Files.deleteIfExists(batchesCsv);
        Files.deleteIfExists(customersCsv);
        Files.deleteIfExists(ordersCsv);
        Files.deleteIfExists(detailsCsv);
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
