package fptu.pro192.foodstoremanagement.report;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.controller.OrderController;
import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.controller.ReportController;
import fptu.pro192.foodstoremanagement.controller.ReportController.CustomerSpendingSummary;
import fptu.pro192.foodstoremanagement.controller.ReportController.MonthlySalesSummary;
import fptu.pro192.foodstoremanagement.controller.ReportController.ProductSalesSummary;
import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import fptu.pro192.foodstoremanagement.view.ReportMenu;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated verification test suite for ReportController and ReportMenu:
 * daily and monthly revenue summaries, best-selling product rankings,
 * customer spending summaries, and inventory health alerts (BR11, BR18, BR19,
 * BR20, BR21, BR22, BR23, BR27).
 */
public class ReportModuleTest {

    private ReportModuleTest() {
    }

    /**
     * Executes all report controller and menu verification tests.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if an unexpected error occurs
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Running ReportModuleTest...");
        testRevenueProductCustomerAndInventoryReports();
        System.out.println("ReportModuleTest PASSED (All assertions succeeded).");
    }

    private static void testRevenueProductCustomerAndInventoryReports() throws Exception {
        Path tempDir = Files.createTempDirectory("report_module_test_");
        Path productsCsv = tempDir.resolve("products.csv");
        Path batchesCsv = tempDir.resolve("batches.csv");
        Path customersCsv = tempDir.resolve("customers.csv");
        Path ordersCsv = tempDir.resolve("orders.csv");
        Path detailsCsv = tempDir.resolve("order_details.csv");

        ProductController productController = new ProductController(productsCsv, batchesCsv);
        CustomerController customerController = new CustomerController(customersCsv);
        OrderController orderController = new OrderController(
                ordersCsv, detailsCsv, productController, customerController);
        ReportController reportController = new ReportController(
                orderController, productController, customerController);

        LocalDate today = LocalDate.now();
        productController.addProduct(
                new Product("P00001", "ST25 Fragrant Rice 5kg", "Grains", "Bag", 150000.0),
                new Batch("B00001", "P00001", today, today.minusDays(10), today.plusDays(60), 20));
        productController.addProduct(
                new Product("P00002", "Hao Hao Instant Noodles", "Noodles", "Pack", 5000.0),
                new Batch("B00002", "P00002", today, today.minusDays(5), today.plusDays(4), 10));

        customerController.registerCustomer(
                "C00001", "Nguyễn Văn An", "0901234567", "12 Lê Lợi, District 1", "Regular");
        customerController.registerCustomer(
                "C00002", "Trần Thị Bình", "0912345678", "45 Nguyễn Huệ, District 1", "VIP");

        Order order1 = orderController.createDraftOrder("O00001", "C00002", today);
        orderController.addItemToDraft(order1, "P00001", 2, today);
        orderController.finalizeOrder(order1, today);

        Order order2 = orderController.createDraftOrder("O00002", "C00001", today);
        orderController.addItemToDraft(order2, "P00002", 6, today);
        orderController.finalizeOrder(order2, today);

        assertEquals(300000.0, reportController.getDailyRevenue(today),
                "Daily net revenue equals 270,000 + 30,000 = 300,000 (BR21)");

        MonthlySalesSummary monthly = reportController.getMonthlySalesReport(
                today.getMonthValue(), today.getYear());
        assertEquals(2, monthly.getTotalTransactions(), "2 completed orders in month");
        assertEquals(8, monthly.getTotalItemsSold(), "8 total units sold in month");
        assertEquals(300000.0, monthly.getTotalRevenue(), "Monthly net revenue");

        List<ProductSalesSummary> topProducts = reportController.getTopSellingProducts(5);
        assertEquals(2, topProducts.size(), "2 sold products ranked");
        assertEquals("P00002", topProducts.get(0).getProductId(),
                "P00002 ranked #1 with 6 units sold vs 2 units of P00001 (BR21)");

        List<ProductSalesSummary> filteredTop = reportController.getTopSellingProducts(
                today.minusDays(1), today.plusDays(1), 5);
        assertEquals(2, filteredTop.size(), "2 sold products ranked within current date range (BR21)");

        List<ProductSalesSummary> emptyTop = reportController.getTopSellingProducts(
                today.plusDays(1), today.plusDays(10), 5);
        assertEquals(0, emptyTop.size(), "0 products sold in future date range (BR21)");

        List<CustomerSpendingSummary> topCustomers = reportController.getCustomerSpendingSummary();
        assertEquals(2, topCustomers.size(), "2 registered customers ranked");
        assertEquals("C00002", topCustomers.get(0).getCustomerId(),
                "C00002 ranked #1 with 270,000 VND spent (BR22)");

        List<CustomerSpendingSummary> filteredCust = reportController.getCustomerSpendingSummary(
                today.minusDays(1), today.plusDays(1));
        assertEquals(2, filteredCust.size(), "2 customers ranked within current date range (BR22)");

        List<CustomerSpendingSummary> emptyCust = reportController.getCustomerSpendingSummary(
                today.plusDays(1), today.plusDays(10));
        assertEquals(0, emptyCust.size(), "0 customers ranked in future date range (BR22)");

        assertEquals(1, reportController.getLowStockReport().size(),
                "P00002 has 4 remaining units (<= 5 low-stock threshold) (BR19)");
        assertEquals(1, reportController.getNearExpiryBatches(7).size(),
                "B00002 expires in 4 days (<= 7 near-expiry window) (BR20)");

        ReportMenu reportMenu = new ReportMenu(reportController);
        String menuInput = "2\n" + today.getMonthValue() + "\n" + today.getYear()
                + "\n\n3\n\n\n5\n\n4\n\n\n\n5\n\n0\n";
        ScannerManager.getInstance().setInputStream(
                new ByteArrayInputStream(menuInput.getBytes(StandardCharsets.UTF_8)));
        reportMenu.display();
        ScannerManager.getInstance().resetToStandardInput();

        Files.deleteIfExists(productsCsv);
        Files.deleteIfExists(batchesCsv);
        Files.deleteIfExists(customersCsv);
        Files.deleteIfExists(ordersCsv);
        Files.deleteIfExists(detailsCsv);
        Files.deleteIfExists(tempDir);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Assertion failed [" + message + "]: expected <"
                    + expected + "> but was <" + actual + ">");
        }
    }
}
