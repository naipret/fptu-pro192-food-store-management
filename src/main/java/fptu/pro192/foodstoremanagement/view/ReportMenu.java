package fptu.pro192.foodstoremanagement.view;

import fptu.pro192.foodstoremanagement.controller.ReportController;
import fptu.pro192.foodstoremanagement.controller.ReportController.CustomerSpendingSummary;
import fptu.pro192.foodstoremanagement.controller.ReportController.ProductSalesSummary;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import fptu.pro192.foodstoremanagement.util.TableFormatter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

/**
 * Console view for Analytical and Revenue Reports: daily and monthly sales
 * summaries, best-selling product rankings, and customer spending rankings
 * (BR21, BR22, BR23, BR27).
 */
public class ReportMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "Daily Sales & Revenue Report",
            "Monthly Sales & Revenue Report",
            "Top-Selling Products Ranking",
            "Top Customers by Spending"
    };

    private static final String TOP_PRODUCTS_BORDER = "+------+--------+-------------------------+------------+-------+---------------+";
    private static final String CUSTOMER_SPEND_BORDER = "+------+--------+------------------------+---------+---------+-----------------+";

    private final ReportController reportController;

    /**
     * Constructs a ReportMenu bound to the shared ReportController instance.
     *
     * @param reportController the shared ReportController
     * @throws IllegalArgumentException if reportController is null
     */
    public ReportMenu(ReportController reportController) {
        super("FOOD STORE MANAGEMENT SYSTEM > [5] REPORTS", MENU_OPTIONS);
        if (reportController == null) {
            throw new IllegalArgumentException("ReportController cannot be null.");
        }
        this.reportController = reportController;
    }

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
            default:
                System.out.println("  [ERROR] Unsupported menu choice: " + choice);
                break;
        }
    }

    @Override
    protected boolean shouldPauseAfterAction(int choice) {
        return false;
    }

    /**
     * Prompts for a calendar date in dd/MM/yyyy format and displays all orders
     * and net revenue for that day in an interactive loop (BR23).
     */
    public void handleDailyRevenueReport() {
        while (true) {
            LocalDate targetDate = ConsoleUtil.readDateWithDefault(
                    "\nEnter report date (dd/MM/yyyy, [Enter] for today, 0 to back): ",
                    LocalDate.now());
            if (targetDate == null) {
                break;
            }
            renderDailyReportForDate(targetDate);
        }
    }

    private void renderDailyReportForDate(LocalDate targetDate) {
        List<Order> dailyOrders = this.reportController.getOrdersByDate(targetDate);
        double dailyRevenue = this.reportController.getDailyRevenue(targetDate);

        System.out.println();
        System.out.println("  Report Date       : " + InputValidator.formatDisplayDate(targetDate));
        System.out.println("  Completed Orders  : " + dailyOrders.size());
        System.out.println("  Net Daily Revenue : " + InputValidator.formatCurrency(dailyRevenue) + " VND");

        if (dailyOrders.isEmpty()) {
            System.out.println("  [INFO] No completed orders on this date.");
        } else {
            SalesMenu.renderOrdersSummaryTable(dailyOrders);
        }
    }

    /**
     * Prompts for a month and year in MM/yyyy format and displays the monthly
     * sales and revenue report in an interactive loop (BR23).
     */
    public void handleMonthlyRevenueReport() {
        while (true) {
            YearMonth ym = ConsoleUtil.readMonthWithDefault(
                    "\nEnter report month (MM/yyyy, [Enter] for current month, 0 to back): ",
                    YearMonth.now());
            if (ym == null) {
                break;
            }
            renderMonthlyReportForMonth(ym);
        }
    }

    private void renderMonthlyReportForMonth(YearMonth ym) {
        List<Order> monthlyOrders = this.reportController.getOrdersByMonth(ym);
        double monthlyRevenue = this.reportController.getMonthlyRevenue(ym);

        System.out.println();
        System.out.printf("  Report Period       : %02d/%04d%n", ym.getMonthValue(), ym.getYear());
        System.out.println("  Completed Orders    : " + monthlyOrders.size());
        System.out.println("  Net Monthly Revenue : " + InputValidator.formatCurrency(monthlyRevenue) + " VND");

        if (monthlyOrders.isEmpty()) {
            System.out.println("  [INFO] No completed orders in this month.");
        } else {
            SalesMenu.renderOrdersSummaryTable(monthlyOrders);
        }
    }

    /**
     * Displays the ranked table of best-selling products by quantity sold and
     * gross revenue for an optional period in an interactive loop (BR21, BR27).
     */
    public void handleBestSellingProductsReport() {
        while (true) {
            boolean proceed = generateTopProductsReport();
            if (!proceed) {
                break;
            }
            if (!ConsoleUtil.confirmWithDefault("\nGenerate another product ranking report?", true)) {
                break;
            }
        }
    }

    private boolean generateTopProductsReport() {
        LocalDate fromDate = promptScopeDate("\nEnter start date (dd/MM/yyyy, [Enter] for all-time, 0 to back): ");
        if (fromDate == null) {
            return false;
        }
        LocalDate toDate = promptScopeEndDate(fromDate);
        if (toDate == null) {
            return false;
        }
        Integer limit = ConsoleUtil.readIntWithDefault(
                "Enter top N limit [Enter for default 5, 0 to back]: ", 1, Integer.MAX_VALUE, 5);
        if (limit == null) {
            return false;
        }
        List<ProductSalesSummary> rankings = this.reportController.getTopSellingProducts(fromDate, toDate, limit);
        renderTopProductsTable(rankings, fromDate, toDate);
        return true;
    }

    private void renderTopProductsTable(List<ProductSalesSummary> rankings, LocalDate fromDate, LocalDate toDate) {
        if (rankings.isEmpty()) {
            System.out.println("  [INFO] No completed product sales recorded for this period.");
            return;
        }
        System.out.println("  Period: " + formatPeriodLabel(fromDate, toDate));
        System.out.println(TOP_PRODUCTS_BORDER);
        System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                TableFormatter.fit("Rank", 4),
                TableFormatter.fit("ProdID", 6),
                TableFormatter.fit("Product Name", 23),
                TableFormatter.fit("Category", 10),
                TableFormatter.fitRight("Sold", 5),
                TableFormatter.fitRight("Gross", 13));
        System.out.println(TOP_PRODUCTS_BORDER);
        for (int i = 0; i < rankings.size(); i++) {
            ProductSalesSummary item = rankings.get(i);
            String revStr = String.format(Locale.US, "%,.0f", item.getGrossRevenue());
            System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                    TableFormatter.fitRight("#" + (i + 1), 4),
                    TableFormatter.fit(item.getProductId(), 6),
                    TableFormatter.fit(item.getProductName(), 23),
                    TableFormatter.fit(item.getCategory(), 10),
                    TableFormatter.fitRight(String.valueOf(item.getQuantitySold()), 5),
                    TableFormatter.fitRight(revStr, 13));
        }
        System.out.println(TOP_PRODUCTS_BORDER);
    }

    /**
     * Displays the ranked table of registered customers by cumulative net
     * spending for an optional period in an interactive loop (BR22, BR27).
     */
    public void handleCustomerSpendingReport() {
        while (true) {
            boolean proceed = generateTopCustomersReport();
            if (!proceed) {
                break;
            }
            if (!ConsoleUtil.confirmWithDefault("\nGenerate another customer ranking report?", true)) {
                break;
            }
        }
    }

    private boolean generateTopCustomersReport() {
        LocalDate fromDate = promptScopeDate("\nEnter start date (dd/MM/yyyy, [Enter] for all-time, 0 to back): ");
        if (fromDate == null) {
            return false;
        }
        LocalDate toDate = promptScopeEndDate(fromDate);
        if (toDate == null) {
            return false;
        }
        Integer limit = ConsoleUtil.readIntWithDefault(
                "Enter top N limit [Enter for default 5, 0 to back]: ", 1, Integer.MAX_VALUE, 5);
        if (limit == null) {
            return false;
        }
        List<CustomerSpendingSummary> summaries = this.reportController.getHighestSpendingCustomers(fromDate, toDate,
                limit);
        renderTopCustomersTable(summaries, fromDate, toDate);
        return true;
    }

    private void renderTopCustomersTable(List<CustomerSpendingSummary> summaries,
            LocalDate fromDate, LocalDate toDate) {
        if (summaries.isEmpty()) {
            System.out.println("  [INFO] No registered customer orders recorded for this period.");
            return;
        }
        System.out.println("  Period: " + formatPeriodLabel(fromDate, toDate));
        System.out.println(CUSTOMER_SPEND_BORDER);
        System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                TableFormatter.fit("Rank", 4),
                TableFormatter.fit("CustID", 6),
                TableFormatter.fit("Customer Name", 22),
                TableFormatter.fit("Tier", 7),
                TableFormatter.fitRight("Orders", 7),
                TableFormatter.fitRight("Total Spent", 15));
        System.out.println(CUSTOMER_SPEND_BORDER);
        for (int i = 0; i < summaries.size(); i++) {
            CustomerSpendingSummary item = summaries.get(i);
            String spentStr = InputValidator.formatCurrency(item.getTotalSpent());
            System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                    TableFormatter.fitRight("#" + (i + 1), 4),
                    TableFormatter.fit(item.getCustomerId(), 6),
                    TableFormatter.fit(item.getCustomerName(), 22),
                    TableFormatter.fit(item.getCustomerType(), 7),
                    TableFormatter.fitRight(String.valueOf(item.getOrderCount()), 7),
                    TableFormatter.fitRight(spentStr, 15));
        }
        System.out.println(CUSTOMER_SPEND_BORDER);
    }

    private LocalDate promptScopeDate(String prompt) {
        LocalDate val = ConsoleUtil.readDateWithDefault(prompt, LocalDate.MIN);
        if (val == null) {
            return null;
        }
        return (val == LocalDate.MIN) ? null : val;
    }

    private LocalDate promptScopeEndDate(LocalDate fromDate) {
        while (true) {
            LocalDate val = ConsoleUtil.readDateWithDefault(
                    "Enter end date (dd/MM/yyyy, [Enter] for all-time, 0 to back): ", LocalDate.MAX);
            if (val == null) {
                return null;
            }
            LocalDate toDate = (val == LocalDate.MAX) ? null : val;
            if (fromDate == null || toDate == null || !toDate.isBefore(fromDate)) {
                return toDate;
            }
            System.out.println("  [ERROR] End date cannot be before start date ("
                    + InputValidator.formatDisplayDate(fromDate) + ").");
        }
    }

    private String formatPeriodLabel(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return InputValidator.formatDisplayDate(fromDate) + " to "
                    + InputValidator.formatDisplayDate(toDate);
        } else if (fromDate != null) {
            return "From " + InputValidator.formatDisplayDate(fromDate) + " onwards";
        } else if (toDate != null) {
            return "Up to " + InputValidator.formatDisplayDate(toDate);
        }
        return "All-time";
    }
}
