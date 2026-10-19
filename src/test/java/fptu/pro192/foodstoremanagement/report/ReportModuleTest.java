package fptu.pro192.foodstoremanagement.report;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.DryFood;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.RegularCustomer;
import fptu.pro192.foodstoremanagement.model.VIPCustomer;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.repository.OrderRepository;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;
import fptu.pro192.foodstoremanagement.service.ReportService;
import fptu.pro192.foodstoremanagement.ui.ReportMenu;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Comprehensive verification test suite for Task #8 (ReportService and ReportMenu).
 * Pure Java 8, zero third-party dependencies.
 *
 * Covers:
 * - BR19: Low stock threshold (<= 5 units).
 * - BR21: Best-selling food products ranking by quantity sold.
 * - BR22: Highest-spending customers ranking by completed order total spend.
 * - BR23: Total revenue and metrics calculated exclusively from completed sales transactions.
 */
public class ReportModuleTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("     RUNNING TASK #8 REPORT MODULE VERIFICATION TESTS           ");
        System.out.println("================================================================");

        testMonthlySalesReportCalculation();
        testBestSellingProductsRanking();
        testHighestSpendingCustomersRanking();
        testLowStockReport();
        testReportServiceValidationAndInvariants();
        testReportMenuInstantiation();

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

    private static void testMonthlySalesReportCalculation() {
        System.out.println("\n--- 1. Testing Monthly Sales Report Math (Task B15 & BR23) ---");

        String pPath = "test_rep_p1.csv";
        String bPath = "test_rep_b1.csv";
        String cPath = "test_rep_c1.csv";
        String oPath = "test_rep_o1.csv";
        String dPath = "test_rep_d1.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);
            ReportService reportService = new ReportService(oRepo, pRepo, cRepo);

            // Completed Order 1 in October 2026: 15 items, finalAmount = 150,000 VND
            Order o1 = new Order("ORD001", "C00001", LocalDate.of(2026, 10, 5));
            o1.addItem(new OrderDetail("ORD001", "P00001", "Gao ST25", "KG", 10000.0, 10));
            o1.addItem(new OrderDetail("ORD001", "P00002", "Mi Hao Hao", "Goi", 10000.0, 5));
            o1.calculateTotals(null);
            o1.setCompleted(true);
            oRepo.saveOrder(o1);

            // Completed Order 2 in October 2026: 20 items, finalAmount = 200,000 VND
            Order o2 = new Order("ORD002", "C00002", LocalDate.of(2026, 10, 15));
            o2.addItem(new OrderDetail("ORD002", "P00001", "Gao ST25", "KG", 10000.0, 20));
            o2.calculateTotals(null);
            o2.setCompleted(true);
            oRepo.saveOrder(o2);

            // Draft Order 3 in October 2026: 100 items, finalAmount = 1,000,000 VND (NOT COMPLETED - BR23)
            Order o3 = new Order("ORD003", "C00001", LocalDate.of(2026, 10, 20));
            o3.addItem(new OrderDetail("ORD003", "P00003", "Dau An", "Chai", 10000.0, 100));
            o3.calculateTotals(null);
            o3.setCompleted(false);
            oRepo.saveOrder(o3);

            // Completed Order 4 in September 2026 (Different month): 50 items, finalAmount = 500,000 VND
            Order o4 = new Order("ORD004", "C00001", LocalDate.of(2026, 9, 25));
            o4.addItem(new OrderDetail("ORD004", "P00001", "Gao ST25", "KG", 10000.0, 50));
            o4.calculateTotals(null);
            o4.setCompleted(true);
            oRepo.saveOrder(o4);

            ReportService.MonthlyReport repOct = reportService.getMonthlySalesReport(10, 2026);
            assertEquals("Monthly report month matches", 10, repOct.getMonth());
            assertEquals("Monthly report year matches", 2026, repOct.getYear());
            assertEquals("Completed transaction count matches (BR23 excludes drafts)", 2, repOct.getTotalTransactions());
            assertEquals("Total items sold matches (15 + 20 = 35)", 35, repOct.getTotalItemsSold());
            assertEquals("Total revenue matches (150,000 + 200,000 = 350,000)", 350000.0, repOct.getTotalRevenue(), 0.001);

            // Check September 2026
            ReportService.MonthlyReport repSep = reportService.getMonthlySalesReport(9, 2026);
            assertEquals("September transaction count matches", 1, repSep.getTotalTransactions());
            assertEquals("September items sold matches", 50, repSep.getTotalItemsSold());
            assertEquals("September revenue matches", 500000.0, repSep.getTotalRevenue(), 0.001);

            // Check Month with zero transactions
            ReportService.MonthlyReport repEmpty = reportService.getMonthlySalesReport(1, 2026);
            assertEquals("Empty month transaction count is 0", 0, repEmpty.getTotalTransactions());
            assertEquals("Empty month items sold is 0", 0, repEmpty.getTotalItemsSold());
            assertEquals("Empty month revenue is 0.0", 0.0, repEmpty.getTotalRevenue(), 0.001);

            // Check validation exceptions
            assertThrows("Invalid month (0) throws IllegalArgumentException", IllegalArgumentException.class,
                    "Tháng không hợp lệ", () -> reportService.getMonthlySalesReport(0, 2026));
            assertThrows("Invalid month (13) throws IllegalArgumentException", IllegalArgumentException.class,
                    "Tháng không hợp lệ", () -> reportService.getMonthlySalesReport(13, 2026));
            assertThrows("Invalid year (0) throws IllegalArgumentException", IllegalArgumentException.class,
                    "Năm không hợp lệ", () -> reportService.getMonthlySalesReport(10, 0));

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testBestSellingProductsRanking() {
        System.out.println("\n--- 2. Testing Best-Selling Products Ranking (Task B16 & BR21) ---");

        String pPath = "test_rep_p2.csv";
        String bPath = "test_rep_b2.csv";
        String cPath = "test_rep_c2.csv";
        String oPath = "test_rep_o2.csv";
        String dPath = "test_rep_d2.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);
            ReportService reportService = new ReportService(oRepo, pRepo, cRepo);

            // Create catalog products
            Food p1 = new DryFood("P00001", "Gao ST25", "DryFood", "KG", 25000.0);
            Food p2 = new DryFood("P00002", "Mi Hao Hao", "DryFood", "Goi", 4500.0);
            Food p3 = new DryFood("P00003", "Nuoc Tuong Maggi", "DryFood", "Chai", 18000.0);
            pRepo.save(p1);
            pRepo.save(p2);
            pRepo.save(p3);

            // Order 1 (Completed): P01: 250 units, P02: 150 units
            Order o1 = new Order("ORD001", "C00001", LocalDate.now());
            o1.addItem(new OrderDetail("ORD001", "P00001", "Gao ST25", "KG", 25000.0, 250));
            o1.addItem(new OrderDetail("ORD001", "P00002", "Mi Hao Hao", "Goi", 4500.0, 150));
            o1.calculateTotals(null);
            o1.setCompleted(true);
            oRepo.saveOrder(o1);

            // Order 2 (Completed): P03: 300 units
            Order o2 = new Order("ORD002", "C00002", LocalDate.now());
            o2.addItem(new OrderDetail("ORD002", "P00003", "Nuoc Tuong Maggi", "Chai", 18000.0, 300));
            o2.calculateTotals(null);
            o2.setCompleted(true);
            oRepo.saveOrder(o2);

            // Order 3 (Draft / Uncompleted): P02: 500 units (must NOT be counted - BR23)
            Order o3 = new Order("ORD003", "C00001", LocalDate.now());
            o3.addItem(new OrderDetail("ORD003", "P00002", "Mi Hao Hao", "Goi", 4500.0, 500));
            o3.calculateTotals(null);
            o3.setCompleted(false);
            oRepo.saveOrder(o3);

            // Retrieve best selling ranking
            List<ReportService.ProductSalesStat> ranking = reportService.getBestSellingProducts(10);
            assertEquals("Ranking list size is 3", 3, ranking.size());

            // BR21: Top 1 is P03 (300 units), Top 2 is P01 (250 units), Top 3 is P02 (150 units)
            assertEquals("Rank 1 product is P00003", "P00003", ranking.get(0).getProductId());
            assertEquals("Rank 1 quantity is 300", 300, ranking.get(0).getQuantitySold());
            assertEquals("Rank 1 name is Nuoc Tuong Maggi", "Nuoc Tuong Maggi", ranking.get(0).getProductName());

            assertEquals("Rank 2 product is P00001", "P00001", ranking.get(1).getProductId());
            assertEquals("Rank 2 quantity is 250", 250, ranking.get(1).getQuantitySold());

            assertEquals("Rank 3 product is P00002", "P00002", ranking.get(2).getProductId());
            assertEquals("Rank 3 quantity is 150 (draft order excluded)", 150, ranking.get(2).getQuantitySold());

            // Test limit parameter
            List<ReportService.ProductSalesStat> top2 = reportService.getBestSellingProducts(2);
            assertEquals("Limit 2 returns exactly 2 products", 2, top2.size());
            assertEquals("Limit 2 first item is P00003", "P00003", top2.get(0).getProductId());
            assertEquals("Limit 2 second item is P00001", "P00001", top2.get(1).getProductId());

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testHighestSpendingCustomersRanking() {
        System.out.println("\n--- 3. Testing Highest-Spending Customers Ranking (Task B17 & BR22) ---");

        String pPath = "test_rep_p3.csv";
        String bPath = "test_rep_b3.csv";
        String cPath = "test_rep_c3.csv";
        String oPath = "test_rep_o3.csv";
        String dPath = "test_rep_d3.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);
            ReportService reportService = new ReportService(oRepo, pRepo, cRepo);

            Customer c1 = new RegularCustomer("C00001", "Nguyen Van An", "0901234567", "Hanoi");
            Customer c2 = new VIPCustomer("C00002", "Tran Thi Binh", "0912345678", "HCM");
            Customer c3 = new RegularCustomer("C00003", "Le Van Cuong", "0923456789", "Da Nang");
            cRepo.save(c1);
            cRepo.save(c2);
            cRepo.save(c3);

            // Order 1: C00001 completes order of 300,000 VND
            Order o1 = new Order("ORD001", "C00001", LocalDate.now(), null, 300000.0, 0.0, 300000.0, true);
            oRepo.saveOrder(o1);

            // Order 2: C00001 completes order of 200,000 VND -> C00001 Total = 500,000 VND across 2 orders
            Order o2 = new Order("ORD002", "C00001", LocalDate.now(), null, 200000.0, 0.0, 200000.0, true);
            oRepo.saveOrder(o2);

            // Order 3: C00002 completes order of 750,000 VND -> C00002 Total = 750,000 VND across 1 order
            Order o3 = new Order("ORD003", "C00002", LocalDate.now(), null, 750000.0, 0.0, 750000.0, true);
            oRepo.saveOrder(o3);

            // Order 4: C00003 has draft order of 1,000,000 VND (NOT COMPLETED - must NOT be counted!)
            Order o4 = new Order("ORD004", "C00003", LocalDate.now(), null, 1000000.0, 0.0, 1000000.0, false);
            oRepo.saveOrder(o4);

            // Order 5: Guest checkout without customerId of 2,000,000 VND (skipped in customer ranking)
            Order o5 = new Order("ORD005", null, LocalDate.now(), null, 2000000.0, 0.0, 2000000.0, true);
            oRepo.saveOrder(o5);

            List<ReportService.CustomerSpendStat> ranking = reportService.getHighestSpendingCustomers(10);
            assertEquals("Customer spend ranking has 2 customers", 2, ranking.size());

            // BR22: C00002 ranks 1st (750,000 VND), C00001 ranks 2nd (500,000 VND)
            assertEquals("Rank 1 customer ID is C00002", "C00002", ranking.get(0).getCustomerId());
            assertEquals("Rank 1 customer Name is Tran Thi Binh", "Tran Thi Binh", ranking.get(0).getCustomerName());
            assertEquals("Rank 1 total spent is 750,000 VND", 750000.0, ranking.get(0).getTotalSpent(), 0.001);
            assertEquals("Rank 1 order count is 1", 1, ranking.get(0).getOrderCount());

            assertEquals("Rank 2 customer ID is C00001", "C00001", ranking.get(1).getCustomerId());
            assertEquals("Rank 2 customer Name is Nguyen Van An", "Nguyen Van An", ranking.get(1).getCustomerName());
            assertEquals("Rank 2 total spent is 500,000 VND", 500000.0, ranking.get(1).getTotalSpent(), 0.001);
            assertEquals("Rank 2 order count is 2", 2, ranking.get(1).getOrderCount());

            // Test limit parameter
            List<ReportService.CustomerSpendStat> top1 = reportService.getHighestSpendingCustomers(1);
            assertEquals("Limit 1 returns 1 customer", 1, top1.size());
            assertEquals("Top 1 customer is C00002", "C00002", top1.get(0).getCustomerId());

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testLowStockReport() {
        System.out.println("\n--- 4. Testing Low Stock Inventory Report (Task B18 & BR19) ---");

        String pPath = "test_rep_p4.csv";
        String bPath = "test_rep_b4.csv";
        String cPath = "test_rep_c4.csv";
        String oPath = "test_rep_o4.csv";
        String dPath = "test_rep_d4.csv";

        cleanFiles(pPath, bPath, cPath, oPath, dPath);

        try {
            ProductRepository pRepo = new ProductRepository(pPath, bPath);
            CustomerRepository cRepo = new CustomerRepository(cPath);
            OrderRepository oRepo = new OrderRepository(oPath, dPath);
            ReportService reportService = new ReportService(oRepo, pRepo, cRepo);

            LocalDate today = LocalDate.now();

            // P01: 2 batches with 2 and 3 units -> totalStock = 5 (LOW STOCK - BR19 threshold <= 5)
            Food p1 = new DryFood("P00001", "Gao Thom", "DryFood", "KG", 20000.0);
            p1.addBatch(new Batch("B001", "P00001", today, today, today.plusDays(30), 2));
            p1.addBatch(new Batch("B002", "P00001", today, today, today.plusDays(40), 3));
            pRepo.save(p1);

            // P02: 1 batch with 6 units -> totalStock = 6 > 5 (NOT low stock)
            Food p2 = new DryFood("P00002", "Duong Trang", "DryFood", "KG", 15000.0);
            p2.addBatch(new Batch("B003", "P00002", today, today, today.plusDays(30), 6));
            pRepo.save(p2);

            // P03: No batches -> totalStock = 0 <= 5 (LOW STOCK - BR19)
            Food p3 = new DryFood("P00003", "Muoi Bien", "DryFood", "KG", 5000.0);
            pRepo.save(p3);

            // P04: Batch of 10 units, but expired yesterday -> available totalStock = 0 <= 5 (LOW STOCK - BR19)
            Food p4 = new DryFood("P00004", "Sua Tuoi", "DryFood", "Hop", 12000.0);
            p4.addBatch(new Batch("B004", "P00004", today.minusDays(10), today.minusDays(10), today.minusDays(1), 10));
            pRepo.save(p4);

            // P05: Total stock = 1 <= 5, BUT soft-deleted (isDeleted = true) -> MUST NOT be in report
            Food p5 = new DryFood("P00005", "Nuoc Mam Cu", "DryFood", "Chai", 25000.0);
            p5.addBatch(new Batch("B005", "P00005", today, today, today.plusDays(30), 1));
            p5.setDeleted(true);
            pRepo.save(p5);

            List<Food> lowStock = reportService.getLowStockReport();
            assertEquals("Low stock list contains exactly 3 items", 3, lowStock.size());

            boolean hasP1 = false;
            boolean hasP2 = false;
            boolean hasP3 = false;
            boolean hasP4 = false;
            boolean hasP5 = false;

            for (Food f : lowStock) {
                if ("P00001".equals(f.getId())) hasP1 = true;
                if ("P00002".equals(f.getId())) hasP2 = true;
                if ("P00003".equals(f.getId())) hasP3 = true;
                if ("P00004".equals(f.getId())) hasP4 = true;
                if ("P00005".equals(f.getId())) hasP5 = true;
            }

            assertTrue("P00001 with totalStock=5 is included (BR19)", hasP1);
            assertTrue("P00002 with totalStock=6 is excluded", !hasP2);
            assertTrue("P00003 with totalStock=0 is included", hasP3);
            assertTrue("P00004 with expired stock (available=0) is included", hasP4);
            assertTrue("P00005 soft-deleted product is excluded", !hasP5);

        } finally {
            cleanFiles(pPath, bPath, cPath, oPath, dPath);
        }
    }

    private static void testReportServiceValidationAndInvariants() {
        System.out.println("\n--- 5. Testing ReportService Null Validations & Edge Cases ---");

        assertThrows("Null OrderRepository throws IllegalArgumentException", IllegalArgumentException.class,
                "OrderRepository cannot be null", () -> new ReportService(null, new ProductRepository(), new CustomerRepository()));

        assertThrows("Null ProductRepository throws IllegalArgumentException", IllegalArgumentException.class,
                "ProductRepository cannot be null", () -> new ReportService(new OrderRepository(), null, new CustomerRepository()));

        assertThrows("Null CustomerRepository throws IllegalArgumentException", IllegalArgumentException.class,
                "CustomerRepository cannot be null", () -> new ReportService(new OrderRepository(), new ProductRepository(), null));

        ReportService service = new ReportService();
        assertTrue("Underlying OrderRepo is accessible", service.getOrderRepo() != null);
        assertTrue("Underlying ProductRepo is accessible", service.getProductRepo() != null);
        assertTrue("Underlying CustomerRepo is accessible", service.getCustomerRepo() != null);

        // Verify immutability of returned list
        List<ReportService.ProductSalesStat> products = service.getBestSellingProducts(10);
        assertThrows("Best-selling products list is unmodifiable", UnsupportedOperationException.class, null, () -> {
            products.clear();
        });

        List<ReportService.CustomerSpendStat> customers = service.getHighestSpendingCustomers(10);
        assertThrows("Highest-spending customers list is unmodifiable", UnsupportedOperationException.class, null, () -> {
            customers.clear();
        });

        List<Food> lowStock = service.getLowStockReport();
        assertThrows("Low-stock list is unmodifiable", UnsupportedOperationException.class, null, () -> {
            lowStock.clear();
        });
    }

    private static void testReportMenuInstantiation() {
        System.out.println("\n--- 6. Testing ReportMenu Instantiation & Configuration ---");

        ReportService service = new ReportService();
        ReportMenu menu1 = new ReportMenu(service);
        assertTrue("ReportMenu instantiated with service", menu1 != null);
        assertTrue("ReportMenu title matches", "BÁO CÁO & THỐNG KÊ DOANH THU".equals(menu1.getTitle()));

        Scanner scanner = new Scanner(System.in);
        ReportMenu menu2 = new ReportMenu(service, scanner);
        assertTrue("ReportMenu instantiated with scanner", menu2.getScanner() == scanner);

        assertThrows("Null ReportService in ReportMenu constructor throws IllegalArgumentException",
                IllegalArgumentException.class, "ReportService cannot be null", () -> new ReportMenu(null));
    }

    private static void cleanFiles(String... paths) {
        for (String p : paths) {
            new File(p).delete();
            new File(p + ".tmp").delete();
        }
    }
}
