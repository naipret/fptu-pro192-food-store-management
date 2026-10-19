package fptu.pro192.foodstoremanagement.view;

import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.controller.ReportController;
import fptu.pro192.foodstoremanagement.controller.ReportController.CustomerSpendingSummary;
import fptu.pro192.foodstoremanagement.controller.ReportController.MonthlySalesSummary;
import fptu.pro192.foodstoremanagement.controller.ReportController.ProductSalesSummary;
import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import fptu.pro192.foodstoremanagement.util.TableFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Console view for Analytical and Revenue Reports: daily and monthly sales
 * summaries, best-selling product rankings, customer spending rankings, and
 * inventory health alerts (low stock, near-expiry, and expired batches)
 * (BR11, BR18, BR19, BR20, BR21, BR22, BR23, BR27).
 */
public class ReportMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
        "Daily Revenue Report (by Date)",
        "Monthly Sales & Revenue Report (by Month/Year)",
        "Best-Selling Products Ranking",
        "Customer Spending Summary Ranking",
        "Inventory Health Alerts (Low Stock & Expiry)"
    };

    private static final String TOP_PRODUCTS_BORDER =
            "+------+--------+------------------------+------------+-------+--------------+";
    private static final String CUSTOMER_SPEND_BORDER =
            "+------+--------+------------------------+---------+--------+----------------+";
    private static final String LOW_STOCK_BORDER =
            "+--------+------------------------+--------------+--------+------------+------+";
    private static final String BATCH_ALERT_BORDER =
            "+--------+--------+------------------+------------+------------+-------+--------+";

    private final ReportController reportController;

    /**
     * Constructs a ReportMenu bound to the shared ReportController instance.
     *
     * @param reportController the shared ReportController
     * @throws IllegalArgumentException if reportController is null
     */
    public ReportMenu(ReportController reportController) {
        super("ANALYTICAL & REVENUE REPORTS", MENU_OPTIONS);
        if (reportController == null) {
            throw new IllegalArgumentException("ReportController cannot be null.");
        }
        this.reportController = reportController;
    }

    /**
     * Dispatches the user's selected report menu option.
     *
     * @param choice the selected 1-based option index
     */
    @Override
    protected void handleChoice(int choice) {
        switch (choice) {
            case 1:
                handleDailyRevenueReport();
                break;
            case 2:
                handleMonthlyRevenueReport();
                break;
            case 3:
                handleBestSellingProductsReport();
                break;
            case 4:
                handleCustomerSpendingReport();
                break;
            case 5:
                handleInventoryHealthAlerts();
                break;
            default:
                System.out.println("  [ERROR] Unsupported menu choice: " + choice);
                break;
        }
    }

    /**
     * Prompts for a calendar date in dd/MM/yyyy format and displays all orders
     * and net revenue for that day (BR21).
     */
    public void handleDailyRevenueReport() {
        printSectionHeader("DAILY REVENUE REPORT");
        LocalDate targetDate = ConsoleUtil.readDate(
                "Enter report date (dd/MM/yyyy, e.g., 19/10/2026): ");
        List<Order> dailyOrders = this.reportController.getOrdersByDate(targetDate);
        double dailyRevenue = this.reportController.getDailyRevenue(targetDate);

        System.out.println();
        System.out.println("  [INFO] Report Date      : "
                + InputValidator.formatDisplayDate(targetDate));
        System.out.println("  [INFO] Completed Orders : " + dailyOrders.size());
        System.out.println("  [INFO] Net Daily Revenue: "
                + InputValidator.formatCurrency(dailyRevenue) + " VND");

        if (!dailyOrders.isEmpty()) {
            SalesMenu helperView = new SalesMenu(
                    this.reportController.getOrderController(),
                    this.reportController.getProductController(),
                    this.reportController.getCustomerController());
            helperView.renderOrdersSummaryTable(dailyOrders);
        }
    }

    /**
     * Prompts for a month (1-12) and four-digit year and displays the monthly
     * sales summary (BR21).
     */
    public void handleMonthlyRevenueReport() {
        printSectionHeader("MONTHLY SALES & REVENUE REPORT");
        int month = ConsoleUtil.readInt("Enter month [1-12] (or 0 to cancel): ", 0, 12);
        if (month == 0) {
            System.out.println("  [INFO] Operation cancelled. Returning to menu.");
            return;
        }
        int year = ConsoleUtil.readInt("Enter year [2000-2100]: ", 2000, 2100);
        MonthlySalesSummary summary = this.reportController.getMonthlySalesReport(month, year);

        System.out.println("+----------------------------------------------------------------------------+");
        System.out.printf("| Monthly Summary for %02d/%04d                                                |%n",
                summary.getMonth(), summary.getYear());
        System.out.println("+----------------------------------------------------------------------------+");
        System.out.printf("| Completed Orders   : %53d |%n", summary.getTotalTransactions());
        System.out.printf("| Total Units Sold   : %53d |%n", summary.getTotalItemsSold());
        System.out.printf("| Gross Sales Amount : %49s VND |%n",
                InputValidator.formatCurrency(summary.getGrossAmount()));
        System.out.printf("| Total VIP Discount : %49s VND |%n",
                InputValidator.formatCurrency(summary.getTotalDiscount()));
        System.out.printf("| Net Monthly Revenue: %49s VND |%n",
                InputValidator.formatCurrency(summary.getTotalRevenue()));
        System.out.println("+----------------------------------------------------------------------------+");
    }

    /**
     * Displays the ranked table of best-selling products by quantity sold and
     * gross revenue (BR22, BR27).
     */
    public void handleBestSellingProductsReport() {
        printSectionHeader("BEST-SELLING PRODUCTS RANKING");
        int limit = ConsoleUtil.readInt(
                "Enter top N limit (0 for all sold products): ", 0, 1000);
        List<ProductSalesSummary> rankings = this.reportController.getTopSellingProducts(limit);
        if (rankings.isEmpty()) {
            System.out.println("  [INFO] No completed product sales recorded yet.");
            return;
        }

        System.out.println(TOP_PRODUCTS_BORDER);
        System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                TableFormatter.fit("Rank", 4),
                TableFormatter.fit("ProdID", 6),
                TableFormatter.fit("Product Name", 22),
                TableFormatter.fit("Category", 10),
                TableFormatter.fitRight("Sold", 5),
                TableFormatter.fitRight("Gross(VND)", 12));
        System.out.println(TOP_PRODUCTS_BORDER);

        for (int i = 0; i < rankings.size(); i++) {
            ProductSalesSummary item = rankings.get(i);
            String revStr = String.format(Locale.US, "%,.0f", item.getGrossRevenue());
            System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                    TableFormatter.fitRight("#" + (i + 1), 4),
                    TableFormatter.fit(item.getProductId(), 6),
                    TableFormatter.fit(item.getProductName(), 22),
                    TableFormatter.fit(item.getCategory(), 10),
                    TableFormatter.fitRight(String.valueOf(item.getQuantitySold()), 5),
                    TableFormatter.fitRight(revStr, 12));
        }
        System.out.println(TOP_PRODUCTS_BORDER);
    }

    /**
     * Displays the ranked table of registered customers by cumulative net
     * spending (BR23, BR27).
     */
    public void handleCustomerSpendingReport() {
        printSectionHeader("CUSTOMER SPENDING SUMMARY RANKING");
        List<CustomerSpendingSummary> summaries = this.reportController.getCustomerSpendingSummary();
        if (summaries.isEmpty()) {
            System.out.println("  [INFO] No registered customer orders recorded yet.");
            return;
        }

        System.out.println(CUSTOMER_SPEND_BORDER);
        System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                TableFormatter.fit("Rank", 4),
                TableFormatter.fit("CustID", 6),
                TableFormatter.fit("Customer Name", 22),
                TableFormatter.fit("Tier", 7),
                TableFormatter.fitRight("Orders", 6),
                TableFormatter.fitRight("Total Spent", 14));
        System.out.println(CUSTOMER_SPEND_BORDER);

        for (int i = 0; i < summaries.size(); i++) {
            CustomerSpendingSummary item = summaries.get(i);
            String spentStr = InputValidator.formatCurrency(item.getTotalSpent());
            System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                    TableFormatter.fitRight("#" + (i + 1), 4),
                    TableFormatter.fit(item.getCustomerId(), 6),
                    TableFormatter.fit(item.getCustomerName(), 22),
                    TableFormatter.fit(item.getCustomerType(), 7),
                    TableFormatter.fitRight(String.valueOf(item.getOrderCount()), 6),
                    TableFormatter.fitRight(spentStr, 14));
        }
        System.out.println(CUSTOMER_SPEND_BORDER);
    }

    /**
     * Displays low-stock products (available stock at or below 5), near-expiry batches
     * (within 7 days), and expired batches still on shelves (BR11, BR19, BR20).
     */
    public void handleInventoryHealthAlerts() {
        printSectionHeader("INVENTORY HEALTH ALERTS (LOW STOCK & EXPIRY)");
        LocalDate today = LocalDate.now();
        List<Product> lowStock = this.reportController.getLowStockProducts(
                Product.DEFAULT_LOW_STOCK_THRESHOLD);
        List<Batch> nearExpiry = this.reportController.getNearExpiryBatches(
                Product.DEFAULT_NEAR_EXPIRY_DAYS);
        List<Batch> expired = this.reportController.getExpiredBatches();

        System.out.println("1. LOW-STOCK PRODUCTS (Available Stock <= "
                + Product.DEFAULT_LOW_STOCK_THRESHOLD + "):");
        if (lowStock.isEmpty()) {
            System.out.println("  [OK] All active products have healthy stock levels.");
        } else {
            System.out.println(LOW_STOCK_BORDER);
            System.out.printf("| %s | %s | %s | %s | %s |%s|%n",
                    TableFormatter.fit("ID", 6),
                    TableFormatter.fit("Product Name", 22),
                    TableFormatter.fit("Category", 12),
                    TableFormatter.fit("Unit", 6),
                    TableFormatter.fitRight("Price(VND)", 10),
                    TableFormatter.fitRight("Stock ", 6));
            System.out.println(LOW_STOCK_BORDER);
            for (Product p : lowStock) {
                String priceStr = String.format(Locale.US, "%,.0f", p.getPrice());
                String stockStr = String.valueOf(p.getAvailableStock(today)) + " ";
                System.out.printf("| %s | %s | %s | %s | %s |%s|%n",
                        TableFormatter.fit(p.getId(), 6),
                        TableFormatter.fit(p.getName(), 22),
                        TableFormatter.fit(p.getCategory(), 12),
                        TableFormatter.fit(p.getUnit(), 6),
                        TableFormatter.fitRight(priceStr, 10),
                        TableFormatter.fitRight(stockStr, 6));
            }
            System.out.println(LOW_STOCK_BORDER);
        }

        System.out.println();
        System.out.println("2. NEAR-EXPIRY BATCHES (Expiring within "
                + Product.DEFAULT_NEAR_EXPIRY_DAYS + " days):");
        renderBatchAlertTable(nearExpiry, today);

        System.out.println();
        System.out.println("3. EXPIRED BATCHES STILL IN STOCK (Blocked from sale):");
        renderBatchAlertTable(expired, today);
    }

    private void renderBatchAlertTable(List<Batch> batches, LocalDate today) {
        if (batches == null || batches.isEmpty()) {
            System.out.println("  [OK] None found.");
            return;
        }
        ProductController pc = this.reportController.getProductController();
        System.out.println(BATCH_ALERT_BORDER);
        System.out.printf("| %s | %s | %s | %s | %s | %s | %s |%n",
                TableFormatter.fit("Batch", 6),
                TableFormatter.fit("ProdID", 6),
                TableFormatter.fit("Product Name", 16),
                TableFormatter.fit("Prod Date", 10),
                TableFormatter.fit("Exp Date", 10),
                TableFormatter.fitRight("Qty", 5),
                TableFormatter.fit("Status", 6));
        System.out.println(BATCH_ALERT_BORDER);
        for (Batch b : batches) {
            Product p = pc.findByIdIncludeDeleted(b.getProductId());
            String productName = (p != null) ? p.getName() : "Unknown";
            String status;
            if (b.getQuantity() == 0) {
                status = "EMPTY";
            } else if (b.isExpired(today)) {
                status = "EXPRD";
            } else if (b.isNearExpiry(today, Product.DEFAULT_NEAR_EXPIRY_DAYS)) {
                status = "NEAR";
            } else {
                status = "ACTIVE";
            }
            System.out.printf("| %s | %s | %s | %s | %s | %s | %s |%n",
                    TableFormatter.fit(b.getBatchId(), 6),
                    TableFormatter.fit(b.getProductId(), 6),
                    TableFormatter.fit(productName, 16),
                    TableFormatter.fit(InputValidator.formatDisplayDate(b.getProductionDate()), 10),
                    TableFormatter.fit(InputValidator.formatDisplayDate(b.getExpirationDate()), 10),
                    TableFormatter.fitRight(String.valueOf(b.getQuantity()), 5),
                    TableFormatter.fit(status, 6));
        }
        System.out.println(BATCH_ALERT_BORDER);
    }
}
