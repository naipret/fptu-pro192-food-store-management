package fptu.pro192.foodstoremanagement;

import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.repository.OrderRepository;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;
import fptu.pro192.foodstoremanagement.service.CustomerService;
import fptu.pro192.foodstoremanagement.service.OrderService;
import fptu.pro192.foodstoremanagement.service.ProductService;
import fptu.pro192.foodstoremanagement.service.ReportService;
import fptu.pro192.foodstoremanagement.ui.CustomerMenu;
import fptu.pro192.foodstoremanagement.ui.MainMenu;
import fptu.pro192.foodstoremanagement.ui.ProductMenu;
import fptu.pro192.foodstoremanagement.ui.ReportMenu;
import fptu.pro192.foodstoremanagement.ui.SalesMenu;
import fptu.pro192.foodstoremanagement.util.TableFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Application entry point and composition root for the Food Store Management System.
 * Coordinates manual dependency injection across Repositories, Services, and Menus,
 * manages global uncaught exception guards, and executes graceful shutdown hooks.
 */
public class Main {

    private static final int BANNER_WIDTH = 80;

    /**
     * Main application execution method.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Register graceful shutdown hook for resource finalization
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                // Ensures terminal flush and clean release on abrupt SIGINT / termination
            }
        }));

        try {
            // 1. Ensure required data storage directory exists
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }

            // 2. Instantiate persistent repositories (composition root singletons)
            ProductRepository productRepo = new ProductRepository();
            CustomerRepository customerRepo = new CustomerRepository();
            OrderRepository orderRepo = new OrderRepository();

            // 3. Instantiate business services with explicit repository dependency injection
            ProductService productService = new ProductService(productRepo);
            CustomerService customerService = new CustomerService(customerRepo);
            OrderService orderService = new OrderService(orderRepo, productService, customerService);
            ReportService reportService = new ReportService(orderRepo, productRepo, customerRepo);

            // 4. Instantiate presentation menus wired with shared service singletons
            ProductMenu productMenu = new ProductMenu(productService);
            CustomerMenu customerMenu = new CustomerMenu(customerService);
            SalesMenu salesMenu = new SalesMenu(orderService, productService, customerService);
            ReportMenu reportMenu = new ReportMenu(reportService);

            // 5. Wire root MainMenu dashboard
            MainMenu mainMenu = new MainMenu(productMenu, customerMenu, salesMenu, reportMenu);

            // 6. Launch main menu lifecycle
            mainMenu.display();

        } catch (IOException e) {
            System.err.println("Lỗi khởi tạo hệ thống lưu trữ tệp dữ liệu: " + e.getMessage());
        } catch (Throwable t) {
            System.err.println("\nĐã xảy ra lỗi hệ thống nghiêm trọng ngoài dự kiến: " + t.getMessage());
            t.printStackTrace();
        } finally {
            // 7. Warm farewell message upon graceful application termination
            renderFarewellMessage();
        }
    }

    /**
     * Renders a standardized 80-column farewell message when the application exits cleanly.
     */
    private static void renderFarewellMessage() {
        System.out.println("\n=" + TableFormatter.repeat('=', BANNER_WIDTH - 2) + "=");
        System.out.println(TableFormatter.center("CẢM ƠN BẠN ĐÃ SỬ DỤNG HỆ THỐNG QUẢN LÝ BÁN HÀNG THỰC PHẨM!", BANNER_WIDTH));
        System.out.println(TableFormatter.center("Hẹn gặp lại quý khách trong các phiên làm việc tiếp theo.", BANNER_WIDTH));
        System.out.println("=" + TableFormatter.repeat('=', BANNER_WIDTH - 2) + "=");
    }
}
