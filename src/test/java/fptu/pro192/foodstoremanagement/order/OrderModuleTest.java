package fptu.pro192.foodstoremanagement.order;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.controller.InsufficientStockException;
import fptu.pro192.foodstoremanagement.controller.OrderController;
import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.Product;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
        testExpiredBatchOrderGuard();
        testInsufficientStockOrderGuard();
        System.out.println("OrderModuleTest PASSED (All assertions succeeded).");
    }

    private static void testOrderDetailMergingAndSnapshotPricing() {
        Product rice = new Product("P00001", "ST25 Fragrant Rice 5kg", "Grains", "Bag", 165000.0);
        Order draft = new Order("O00001", null, LocalDateTime.now());

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
        Path pCsv = tempDir.resolve("p.csv");
        Path bCsv = tempDir.resolve("b.csv");
        Path cCsv = tempDir.resolve("c.csv");
        Path oCsv = tempDir.resolve("o.csv");
        Path dCsv = tempDir.resolve("d.csv");

        ProductController pc = new ProductController(pCsv, bCsv);
        CustomerController cc = new CustomerController(cCsv);
        OrderController oc = new OrderController(oCsv, dCsv, pc, cc);

        LocalDate today = LocalDate.now();
        Product milk = pc.addProduct("Vinamilk Fresh Milk 1L", "Dairy", "Carton", 40000.0);
        pc.importBatch(milk.getId(), today, today.minusDays(5), today.plusDays(30), 10);
        pc.importBatch(milk.getId(), today, today.minusDays(10), today.plusDays(5), 4);
        Customer vip = cc.registerCustomer("Trần Thị Bình", "0912345678", "45 Nguyễn Huệ", "VIP");

        Order draft = oc.createDraftOrder(vip.getId());
        oc.addItemToDraft(draft, milk.getId(), 6);
        assertEquals(14, pc.findById(milk.getId()).getAvailableStock(), "Stock not deducted in draft (BR14)");
        assertEquals(240000.0, draft.getTotalAmount(), "Gross total 6 * 40,000 (BR13)");
        assertEquals(24000.0, draft.getDiscount(), "VIP 10% discount (BR15)");
        assertEquals(216000.0, draft.getFinalAmount(), "Final amount (BR16)");

        Order finalized = oc.finalizeOrder(draft);
        assertEquals("O00001", finalized.getOrderId(), "Finalized order ID");
        assertEquals(8, pc.findById(milk.getId()).getAvailableStock(), "Stock deducted to 8 (BR14)");

        OrderController reloaded = new OrderController(oCsv, dCsv, pc, cc);
        Order loaded = reloaded.findById("O1");
        assertTrue(loaded != null, "Reloaded order found");
        assertEquals(216000.0, loaded.getFinalAmount(), "Reloaded order final amount");
        assertEquals(1, reloaded.searchOrders("O1").size(), "Search order with shorthand O1");

        deleteFiles(pCsv, bCsv, cCsv, oCsv, dCsv, tempDir);
    }

    private static void testExpiredBatchOrderGuard() throws Exception {
        Path tempDir = Files.createTempDirectory("order_exp_guard_test_");
        Path pCsv = tempDir.resolve("p.csv");
        Path bCsv = tempDir.resolve("b.csv");
        Path cCsv = tempDir.resolve("c.csv");
        Path oCsv = tempDir.resolve("o.csv");
        Path dCsv = tempDir.resolve("d.csv");

        ProductController pc = new ProductController(pCsv, bCsv);
        CustomerController cc = new CustomerController(cCsv);
        OrderController oc = new OrderController(oCsv, dCsv, pc, cc);

        LocalDate today = LocalDate.now();
        Product p1 = pc.addProduct("Expired Yogurt", "Dairy", "Pack", 28000.0);
        pc.importBatch(p1.getId(), today.minusDays(20), today.minusDays(25), today.minusDays(1), 12);

        Order draft = oc.createDraftOrder(null);
        boolean caught = false;
        try {
            oc.addItemToDraft(draft, p1.getId(), 1);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Adding expired product must throw IllegalArgumentException (BR11)");
        deleteFiles(pCsv, bCsv, cCsv, oCsv, dCsv, tempDir);
    }

    private static void testInsufficientStockOrderGuard() throws Exception {
        Path tempDir = Files.createTempDirectory("order_stock_guard_test_");
        Path pCsv = tempDir.resolve("p.csv");
        Path bCsv = tempDir.resolve("b.csv");
        Path cCsv = tempDir.resolve("cCsv.csv");
        Path oCsv = tempDir.resolve("o.csv");
        Path dCsv = tempDir.resolve("d.csv");

        ProductController pc = new ProductController(pCsv, bCsv);
        CustomerController cc = new CustomerController(cCsv);
        OrderController oc = new OrderController(oCsv, dCsv, pc, cc);

        LocalDate today = LocalDate.now();
        Product p = pc.addProduct("Canned Tuna", "Canned", "Can", 32000.0);
        pc.importBatch(p.getId(), today, today.minusDays(10), today.plusDays(90), 5);

        Order draft = oc.createDraftOrder(null);
        boolean caught = false;
        try {
            oc.addItemToDraft(draft, p.getId(), 6);
        } catch (InsufficientStockException e) {
            caught = true;
        }
        assertTrue(caught, "Exceeding stock must throw InsufficientStockException (BR10)");
        deleteFiles(pCsv, bCsv, cCsv, oCsv, dCsv, tempDir);
    }

    private static void deleteFiles(Path... paths) throws Exception {
        for (Path p : paths) {
            Files.deleteIfExists(p);
        }
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
