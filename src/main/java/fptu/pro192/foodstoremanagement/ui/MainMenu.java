package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.repository.OrderRepository;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;
import fptu.pro192.foodstoremanagement.service.CustomerService;
import fptu.pro192.foodstoremanagement.service.OrderService;
import fptu.pro192.foodstoremanagement.service.ProductService;
import fptu.pro192.foodstoremanagement.service.ReportService;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import fptu.pro192.foodstoremanagement.util.TableFormatter;

import java.util.Scanner;

/**
 * Root console navigation menu for the Food Store Management System.
 * Coordinates high-level screen routing across Product, Customer, Sales, Inventory,
 * and Reporting modules conforming to the 80-column ASCII terminal display standard
 * and Template Method Pattern.
 */
public class MainMenu extends AbstractMenu {

    private final ProductMenu productMenu;
    private final CustomerMenu customerMenu;
    private final SalesMenu salesMenu;
    private final ReportMenu reportMenu;
    private final Scanner scanner;

    /**
     * Default constructor wiring production repositories, services, and menus with shared state.
     */
    public MainMenu() {
        this(createDefaultProductMenu(),
                createDefaultCustomerMenu(),
                createDefaultSalesMenu(),
                createDefaultReportMenu(),
                ScannerManager.getInstance().getScanner());
    }

    /**
     * Parameterized constructor enabling dependency injection of all feature menus.
     *
     * @param productMenu The food product and inventory menu
     * @param customerMenu The customer management menu
     * @param salesMenu The sales checkout and order menu
     * @param reportMenu The reporting and statistics menu
     * @throws IllegalArgumentException If any required menu dependency is null
     */
    public MainMenu(ProductMenu productMenu, CustomerMenu customerMenu, SalesMenu salesMenu, ReportMenu reportMenu) {
        this(productMenu, customerMenu, salesMenu, reportMenu, ScannerManager.getInstance().getScanner());
    }

    /**
     * Parameterized constructor enabling dependency injection with custom scanner.
     *
     * @param productMenu The food product and inventory menu
     * @param customerMenu The customer management menu
     * @param salesMenu The sales checkout and order menu
     * @param reportMenu The reporting and statistics menu
     * @param scanner The input scanner
     * @throws IllegalArgumentException If any required menu dependency is null
     */
    public MainMenu(ProductMenu productMenu, CustomerMenu customerMenu, SalesMenu salesMenu, ReportMenu reportMenu, Scanner scanner) {
        super("HỆ THỐNG QUẢN LÝ BÁN HÀNG THỰC PHẨM");
        if (productMenu == null) {
            throw new IllegalArgumentException("ProductMenu cannot be null.");
        }
        if (customerMenu == null) {
            throw new IllegalArgumentException("CustomerMenu cannot be null.");
        }
        if (salesMenu == null) {
            throw new IllegalArgumentException("SalesMenu cannot be null.");
        }
        if (reportMenu == null) {
            throw new IllegalArgumentException("ReportMenu cannot be null.");
        }
        this.productMenu = productMenu;
        this.customerMenu = customerMenu;
        this.salesMenu = salesMenu;
        this.reportMenu = reportMenu;
        this.scanner = (scanner != null) ? scanner : ScannerManager.getInstance().getScanner();
    }

    @Override
    protected void renderBody() {
        System.out.println("  1. Quản lý Sản phẩm Thực phẩm (Manage Food Products)");
        System.out.println("  2. Quản lý Khách hàng (Manage Customers)");
        System.out.println("  3. Quản lý Bán hàng & Tạo Đơn (Sales Management)");
        System.out.println("  4. Quản lý Tồn kho & Hạn Dùng (Inventory Management)");
        System.out.println("  5. Báo cáo & Thống kê Doanh thu (Reports)");
        System.out.println("  6. Thoát hệ thống (Exit)");
    }

    @Override
    protected int getMaxOption() {
        return 6;
    }

    @Override
    protected void renderFooter() {
        System.out.println("-" + TableFormatter.repeat('-', MENU_WIDTH - 2) + "-");
        System.out.println("[0] Thoát hệ thống | Nhập số tương ứng [1 - 6] để chọn chức năng");
        System.out.println("-" + TableFormatter.repeat('-', MENU_WIDTH - 2) + "-");
    }

    @Override
    protected boolean handleOption(int choice) {
        switch (choice) {
            case 1:
                productMenu.display();
                break;
            case 2:
                customerMenu.display();
                break;
            case 3:
                salesMenu.display();
                break;
            case 4:
                // Routes to inventory management features in ProductMenu
                productMenu.display();
                break;
            case 5:
                reportMenu.display();
                break;
            case 6:
                return false; // Clean exit
            default:
                return false;
        }
        return true;
    }

    @Override
    public void display() {
        boolean running = true;
        while (running) {
            ConsoleUtil.clearScreen();
            renderHeader();
            renderBody();
            renderFooter();

            int choice = ConsoleUtil.readInt("Chọn chức năng: ", 0, getMaxOption());
            if (choice == 0 || choice == 6) {
                break; // Clean exit on option 0 or 6
            }

            running = handleOption(choice);
        }
    }

    /**
     * Returns the injected ProductMenu instance.
     *
     * @return ProductMenu
     */
    public ProductMenu getProductMenu() {
        return productMenu;
    }

    /**
     * Returns the injected CustomerMenu instance.
     *
     * @return CustomerMenu
     */
    public CustomerMenu getCustomerMenu() {
        return customerMenu;
    }

    /**
     * Returns the injected SalesMenu instance.
     *
     * @return SalesMenu
     */
    public SalesMenu getSalesMenu() {
        return salesMenu;
    }

    /**
     * Returns the injected ReportMenu instance.
     *
     * @return ReportMenu
     */
    public ReportMenu getReportMenu() {
        return reportMenu;
    }

    /**
     * Returns the input scanner.
     *
     * @return Scanner
     */
    public Scanner getScanner() {
        return scanner;
    }

    /**
     * Returns the menu title string.
     *
     * @return String title
     */
    public String getTitle() {
        return title;
    }

    private static ProductMenu createDefaultProductMenu() {
        return new ProductMenu(new ProductService(new ProductRepository()));
    }

    private static CustomerMenu createDefaultCustomerMenu() {
        return new CustomerMenu(new CustomerService(new CustomerRepository()));
    }

    private static SalesMenu createDefaultSalesMenu() {
        return new SalesMenu(new OrderService());
    }

    private static ReportMenu createDefaultReportMenu() {
        return new ReportMenu(new ReportService());
    }
}
