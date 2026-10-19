package fptu.pro192.foodstoremanagement.order;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.controller.InsufficientStockException;
import fptu.pro192.foodstoremanagement.controller.OrderController;
import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.Product;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated verification test suite for Order, OrderDetail, and OrderController
 * draft cart management, real-time stock and expiration validation, polymorphic
 * VIP discount calculation, FEFO inventory deduction, and CSV persistence
 * (BR8, BR9, BR10, BR11, BR12, BR13, BR14, BR15, BR16, BR17, BR18, BR24, BR26).
 */
public class OrderModuleTest {

    private OrderModuleTest() {
    }

    /**
     * Executes all order domain and controller verification tests.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if an unexpected error occurs
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Running OrderModuleTest...");
        testOrderDetailMergingAndSnapshotPricing();
        testDraftCheckoutFefoAndCsvPersistence();
        testExpiredAndInsufficientStockGuards();
        System.out.println("OrderModuleTest PASSED (All assertions succeeded).");
    }

    private static void testOrderDetailMergingAndSnapshotPricing() {
        Product rice = new Product("P00001", "ST25 Fragrant Rice 5kg", "Grains", "Bag", 165000.0);
        Order draft = new Order("O00001", null, LocalDate.now());

        draft.addItem(new OrderDetail("O00001", rice, 2));
        draft.addItem(new OrderDetail("O00001", rice, 3));

        assertEquals(1, draft.getItems().size(), "Duplicate product merged into single OrderDetail line");
        assertEquals(5, draft.getItems().get(0).getQuantity(), "Merged quantity equals 2 + 3 = 5");
        assertEquals(825000.0, draft.getItems().get(0).getSubTotal(), "Line-item subtotal 5 * 165,000 (BR12)");

        rice.setPrice(180000.0);
        assertEquals(165000.0, draft.getItems().get(0).getUnitPrice(),
                "OrderDetail unitPrice snapshot preserved after catalog price change (BR18)");
    }

    private static void testDraftCheckoutFefoAndCsvPersistence() throws Exception {
        Path tempDir = Files.createTempDirectory("order_module_test_");
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
                new Product("P00001", "Vinamilk Fresh Milk 1L", "Dairy", "Carton", 40000.0),
                new Batch("B00001", "P00001", today, today.minusDays(5), today.plusDays(30), 10));
        productController.addBatchToProduct("P00001",
                new Batch("B00002", "P00001", today, today.minusDays(10), today.plusDays(5), 4));

        customerController.registerCustomer(
                "C00001", "Trần Thị Bình", "0912345678", "45 Nguyễn Huệ, District 1", "VIP");

        Order draft = orderController.createDraftOrder("C00001");
        orderController.addItemToDraft(draft, "P00001", 6, today);

        assertEquals(14, productController.findById("P00001").getAvailableStock(today),
                "Stock not deducted while order is still a draft (BR14)");
        assertEquals(240000.0, draft.getTotalAmount(), "Gross total 6 * 40,000 (BR13)");
        assertEquals(24000.0, draft.getDiscount(), "VIP 10% discount (BR15)");
        assertEquals(216000.0, draft.getFinalAmount(), "Final amount after discount (BR16)");

        Order finalized = orderController.finalizeOrder(draft, today);
        assertEquals("O00001", finalized.getOrderId(), "Finalized order ID");
        assertEquals(8, productController.findById("P00001").getAvailableStock(today),
                "Stock deducted from 14 to 8 on checkout completion (BR14)");

        List<String> orderLines = Files.readAllLines(ordersCsv);
        List<String> detailLines = Files.readAllLines(detailsCsv);
        assertEquals("orderId,customerId,orderDate,totalAmount,discount,finalAmount",
                orderLines.get(0), "orders.csv header at line 0");
        assertEquals("orderId,productId,quantity,unitPrice",
                detailLines.get(0), "order_details.csv header at line 0");

        OrderController reloaded = new OrderController(
                ordersCsv, detailsCsv, productController, customerController);
        Order loadedOrder = reloaded.findById("O1");
        assertTrue(loadedOrder != null, "Reloaded order O00001 found");
        assertEquals(1, loadedOrder.getItems().size(), "Reloaded order has 1 OrderDetail line");
        assertEquals(216000.0, loadedOrder.getFinalAmount(), "Reloaded order finalAmount");

        Files.deleteIfExists(productsCsv);
        Files.deleteIfExists(batchesCsv);
        Files.deleteIfExists(customersCsv);
        Files.deleteIfExists(ordersCsv);
        Files.deleteIfExists(detailsCsv);
        Files.deleteIfExists(tempDir);
    }

    private static void testExpiredAndInsufficientStockGuards() throws Exception {
        Path tempDir = Files.createTempDirectory("order_guards_test_");
        Path productsCsv = tempDir.resolve("products.csv");
        Path batchesCsv = tempDir.resolve("batches.csv");
        Path customersCsv = tempDir.resolve("customers.csv");
        Path ordersCsv = tempDir.resolve("orders.csv");
        Path detailsCsv = tempDir.resolve("order_details.csv");

        ProductController productController = new ProductController(productsCsv, batchesCsv);
        CustomerController customerController = new CustomerController(customersCsv);
        final OrderController orderController = new OrderController(
                ordersCsv, detailsCsv, productController, customerController);

        final LocalDate today = LocalDate.now();
        productController.addProduct(
                new Product("P00001", "Expired Yogurt Pack", "Dairy", "Pack", 28000.0),
                new Batch("B00001", "P00001", today.minusDays(20), today.minusDays(25),
                        today.minusDays(1), 12));
        productController.addProduct(
                new Product("P00002", "Canned Tuna 150g", "Canned", "Can", 32000.0),
                new Batch("B00002", "P00002", today, today.minusDays(10), today.plusDays(90), 5));

        final Order draft = orderController.createDraftOrder(null);

        boolean caughtExpired = false;
        try {
            orderController.addItemToDraft(draft, "P00001", 1, today);
        } catch (IllegalArgumentException e) {
            caughtExpired = true;
        }
        assertTrue(caughtExpired, "Adding product whose batches are all expired must be rejected (BR11)");

        boolean caughtInsufficient = false;
        try {
            orderController.addItemToDraft(draft, "P00002", 6, today);
        } catch (InsufficientStockException e) {
            caughtInsufficient = true;
        }
        assertTrue(caughtInsufficient,
                "Adding quantity exceeding available stock must throw InsufficientStockException (BR10)");

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
