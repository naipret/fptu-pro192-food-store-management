package fptu.pro192.foodstoremanagement.order;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.DryFood;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.RegularCustomer;
import fptu.pro192.foodstoremanagement.model.Transaction;
import fptu.pro192.foodstoremanagement.model.VIPCustomer;
import fptu.pro192.foodstoremanagement.repository.OrderRepository;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Verification test suite for Task #4 (Order Domain Models and Order Repository).
 * Pure Java 8, zero third-party dependencies.
 */
public class OrderModuleTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   RUNNING TASK #4 SALES ORDER MODULE VERIFICATION TESTS        ");
        System.out.println("================================================================");

        testOrderDetailCalculationsAndInvariants();
        testOrderTotalAndCustomerDiscountCalculations();
        testTransactionCreationAndLinking();
        testOrderRepositoryPersistenceAndReload();
        testDefensiveCopying();

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

    private static void assertEquals(String testName, double expected, double actual, double delta) {
        boolean match = Math.abs(expected - actual) <= delta;
        if (match) {
            System.out.printf("  [PASS] %s\n", testName);
            totalPassed++;
        } else {
            System.err.printf("  [FAIL] %s: Expected [%f] but got [%f]\n", testName, expected, actual);
            totalFailed++;
        }
    }

    private static void assertThrows(String testName, Runnable runnable) {
        try {
            runnable.run();
            System.err.printf("  [FAIL] %s: Expected exception but none was thrown\n", testName);
            totalFailed++;
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            System.out.printf("  [PASS] %s\n", testName);
            totalPassed++;
        } catch (Exception e) {
            System.err.printf("  [FAIL] %s: Unexpected exception type: %s\n", testName, e.getClass().getName());
            totalFailed++;
        }
    }

    private static void testOrderDetailCalculationsAndInvariants() {
        System.out.println("\n--- 1. Testing OrderDetail Calculations & Invariants ---");

        // BR13: subtotal = unitPrice * quantity
        OrderDetail detail = new OrderDetail("ORD001", "P00001", "Mi Goi Hao Hao", "Goi", 8000.0, 5);
        assertEquals("Subtotal calculation: 8000 * 5 = 40000", 40000.0, detail.getSubtotal(), 0.001);

        // BR9, BR26: quantity > 0
        assertThrows("Zero quantity throws IllegalArgumentException", () -> {
            new OrderDetail("ORD001", "P00001", "Mi Goi", "Goi", 8000.0, 0);
        });
        assertThrows("Negative quantity throws IllegalArgumentException", () -> {
            new OrderDetail("ORD001", "P00001", "Mi Goi", "Goi", 8000.0, -2);
        });

        // BR5: unitPrice > 0
        assertThrows("Zero unitPrice throws IllegalArgumentException", () -> {
            new OrderDetail("ORD001", "P00001", "Mi Goi", "Goi", 0.0, 1);
        });
        assertThrows("Negative unitPrice throws IllegalArgumentException", () -> {
            new OrderDetail("ORD001", "P00001", "Mi Goi", "Goi", -5000.0, 1);
        });

        // Snapshot constructor from Food
        Food dryFood = new DryFood("P00002", "Gao ST25", "DRY", "Kg", 35000.0);
        OrderDetail snapshotDetail = new OrderDetail("ORD002", dryFood, 3);
        assertEquals("Snapshot captures product ID", "P00002", snapshotDetail.getProductId());
        assertEquals("Snapshot captures product Name", "Gao ST25", snapshotDetail.getProductName());
        assertEquals("Snapshot captures product Unit", "Kg", snapshotDetail.getUnit());
        assertEquals("Snapshot captures unit price", 35000.0, snapshotDetail.getUnitPrice(), 0.001);
        assertEquals("Snapshot captures quantity", 3, snapshotDetail.getQuantity());
        assertEquals("Snapshot calculates subtotal correctly", 105000.0, snapshotDetail.getSubtotal(), 0.001);

        // BR8: Deleted food rejected
        Food deletedFood = new DryFood("P00003", "Nuoc Tuong", "DRY", "Chai", 15000.0, 15.0, 25.0, 30.0, 60.0, true);
        assertThrows("Deleted product cannot be added to order (BR8)", () -> {
            new OrderDetail("ORD003", deletedFood, 1);
        });
        assertThrows("Null product cannot be added to order", () -> {
            new OrderDetail("ORD003", (Food) null, 1);
        });
    }

    private static void testOrderTotalAndCustomerDiscountCalculations() {
        System.out.println("\n--- 2. Testing Order Totals & Customer Discount Calculations ---");

        // Acceptance Criteria Test:
        // Adding 3 items with prices 10k, 20k, 30k produces subtotal 60k.
        // Attaching a VIP customer produces discount 6,000 VND and final total 54,000 VND (BR13, BR16, BR17).
        Order order = new Order("ORD001", "C00001", LocalDate.of(2026, 10, 8));

        Food f1 = new DryFood("P00001", "Item A", "DRY", "Cai", 10000.0);
        Food f2 = new DryFood("P00002", "Item B", "DRY", "Cai", 20000.0);
        Food f3 = new DryFood("P00003", "Item C", "DRY", "Cai", 30000.0);

        order.addItem(f1, 1);
        order.addItem(f2, 1);
        order.addItem(f3, 1);

        // Test with VIP Customer (10% discount)
        Customer vip = new VIPCustomer("C00001", "Nguyen Van A", "0901234567", "Hanoi");
        order.calculateTotals(vip);

        assertEquals("Subtotal with 3 items (10k + 20k + 30k) is 60,000", 60000.0, order.getSubtotal(), 0.001);
        assertEquals("VIP discount (10% of 60,000) is 6,000", 6000.0, order.getDiscountAmount(), 0.001);
        assertEquals("VIP final amount (60,000 - 6,000) is 54,000", 54000.0, order.getFinalAmount(), 0.001);

        // Test with Regular Customer (0% discount - BR15)
        Customer regular = new RegularCustomer("C00002", "Tran Van B", "0912345678", "Hanoi");
        order.calculateTotals(regular);

        assertEquals("Subtotal remains 60,000", 60000.0, order.getSubtotal(), 0.001);
        assertEquals("Regular customer discount is 0", 0.0, order.getDiscountAmount(), 0.001);
        assertEquals("Regular customer final amount is 60,000", 60000.0, order.getFinalAmount(), 0.001);

        // Test with Guest / null Customer
        order.calculateTotals(null);
        assertEquals("Guest customer discount is 0", 0.0, order.getDiscountAmount(), 0.001);
        assertEquals("Guest customer final amount is 60,000", 60000.0, order.getFinalAmount(), 0.001);
    }

    private static void testTransactionCreationAndLinking() {
        System.out.println("\n--- 3. Testing Transaction Creation & Linking ---");

        Order order = new Order("ORD001", "C00001", LocalDate.of(2026, 10, 8));
        Food food = new DryFood("P00001", "Gao Thom", "DRY", "Kg", 25000.0);
        order.addItem(food, 2);
        order.calculateTotals(null);
        order.setCompleted(true);

        Transaction tx = new Transaction("TX0001", order, "BANK_TRANSFER");
        assertEquals("Transaction ID set", "TX0001", tx.getTransactionId());
        assertEquals("Order ID linked", "ORD001", tx.getOrderId());
        assertEquals("Customer ID linked", "C00001", tx.getCustomerId());
        assertEquals("Total paid equals final order amount", 50000.0, tx.getTotalPaid(), 0.001);
        assertEquals("Payment method matches", "BANK_TRANSFER", tx.getPaymentMethod());
        assertEquals("Transaction date matches order date", LocalDate.of(2026, 10, 8), tx.getTransactionDate());

        assertThrows("Null order throws IllegalArgumentException", () -> {
            new Transaction("TX0002", (Order) null, "CASH");
        });
    }

    private static void testOrderRepositoryPersistenceAndReload() {
        System.out.println("\n--- 4. Testing OrderRepository Persistence & Reload ---");

        String ordersTestPath = "test_orders.csv";
        String detailsTestPath = "test_order_details.csv";

        new File(ordersTestPath).delete();
        new File(detailsTestPath).delete();

        try {
            OrderRepository repo = new OrderRepository(ordersTestPath, detailsTestPath);

            // Create Order 1 with 2 items
            Order o1 = new Order("ORD001", "C00001", LocalDate.of(2026, 10, 8));
            Food f1 = new DryFood("P00001", "Mi Hao Hao", "DRY", "Goi", 8000.0);
            Food f2 = new DryFood("P00002", "Sua Dac", "DRY", "Lon", 24000.0);
            o1.addItem(f1, 5); // 40,000
            o1.addItem(f2, 1); // 24,000
            Customer vip = new VIPCustomer("C00001", "Nguyen Van A", "0901234567", "Hanoi");
            o1.calculateTotals(vip); // Subtotal 64,000, Discount 6,400, Final 57,600
            o1.setCompleted(true);
            repo.saveOrder(o1);

            // Create Order 2 with 1 item
            Order o2 = new Order("ORD002", "C00002", LocalDate.of(2026, 10, 8));
            o2.addItem(f1, 2); // 16,000
            o2.calculateTotals(null);
            o2.setCompleted(true);
            repo.saveOrder(o2);

            assertEquals("Repository contains 2 orders", 2, repo.findAll().size());
            assertEquals("Repository contains 2 completed transactions", 2, repo.findAllTransactions().size());

            // Reload repository from CSV files
            OrderRepository reloaded = new OrderRepository(ordersTestPath, detailsTestPath);
            assertEquals("Reloaded repository has 2 orders", 2, reloaded.findAll().size());

            Order reloadedO1 = reloaded.findById("ORD001");
            assertTrue("Reloaded ORD001 exists", reloadedO1 != null);
            assertEquals("Reloaded ORD001 customer ID matches", "C00001", reloadedO1.getCustomerId());
            assertEquals("Reloaded ORD001 has 2 items", 2, reloadedO1.getItems().size());
            assertEquals("Reloaded ORD001 subtotal matches", 64000.0, reloadedO1.getSubtotal(), 0.001);
            assertEquals("Reloaded ORD001 discount matches", 6400.0, reloadedO1.getDiscountAmount(), 0.001);
            assertEquals("Reloaded ORD001 final amount matches", 57600.0, reloadedO1.getFinalAmount(), 0.001);
            assertTrue("Reloaded ORD001 is completed", reloadedO1.isCompleted());

            // Check item detail snapshots
            OrderDetail item1 = reloadedO1.getItems().get(0);
            assertEquals("Item 1 productId matches", "P00001", item1.getProductId());
            assertEquals("Item 1 quantity matches", 5, item1.getQuantity());
            assertEquals("Item 1 unitPrice matches", 8000.0, item1.getUnitPrice(), 0.001);
            assertEquals("Item 1 subtotal matches", 40000.0, item1.getSubtotal(), 0.001);

            // Check case-insensitive lookup
            Order caseInsensitive = reloaded.findById("ord001");
            assertTrue("Case-insensitive order lookup succeeds", caseInsensitive != null);

            // Check customer lookup
            List<Order> cust1Orders = reloaded.findByCustomerId("C00001");
            assertEquals("Customer C00001 has 1 order", 1, cust1Orders.size());

            // Check completed orders query
            List<Order> completedOrders = reloaded.findCompletedOrders();
            assertEquals("Both orders are completed", 2, completedOrders.size());

            // Check transactions list
            List<Transaction> txList = reloaded.findAllTransactions();
            assertEquals("2 transactions reconstructed on reload", 2, txList.size());

        } finally {
            new File(ordersTestPath).delete();
            new File(detailsTestPath).delete();
            new File(ordersTestPath + ".tmp").delete();
            new File(detailsTestPath + ".tmp").delete();
        }
    }

    private static void testDefensiveCopying() {
        System.out.println("\n--- 5. Testing Defensive Copying & Immutability ---");

        Order order = new Order("ORD001", "C00001", LocalDate.now());
        Food food = new DryFood("P00001", "Item", "DRY", "Cai", 10000.0);
        order.addItem(food, 1);

        assertThrows("Direct modification of order items throws UnsupportedOperationException", () -> {
            order.getItems().add(new OrderDetail("ORD001", food, 2));
        });

        OrderRepository repo = new OrderRepository("data/orders.csv", "data/order_details.csv");
        assertThrows("Direct modification of repo.findAll() throws UnsupportedOperationException", () -> {
            repo.findAll().add(new Order("ORD999", "C999", LocalDate.now()));
        });

        assertThrows("Direct modification of repo.findAllTransactions() throws UnsupportedOperationException", () -> {
            repo.findAllTransactions().add(new Transaction("TX999", "ORD999", "C999", LocalDate.now(), 100.0, "CASH"));
        });
    }
}
