package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.repository.OrderRepository;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;
import fptu.pro192.foodstoremanagement.service.ReportService;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import fptu.pro192.foodstoremanagement.util.TableFormatter;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Console user interface menu for generating analytical reports and performance rankings.
 * Strictly conforms to the 80-column ASCII terminal display standard and Template Method Pattern.
 */
public class ReportMenu extends AbstractMenu {

    private final ReportService reportService;
    private final Scanner scanner;

    // 80-column Best-Selling Products Ranking Table Border:
    // +------+--------+--------------------------------+------------+----------------+ (80 chars)
    private static final String PRODUCT_RANK_BORDER =
            "+------+--------+--------------------------------+------------+----------------+";

    // 80-column Highest-Spending Customers Ranking Table Border:
    // +------+--------+------------------------------+----------+--------------------+ (80 chars)
    private static final String CUSTOMER_RANK_BORDER =
            "+------+--------+------------------------------+----------+--------------------+";

    // 80-column Low Stock Inventory Table Border (Architecture Model Section 5.2.1):
    // +--------+------------------------+-----------+--------+-------------+---------+ (80 chars)
    private static final String LOW_STOCK_BORDER =
            "+--------+------------------------+-----------+--------+-------------+---------+";

    /**
     * Default constructor wiring default ReportService.
     */
    public ReportMenu() {
        this(new ReportService());
    }

    /**
     * Constructs ReportMenu with an injected ReportService.
     *
     * @param reportService The analytical reporting service
     */
    public ReportMenu(ReportService reportService) {
        this(reportService, ScannerManager.getInstance().getScanner());
    }

    /**
     * Constructs ReportMenu with full dependency injection.
     *
     * @param reportService The analytical reporting service
     * @param scanner The input scanner
     */
    public ReportMenu(ReportService reportService, Scanner scanner) {
        super("BÁO CÁO & THỐNG KÊ DOANH THU");
        if (reportService == null) {
            throw new IllegalArgumentException("ReportService cannot be null.");
        }
        this.reportService = reportService;
        this.scanner = scanner != null ? scanner : ScannerManager.getInstance().getScanner();
    }

    @Override
    protected void renderBody() {
        System.out.println("  1. Báo cáo doanh thu theo tháng (Task B15)");
        System.out.println("  2. Top sản phẩm bán chạy nhất (Task B16)");
        System.out.println("  3. Top khách hàng chi tiêu cao nhất (Task B17)");
        System.out.println("  4. Báo cáo sản phẩm tồn kho thấp (<= 5) (Task B18)");
    }

    @Override
    protected int getMaxOption() {
        return 4;
    }

    @Override
    protected boolean handleOption(int choice) {
        switch (choice) {
            case 1:
                showMonthlySalesReport();
                break;
            case 2:
                showBestSellingProducts();
                break;
            case 3:
                showHighestSpendingCustomers();
                break;
            case 4:
                showLowStockReport();
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Task B15: Prompts for month and year, displays monthly revenue and sales volume report.
     */
    private void showMonthlySalesReport() {
        System.out.println("\n--- BÁO CÁO DOANH THU THEO THÁNG (TASK B15) ---");

        LocalDate now = LocalDate.now();
        System.out.println("Nhập tháng và năm cần thống kê doanh thu:");
        int month = ConsoleUtil.readInt(String.format("Nhập tháng (1 - 12) [mặc định %d]: ", now.getMonthValue()), 1, 12);
        int year = ConsoleUtil.readInt(String.format("Nhập năm (2000 - 2100) [mặc định %d]: ", now.getYear()), 2000, 2100);

        ReportService.MonthlyReport report = reportService.getMonthlySalesReport(month, year);

        System.out.println("=" + TableFormatter.repeat('=', MENU_WIDTH - 2) + "=");
        System.out.println(TableFormatter.center(String.format("BÁO CÁO DOANH THU THÁNG %02d/%d", month, year), MENU_WIDTH));
        System.out.println("=" + TableFormatter.repeat('=', MENU_WIDTH - 2) + "=");
        System.out.printf("  - Tổng số đơn hàng hoàn tất (Orders)  : %,15d đơn\n", report.getTotalTransactions());
        System.out.printf("  - Tổng số lượng sản phẩm đã bán (Units): %,15d sản phẩm\n", report.getTotalItemsSold());
        System.out.printf("  - TỔNG DOANH THU THUẦN (Total Revenue) : %,15.0f VND\n", report.getTotalRevenue());
        System.out.println("=" + TableFormatter.repeat('=', MENU_WIDTH - 2) + "=");
    }

    /**
     * Task B16: Prompts for limit, displays best-selling food items ranked by total quantity sold.
     */
    private void showBestSellingProducts() {
        System.out.println("\n--- TOP SẢN PHẨM BÁN CHẠY NHẤT (TASK B16) ---");
        int limit = ConsoleUtil.readInt("Nhập số lượng sản phẩm Top cần xem (1 - 50) [mặc định 10]: ", 1, 50);

        List<ReportService.ProductSalesStat> stats = reportService.getBestSellingProducts(limit);
        if (stats == null || stats.isEmpty()) {
            System.out.println("Chưa có dữ liệu bán hàng nào được ghi nhận.");
            return;
        }

        System.out.println("\nBẢNG XẾP HẠNG SẢN PHẨM BÁN CHẠY (BR21):");
        System.out.println(PRODUCT_RANK_BORDER);
        System.out.printf("| %4s | %-6s | %-30s | %10s | %14s |\n",
                "Hạng", "Mã SP", "Tên Sản Phẩm", "Số Lượng", "Doanh Thu");
        System.out.println(PRODUCT_RANK_BORDER);

        for (int i = 0; i < stats.size(); i++) {
            ReportService.ProductSalesStat stat = stats.get(i);
            System.out.printf("| %4d | %-6s | %-30s | %,10d | %14s |\n",
                    i + 1,
                    TableFormatter.fit(stat.getProductId(), 6),
                    TableFormatter.fit(stat.getProductName(), 30),
                    stat.getQuantitySold(),
                    String.format("%,.0f VND", stat.getTotalRevenue()));
        }
        System.out.println(PRODUCT_RANK_BORDER);
    }

    /**
     * Task B17: Prompts for limit, displays highest-spending customers ranked by total purchase value.
     */
    private void showHighestSpendingCustomers() {
        System.out.println("\n--- TOP KHÁCH HÀNG CHI TIÊU CAO NHẤT (TASK B17) ---");
        int limit = ConsoleUtil.readInt("Nhập số lượng khách hàng Top cần xem (1 - 50) [mặc định 10]: ", 1, 50);

        List<ReportService.CustomerSpendStat> stats = reportService.getHighestSpendingCustomers(limit);
        if (stats == null || stats.isEmpty()) {
            System.out.println("Chưa có dữ liệu giao dịch khách hàng nào.");
            return;
        }

        System.out.println("\nBẢNG XẾP HẠNG KHÁCH HÀNG CHI TIÊU CAO NHẤT (BR22):");
        System.out.println(CUSTOMER_RANK_BORDER);
        System.out.printf("| %4s | %-6s | %-28s | %8s | %18s |\n",
                "Hạng", "Mã KH", "Họ Và Tên", "Số Đơn", "Tổng Chi Tiêu");
        System.out.println(CUSTOMER_RANK_BORDER);

        for (int i = 0; i < stats.size(); i++) {
            ReportService.CustomerSpendStat stat = stats.get(i);
            System.out.printf("| %4d | %-6s | %-28s | %8d | %18s |\n",
                    i + 1,
                    TableFormatter.fit(stat.getCustomerId(), 6),
                    TableFormatter.fit(stat.getCustomerName(), 28),
                    stat.getOrderCount(),
                    String.format("%,.0f VND", stat.getTotalSpent()));
        }
        System.out.println(CUSTOMER_RANK_BORDER);
    }

    /**
     * Task B18: Displays all products whose total available inventory stock is <= 5 units.
     */
    private void showLowStockReport() {
        System.out.println("\n--- BÁO CÁO SẢN PHẨM TỒN KHO THẤP (<= 5) (TASK B18) ---");
        List<Food> lowStockProducts = reportService.getLowStockReport();

        if (lowStockProducts == null || lowStockProducts.isEmpty()) {
            System.out.println("Tất cả sản phẩm đều đang đảm bảo mức tồn kho an toàn (> 5 đơn vị).");
            return;
        }

        LocalDate today = LocalDate.now();
        System.out.println("\nDANH SÁCH SẢN PHẨM CẦN NHẬP THÊM HÀNG (BR19):");
        System.out.println(LOW_STOCK_BORDER);
        System.out.printf("| %-6s | %-22s | %-9s | %-6s | %11s | %7s |\n",
                "Mã SP", "Tên Sản Phẩm", "Phân Loại", "ĐVT", "Đơn Giá", "Tồn Kho");
        System.out.println(LOW_STOCK_BORDER);

        for (Food f : lowStockProducts) {
            System.out.printf("| %-6s | %-22s | %-9s | %-6s | %11s | %7d |\n",
                    TableFormatter.fit(f.getId(), 6),
                    TableFormatter.fit(f.getName(), 22),
                    TableFormatter.fit(f.getCategory(), 9),
                    TableFormatter.fit(f.getUnit(), 6),
                    String.format("%,.0f", f.getPrice()),
                    f.getTotalStock(today));
        }
        System.out.println(LOW_STOCK_BORDER);
    }

    /**
     * Returns the underlying Scanner instance.
     *
     * @return The scanner
     */
    public Scanner getScanner() {
        return scanner;
    }

    /**
     * Returns the title of this menu.
     *
     * @return Menu title string
     */
    public String getTitle() {
        return title;
    }
}
