package fptu.pro192.foodstoremanagement.core;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.controller.OrderController;
import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.controller.ReportController;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import fptu.pro192.foodstoremanagement.view.CustomerMenu;
import fptu.pro192.foodstoremanagement.view.InventoryMenu;
import fptu.pro192.foodstoremanagement.view.MainMenu;
import fptu.pro192.foodstoremanagement.view.ProductMenu;
import fptu.pro192.foodstoremanagement.view.ReportMenu;
import fptu.pro192.foodstoremanagement.view.SalesMenu;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * End-to-end integration test suite verifying navigation and state sharing
 * across MainMenu, all five console submenus, and the retail demo seed datasets
 * in data/.
 */
public class MainMenuIntegrationTest {

    private MainMenuIntegrationTest() {
    }

    /**
     * Executes the end-to-end MainMenu integration and seed dataset verification
     * tests.
     *
     * @param args command-line arguments (unused)
     * @throws Exception if an unexpected error occurs
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Running MainMenuIntegrationTest...");
        testSeedDataCsvLoadingVerification();
        testEndToEndMainMenuNavigationAndSharedState();
        System.out.println("MainMenuIntegrationTest PASSED (All assertions succeeded).");
    }

    private static void testSeedDataCsvLoadingVerification() {
        ProductController productController = new ProductController();
        CustomerController customerController = new CustomerController();
        OrderController orderController = new OrderController(
                productController, customerController);
        ReportController reportController = new ReportController(
                orderController, productController, customerController);

        assertEquals(12, productController.getAllProducts().size(),
                "12 active retail products loaded from data/products.csv");
        assertEquals(6, customerController.getAllCustomers().size(),
                "6 active customers loaded from data/customers.csv");
        assertEquals(6, orderController.getAllOrders().size(),
                "6 completed historical orders loaded from data/orders.csv");
        assertEquals(12, reportController.getTopSellingProducts(0).size(),
                "12 distinct sold products ranked by ReportController");
    }

    private static void testEndToEndMainMenuNavigationAndSharedState() throws Exception {
        Path tempDir = Files.createTempDirectory("main_menu_integration_test_");
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

        MainMenu mainMenu = new MainMenu(
                new ProductMenu(productController),
                new CustomerMenu(customerController),
                new SalesMenu(orderController, productController, customerController),
                new InventoryMenu(productController),
                new ReportMenu(reportController));

        String endToEndInput = "1\n"
                + "3\n\nST25 Fragrant Rice 5kg\n1\n5\n160000\n\n"
                + "0\n"
                + "4\n"
                + "1\nP00001\n\n20\n01/10/2026\n31/12/2026\n\n"
                + "0\n"
                + "2\n"
                + "3\n\nNguyễn Văn An\n0901234567\n12 Lê Lợi, District 1\n2\n\n"
                + "0\n"
                + "3\n"
                + "1\nC00001\n1\nP00001\n2\n3\nY\n\n"
                + "0\n"
                + "5\n"
                + "4\n\n\n\n"
                + "0\n"
                + "0\n";

        ScannerManager.getInstance().setInputStream(
                new ByteArrayInputStream(endToEndInput.getBytes(StandardCharsets.UTF_8)));
        mainMenu.display();
        ScannerManager.getInstance().resetToStandardInput();

        assertEquals(1, productController.getAllProducts().size(),
                "1 product created via MainMenu -> ProductMenu");
        assertEquals(18, productController.findById("P00001").getTotalStock(),
                "20 imported via InventoryMenu minus 2 sold via SalesMenu = 18");
        assertEquals(1, customerController.getAllCustomers().size(), "1 VIP customer created via CustomerMenu");
        assertEquals(1, orderController.getAllOrders().size(), "1 order finalized via SalesMenu");
        assertEquals(288000.0, orderController.findById("O00001").getFinalAmount(),
                "Final amount 2 * 160,000 - 10% VIP discount = 288,000");

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
