package fptu.pro192.foodstoremanagement.report;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.controller.OrderController;
import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.controller.ReportController;
import fptu.pro192.foodstoremanagement.controller.ReportController.CustomerSpendingSummary;
import fptu.pro192.foodstoremanagement.controller.ReportController.ProductSalesSummary;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.Product;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Automated verification test suite for ReportController:
 * daily and monthly revenue summaries, best-selling product rankings,
 * and customer spending rankings with optional period filtering
 * (BR18, BR21, BR22, BR23, BR27).
 */
public class ReportModuleTest {

    private ReportModuleTest() {
    }

    /**
     * Executes all report controller verification tests.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if an unexpected error occurs
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Running ReportModuleTest...");
        testRevenueReports();
        testRankingReports();
        System.out.println("ReportModuleTest PASSED (All assertions succeeded).");
    }

    private static ReportController createTestReportController(Path dir, LocalDate today) throws Exception {
        Path pCsv = dir.resolve("p.csv");
        Path bCsv = dir.resolve("b.csv");
        Path cCsv = dir.resolve("c.csv");
        Path oCsv = dir.resolve("o.csv");
        Path dCsv = dir.resolve("d.csv");

        ProductController pc = new ProductController(pCsv, bCsv);
        CustomerController cc = new CustomerController(cCsv);
        OrderController oc = new OrderController(oCsv, dCsv, pc, cc);

        Product p1 = pc.addProduct("ST25 Fragrant Rice 5kg", "Grains", "Bag", 150000.0);
        pc.importBatch(p1.getId(), today, today.minusDays(10), today.plusDays(60), 20);

        Product p2 = pc.addProduct("Hao Hao Instant Noodles", "Noodles", "Pack", 5000.0);
        pc.importBatch(p2.getId(), today, today.minusDays(5), today.plusDays(4), 10);

        Customer c1 = cc.registerCustomer("Nguyen Van An", "0901234567", "12 Le Loi", "Regular");
        Customer c2 = cc.registerCustomer("Tran Thi Binh", "0912345678", "45 Nguyen Hue", "VIP");

        Order order1 = oc.createDraftOrder(c2.getId());
        oc.addItemToDraft(order1, p1.getId(), 2);
        oc.finalizeOrder(order1);

        Order order2 = oc.createDraftOrder(c1.getId());
        oc.addItemToDraft(order2, p2.getId(), 6);
        oc.finalizeOrder(order2);

        return new ReportController(oc, pc, cc);
    }

    private static void testRevenueReports() throws Exception {
        Path tempDir = Files.createTempDirectory("rep_rev_test_");
        LocalDate today = LocalDate.now();
        ReportController rc = createTestReportController(tempDir, today);

        assertEquals(2, rc.getOrdersByDate(today).size(), "2 orders on current date");
        assertEquals(300000.0, rc.getDailyRevenue(today), "Daily net revenue (BR21)");

        YearMonth currentYm = YearMonth.of(today.getYear(), today.getMonthValue());
        assertEquals(2, rc.getOrdersByMonth(currentYm).size(), "2 completed orders in month");
        assertEquals(300000.0, rc.getMonthlyRevenue(currentYm), "Monthly net revenue (BR21)");

        cleanupDir(tempDir);
    }

    private static void testRankingReports() throws Exception {
        Path tempDir = Files.createTempDirectory("rep_rank_test_");
        LocalDate today = LocalDate.now();
        ReportController rc = createTestReportController(tempDir, today);

        List<ProductSalesSummary> topP = rc.getTopSellingProducts(null, null, 5);
        assertEquals(2, topP.size(), "2 sold products ranked");
        assertEquals("P00002", topP.get(0).getProductId(), "P00002 ranked #1 with 6 units sold (BR21)");
        assertEquals(2, rc.getTopSellingProducts(today.minusDays(1), today.plusDays(1), 5).size(),
                "Product date range");
        assertEquals(0, rc.getTopSellingProducts(today.plusDays(1), today.plusDays(10), 5).size(),
                "Product future range");

        List<CustomerSpendingSummary> topC = rc.getHighestSpendingCustomers(null, null, 5);
        assertEquals(2, topC.size(), "2 registered customers ranked");
        assertEquals("C00002", topC.get(0).getCustomerId(), "C00002 ranked #1 with 270,000 spent (BR22)");
        assertEquals(2, rc.getHighestSpendingCustomers(today.minusDays(1), today.plusDays(1), 5).size(),
                "Cust date range");
        assertEquals(0, rc.getHighestSpendingCustomers(today.plusDays(1), today.plusDays(10), 5).size(),
                "Cust future range");

        cleanupDir(tempDir);
    }

    private static void cleanupDir(Path dir) throws Exception {
        File[] files = dir.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        Files.deleteIfExists(dir);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Assertion failed [" + message + "]: expected <"
                    + expected + "> but was <" + actual + ">");
        }
    }
}
