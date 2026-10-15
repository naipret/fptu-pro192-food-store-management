package fptu.pro192.foodstoremanagement.sales;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.DryFood;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.RegularCustomer;
import fptu.pro192.foodstoremanagement.model.Transaction;
import fptu.pro192.foodstoremanagement.model.VIPCustomer;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.repository.OrderRepository;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;
import fptu.pro192.foodstoremanagement.service.CustomerService;
import fptu.pro192.foodstoremanagement.service.InsufficientStockException;
import fptu.pro192.foodstoremanagement.service.OrderService;
import fptu.pro192.foodstoremanagement.service.ProductService;

import java.io.File;
import java.time.LocalDate;

/**
 * Comprehensive verification test suite for Task #7 (Sales Checkout Flow and SalesMenu).
 * Pure Java 8, zero third-party dependencies.
 */
public class SalesModuleTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   RUNNING TASK #7 SALES CHECKOUT MODULE VERIFICATION TESTS     ");
        System.out.println("================================================================");

        testDraftCartCreationAndInvariants();
        testExpiredProductRejection();
        testInsufficientStockRejection();
        testCartAccumulationAndDiscountMath();
        testSaleCancellationLeavesInventoryUntouched();
        testSaleConfirmationDeductsStockFEFOAndPersistsCSV();
        testEmptyOrderConfirmationRejection();

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

    private static void assertThrows(String testName, Class<? extends Throwable> expectedType, String expectedMessageSubstring, Runnable runnable) {
        try {
            runnable.run();
            System.err.printf("  [FAIL] %s: Expected %s but no exception was thrown\n", testName, expectedType.getSimpleName());
            totalFailed++;
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                if (expectedMessageSubstring == null || (t.getMessage() != null && t.getMessage().contains(expectedMessageSubstring))) {
                    System.out.printf("  [PASS] %s\n", testName);
                    totalPassed++;
                } else {
                    System.err.printf("  [FAIL] %s: Expected message containing [%s] but got [%s]\n",
                            testName, expectedMessageSubstring, t.getMessage());
                    totalFailed++;
                }
            } else {
                System.err.printf("  [FAIL] %s: Expected %s but caught %s (%s)\n",
                        testName, expectedType.getSimpleName(), t.getClass().getSimpleName(), t.getMessage());
                totalFailed++;
            }
        }
    }

    private static void testDraftCartCreationAndInvariants() {
        System.out.println("\n--- 1. Testing Draft Cart Creation & Invariants (Task B8) ---");

        String pPath = "test_sales_p1.csv";
        String bPath = "test_sales_b1.csv";
        String cPath = "test_sales_c1.csv";
        String oPath = "test_sales_o1.csv";
        String dPath = "test_sales_d1.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);

            ProductService pService = new ProductService(pRepo);
            CustomerService cService = new CustomerService(cRepo);
            OrderService oService = new OrderService(oRepo, pService, cService);

            // Register customer
            Customer vip = new VIPCustomer("C00001", "Nguyen Van An", "0901234567", "Hanoi");
            cRepo.save(vip);

            // Create draft order for VIP customer
            Order draft = oService.createDraftOrder("ORD001", "C00001", LocalDate.of(2026, 10, 10));
            assertEquals("Draft order ID matches", "ORD001", draft.getOrderId());
            assertEquals("Draft customer ID matches", "C00001", draft.getCustomerId());
            assertEquals("Draft order is not completed", false, draft.isCompleted());
            assertEquals("Draft order has 0 items initially", 0, draft.getItems().size());

            // Draft creation rejects duplicate ID
            oRepo.saveOrder(draft);
            assertThrows("Duplicate order ID throws IllegalArgumentException", IllegalArgumentException.class,
                    "đã tồn tại", () -> {
                        oService.createDraftOrder("ORD001", "C00001");
                    });

            // Draft creation rejects non-existent customer ID
            assertThrows("Non-existent customer throws IllegalArgumentException", IllegalArgumentException.class,
                    "Không tìm thấy khách hàng", () -> {
                        oService.createDraftOrder("ORD002", "C99999");
                    });

            // Empty / blank orderId rejected
            assertThrows("Blank order ID throws IllegalArgumentException", IllegalArgumentException.class,
                    "không được để trống", () -> {
                        oService.createDraftOrder("   ", null);
                    });

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testExpiredProductRejection() {
        System.out.println("\n--- 2. Testing Expired Product Rejection (BR11 Acceptance Criteria) ---");

        String pPath = "test_sales_p2.csv";
        String bPath = "test_sales_b2.csv";
        String cPath = "test_sales_c2.csv";
        String oPath = "test_sales_o2.csv";
        String dPath = "test_sales_d2.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);

            ProductService pService = new ProductService(pRepo);
            CustomerService cService = new CustomerService(cRepo);
            OrderService oService = new OrderService(oRepo, pService, cService);

            LocalDate today = LocalDate.of(2026, 10, 10);

            // Product with only expired batches
            Food expiredFood = new DryFood("P00001", "Banh Quy Bo", "DRY", "Hop", 25000.0);
            Batch expiredBatch = new Batch("B00001", "P00001", today.minusDays(30), today.minusDays(30), today.minusDays(2), 20);
            expiredFood.addBatch(expiredBatch);
            pRepo.save(expiredFood);

            Order draft = oService.createDraftOrder("ORD101", null, today);

            // Acceptance Criteria: Fails with "Failed to add product. This food product has expired." (BR11)
            assertThrows("Cart validation: Adding expired item fails with exact message (BR11)",
                    IllegalArgumentException.class,
                    "Failed to add product. This food product has expired.", () -> {
                        oService.addItemToDraft(draft, "P00001", 1, today);
                    });

            // Deleted product rejected (BR8)
            Food deletedFood = new DryFood("P00002", "Banh Mi", "DRY", "Cai", 10000.0, 15.0, 25.0, 30.0, 60.0, true);
            pRepo.save(deletedFood);
            assertThrows("Cart validation: Adding deleted product fails (BR8)",
                    IllegalArgumentException.class,
                    "Failed to add product. Product does not exist or has been deleted.", () -> {
                        oService.addItemToDraft(draft, "P00002", 1, today);
                    });

            // Sold quantity <= 0 rejected (BR9, BR26)
            Food activeFood = new DryFood("P00003", "Keo Dua", "DRY", "Goi", 15000.0);
            activeFood.addBatch(new Batch("B00002", "P00003", today, today, today.plusDays(30), 50));
            pRepo.save(activeFood);

            assertThrows("Zero quantity rejected (BR9, BR26)", IllegalArgumentException.class,
                    "Sold quantity must be greater than zero", () -> {
                        oService.addItemToDraft(draft, "P00003", 0, today);
                    });

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testInsufficientStockRejection() {
        System.out.println("\n--- 3. Testing Insufficient Stock Rejection (BR10 Acceptance Criteria) ---");

        String pPath = "test_sales_p3.csv";
        String bPath = "test_sales_b3.csv";
        String cPath = "test_sales_c3.csv";
        String oPath = "test_sales_o3.csv";
        String dPath = "test_sales_d3.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);

            ProductService pService = new ProductService(pRepo);
            CustomerService cService = new CustomerService(cRepo);
            OrderService oService = new OrderService(oRepo, pService, cService);

            LocalDate today = LocalDate.of(2026, 10, 10);

            // Product with active stock = 10
            Food food = new DryFood("P00001", "Mi Hao Hao", "DRY", "Goi", 8000.0);
            food.addBatch(new Batch("B00001", "P00001", today, today, today.plusDays(60), 10));
            pRepo.save(food);

            Order draft = oService.createDraftOrder("ORD201", null, today);

            // Acceptance Criteria: Adding quantity > active stock fails with "Failed to add product. Insufficient stock." (BR10)
            assertThrows("Insufficient stock test: Adding qty > active stock fails with exact message (BR10)",
                    InsufficientStockException.class,
                    "Failed to add product. Insufficient stock.", () -> {
                        oService.addItemToDraft(draft, "P00001", 15, today);
                    });

            // Adding within available stock succeeds
            oService.addItemToDraft(draft, "P00001", 8, today);
            assertEquals("Cart contains 8 units", 8, draft.getItems().get(0).getQuantity());

            // Adding more that pushes total in cart over available stock fails
            assertThrows("Adding cumulative quantity exceeding stock fails",
                    InsufficientStockException.class,
                    "Failed to add product. Insufficient stock.", () -> {
                        oService.addItemToDraft(draft, "P00001", 3, today); // 8 + 3 = 11 > 10
                    });

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testCartAccumulationAndDiscountMath() {
        System.out.println("\n--- 4. Testing Cart Accumulation & VIP Discount Strategy (BR13, BR15, BR16, BR17) ---");

        String pPath = "test_sales_p4.csv";
        String bPath = "test_sales_b4.csv";
        String cPath = "test_sales_c4.csv";
        String oPath = "test_sales_o4.csv";
        String dPath = "test_sales_d4.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);

            ProductService pService = new ProductService(pRepo);
            CustomerService cService = new CustomerService(cRepo);
            OrderService oService = new OrderService(oRepo, pService, cService);

            LocalDate today = LocalDate.of(2026, 10, 10);

            // Register VIP and Regular customers
            Customer vip = new VIPCustomer("C00001", "Nguyen VIP", "0901234567", "Hanoi");
            Customer regular = new RegularCustomer("C00002", "Tran Regular", "0912345678", "Danang");
            cRepo.save(vip);
            cRepo.save(regular);

            // Register 2 products
            Food f1 = new DryFood("P00001", "Item 10k", "DRY", "Cai", 10000.0);
            f1.addBatch(new Batch("B00001", "P00001", today, today, today.plusDays(30), 100));
            Food f2 = new DryFood("P00002", "Item 20k", "DRY", "Cai", 20000.0);
            f2.addBatch(new Batch("B00002", "P00002", today, today, today.plusDays(30), 100));
            pRepo.save(f1);
            pRepo.save(f2);

            // Case A: VIP customer gets 10% discount
            Order vipOrder = oService.createDraftOrder("ORD_VIP", "C00001", today);
            oService.addItemToDraft(vipOrder, "P00001", 2, today); // 20,000
            oService.addItemToDraft(vipOrder, "P00002", 4, today); // 80,000
            // Subtotal = 100,000
            assertEquals("VIP Subtotal is 100,000 (BR13)", 100000.0, vipOrder.getSubtotal(), 0.001);
            assertEquals("VIP Discount is 10,000 (10%) (BR16)", 10000.0, vipOrder.getDiscountAmount(), 0.001);
            assertEquals("VIP Final Amount is 90,000 (BR17)", 90000.0, vipOrder.getFinalAmount(), 0.001);

            // Case B: Regular customer gets 0% discount
            Order regOrder = oService.createDraftOrder("ORD_REG", "C00002", today);
            oService.addItemToDraft(regOrder, "P00001", 2, today); // 20,000
            oService.addItemToDraft(regOrder, "P00002", 4, today); // 80,000
            assertEquals("Regular Subtotal is 100,000 (BR13)", 100000.0, regOrder.getSubtotal(), 0.001);
            assertEquals("Regular Discount is 0 (BR15)", 0.0, regOrder.getDiscountAmount(), 0.001);
            assertEquals("Regular Final Amount is 100,000 (BR17)", 100000.0, regOrder.getFinalAmount(), 0.001);

            // Case C: Guest customer gets 0% discount
            Order guestOrder = oService.createDraftOrder("ORD_GUEST", null, today);
            oService.addItemToDraft(guestOrder, "P00001", 1, today);
            assertEquals("Guest Discount is 0", 0.0, guestOrder.getDiscountAmount(), 0.001);
            assertEquals("Guest Final Amount is 10,000", 10000.0, guestOrder.getFinalAmount(), 0.001);

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testSaleCancellationLeavesInventoryUntouched() {
        System.out.println("\n--- 5. Testing Sale Cancellation Leaves Inventory & CSV Untouched (Acceptance Criteria) ---");

        String pPath = "test_sales_p5.csv";
        String bPath = "test_sales_b5.csv";
        String cPath = "test_sales_c5.csv";
        String oPath = "test_sales_o5.csv";
        String dPath = "test_sales_d5.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);

            ProductService pService = new ProductService(pRepo);
            CustomerService cService = new CustomerService(cRepo);
            OrderService oService = new OrderService(oRepo, pService, cService);

            LocalDate today = LocalDate.of(2026, 10, 10);

            // Initial stock = 50
            Food food = new DryFood("P00001", "Nuoc Mam", "DRY", "Chai", 30000.0);
            food.addBatch(new Batch("B00001", "P00001", today, today, today.plusDays(90), 50));
            pRepo.save(food);

            // User builds a draft cart with 20 units
            Order draft = oService.createDraftOrder("ORD_CANCEL", null, today);
            oService.addItemToDraft(draft, "P00001", 20, today);

            // Stock in warehouse must remain 50 while in draft
            assertEquals("Warehouse stock remains 50 while cart is draft", 50, food.getTotalStock(today));

            // User chooses Cancel
            oService.cancelDraft(draft);

            // Verify inventory remains untouched
            assertEquals("Warehouse stock remains 50 after cancellation", 50, food.getTotalStock(today));
            assertEquals("Cancelled draft cart is empty", 0, draft.getItems().size());
            assertEquals("Cancelled draft is not completed", false, draft.isCompleted());

            // Verify CSV files do not contain the cancelled order
            assertEquals("Order repository has 0 saved orders", 0, oRepo.findAll().size());
            assertEquals("Order repository has 0 transactions", 0, oRepo.findAllTransactions().size());

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testSaleConfirmationDeductsStockFEFOAndPersistsCSV() {
        System.out.println("\n--- 6. Testing Sale Confirmation Deducts Stock via FEFO & Persists CSV (BR14, BR24) ---");

        String pPath = "test_sales_p6.csv";
        String bPath = "test_sales_b6.csv";
        String cPath = "test_sales_c6.csv";
        String oPath = "test_sales_o6.csv";
        String dPath = "test_sales_d6.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);

            ProductService pService = new ProductService(pRepo);
            CustomerService cService = new CustomerService(cRepo);
            OrderService oService = new OrderService(oRepo, pService, cService);

            LocalDate today = LocalDate.of(2026, 10, 10);

            // Setup FEFO batches:
            // Batch 1: Expiring in 10 days, quantity = 15
            // Batch 2: Expiring in 30 days, quantity = 25
            Food food = new DryFood("P00001", "Gao Thom", "DRY", "Kg", 20000.0);
            Batch bEarlier = new Batch("B00001", "P00001", today.minusDays(5), today.minusDays(5), today.plusDays(10), 15);
            Batch bLater = new Batch("B00002", "P00001", today, today, today.plusDays(30), 25);
            food.addBatch(bLater);
            food.addBatch(bEarlier);
            pRepo.save(food);

            assertEquals("Initial total stock is 40", 40, food.getTotalStock(today));

            // Create draft order for 20 units (should deplete Batch 1 (15) and take 5 from Batch 2)
            Order draft = oService.createDraftOrder("ORD_CONFIRM", null, today);
            oService.addItemToDraft(draft, "P00001", 20, today);

            // Confirm sale
            Transaction tx = oService.confirmSale(draft, today, "CASH");

            assertTrue("Transaction returned is not null", tx != null);
            assertEquals("Transaction ID links to order", "TX-ORD_CONFIRM", tx.getTransactionId());
            assertEquals("Transaction totalPaid matches final amount", 400000.0, tx.getTotalPaid(), 0.001);
            assertEquals("Order marked isCompleted = true", true, draft.isCompleted());

            // BR14, BR24: Stock deducted via FEFO
            assertEquals("Batch 1 (earlier) depleted to 0", 0, bEarlier.getQuantity());
            assertEquals("Batch 2 reduced from 25 to 20", 20, bLater.getQuantity());
            assertEquals("Total stock reduced from 40 to 20", 20, food.getTotalStock(today));

            // Verify persistence: Reload repositories from disk
            ProductRepository reloadedProductRepo = new ProductRepository(pPath, bPath);
            Food reloadedFood = reloadedProductRepo.findById("P00001");
            assertEquals("Reloaded total stock reflects FEFO deduction (20)", 20, reloadedFood.getTotalStock(today));

            OrderRepository reloadedOrderRepo = new OrderRepository(oPath, dPath);
            assertEquals("Reloaded order repository has 1 order", 1, reloadedOrderRepo.findAll().size());
            assertEquals("Reloaded order repository has 1 completed transaction", 1, reloadedOrderRepo.findAllTransactions().size());

            Order reloadedOrder = reloadedOrderRepo.findById("ORD_CONFIRM");
            assertTrue("Reloaded order exists", reloadedOrder != null);
            assertEquals("Reloaded order has 1 item", 1, reloadedOrder.getItems().size());
            assertEquals("Reloaded item quantity is 20", 20, reloadedOrder.getItems().get(0).getQuantity());
            assertEquals("Reloaded order finalAmount is 400,000", 400000.0, reloadedOrder.getFinalAmount(), 0.001);
            assertTrue("Reloaded order is marked completed", reloadedOrder.isCompleted());

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testEmptyOrderConfirmationRejection() {
        System.out.println("\n--- 7. Testing Empty Order Confirmation Rejection (BR12) ---");

        String pPath = "test_sales_p7.csv";
        String bPath = "test_sales_b7.csv";
        String cPath = "test_sales_c7.csv";
        String oPath = "test_sales_o7.csv";
        String dPath = "test_sales_d7.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);

            ProductService pService = new ProductService(pRepo);
            CustomerService cService = new CustomerService(cRepo);
            OrderService oService = new OrderService(oRepo, pService, cService);

            Order emptyDraft = oService.createDraftOrder("ORD_EMPTY", null, LocalDate.now());

            assertThrows("Confirming empty order throws IllegalArgumentException (BR12)",
                    IllegalArgumentException.class,
                    "Order must contain at least one item", () -> {
                        oService.confirmSale(emptyDraft, LocalDate.now(), "CASH");
                    });

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void cleanFiles(String... paths) {
        for (String p : paths) {
            new File(p).delete();
            new File(p + ".tmp").delete();
        }
    }
}

