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
            "Daily Revenue Report",
            "Monthly Revenue Report",
            "Best-Selling Products Ranking",
            "Best-Spending Customers Ranking"
    };

    private static final String TOP_PRODUCTS_BORDER = "+------+--------+-------------------------+------------+-------+---------------+";
    private static final String CUSTOMER_SPEND_BORDER = "+------+--------+------------------------+---------+---------+-----------------+";
    private static final LocalDate CANCEL_DATE = LocalDate.MIN;

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

    /**
     * Prompts for a calendar date in dd/MM/yyyy format and displays all orders
     * and net revenue for that day in an interactive loop (BR23).
     */
    public void handleDailyRevenueReport() {
        while (true) {
            LocalDate targetDate = ConsoleUtil.readDate(
                    "Enter report date [dd/MM/yyyy] (Enter for today, 0 to cancel): ",
                    LocalDate.now());
            if (targetDate == null) {
                break;
            }
            renderDailyReportForDate(targetDate);
            printDivider();
        }
    }

    private void renderDailyReportForDate(LocalDate targetDate) {
        List<Order> dailyOrders = this.reportController.getOrdersByDate(targetDate);
        double dailyRevenue = this.reportController.getDailyRevenue(targetDate);

        System.out.println();
        System.out.println("  [INFO] Report Date      : " + InputValidator.formatDisplayDate(targetDate));
        System.out.println("  [INFO] Completed Orders : " + dailyOrders.size());
        System.out.println("  [INFO] Net Daily Revenue: " + InputValidator.formatCurrency(dailyRevenue));

        if (!dailyOrders.isEmpty()) {
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
                    "Enter report month [MM/yyyy] (Enter for this month, 0 to cancel): ",
                    YearMonth.now());
            if (ym == null) {
                break;
            }
            renderMonthlyReportForMonth(ym);
            printDivider();
        }
    }

    private void renderMonthlyReportForMonth(YearMonth ym) {
        List<Order> monthlyOrders = this.reportController.getOrdersByMonth(ym);
        double monthlyRevenue = this.reportController.getMonthlyRevenue(ym);

        System.out.printf("  [INFO] Report Period      : %02d/%04d%n", ym.getMonthValue(), ym.getYear());
        System.out.println("  [INFO] Completed Orders   : " + monthlyOrders.size());
        System.out.println("  [INFO] Net Monthly Revenue: " + InputValidator.formatCurrency(monthlyRevenue));

        if (!monthlyOrders.isEmpty()) {
            SalesMenu.renderOrdersSummaryTable(monthlyOrders);
        }
    }

    /**
     * Displays the ranked table of best-selling products by quantity sold and
     * gross revenue for an optional period in an interactive loop (BR21, BR27).
     */
    public void handleBestSellingProductsReport() {
        while (true) {
            LocalDate fromDate = promptScopeDate(
                    "Enter from date [dd/MM/yyyy] (Enter for all-time, 0 to cancel): ");
            if (fromDate == CANCEL_DATE) {
                break;
            }
            LocalDate toDate = promptScopeEndDate(fromDate);
            if (toDate == CANCEL_DATE) {
                System.out.println("  [INFO] Operation cancelled.");
                printDivider();
                continue;
            }
            Integer limit = ConsoleUtil.readInt(
                    "Enter top N limit [N] (Enter for all-products, 0 to cancel): ",
                    1, Integer.MAX_VALUE, Integer.MAX_VALUE);
            if (limit == null) {
                System.out.println("  [INFO] Operation cancelled.");
                printDivider();
                continue;
            }
            List<ProductSalesSummary> rankings = this.reportController.getTopSellingProducts(fromDate, toDate, limit);
            renderTopProductsTable(rankings, fromDate, toDate);
            printDivider();
        }
    }

    private void renderTopProductsTable(List<ProductSalesSummary> rankings, LocalDate fromDate, LocalDate toDate) {
        if (rankings.isEmpty()) {
            System.out.println("  [INFO] No completed product sales recorded for this period.");
            return;
        }
        System.out.println("  [INFO] Period: " + formatPeriodLabel(fromDate, toDate));
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
            String revStr = InputValidator.formatCurrency(item.getGrossRevenue());
            System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                    TableFormatter.fitRight("#" + (i + 1), 4),
                    TableFormatter.fit(item.getProductId(), 6),
                    TableFormatter.fit(item.getProductName(), 23),
                    TableFormatter.fit(item.getCategory(), 10),
                    TableFormatter.fitRight(String.valueOf(item.getQuantitySold()), 5),
                    TableFormatter.fitRight(revStr, 13));
        }
        System.out.println(TOP_PRODUCTS_BORDER);
        System.out.printf("  Total products listed: %d%n", rankings.size());
    }

    /**
     * Displays the ranked table of registered customers by cumulative net
     * spending for an optional period in an interactive loop (BR22, BR27).
     */
    public void handleCustomerSpendingReport() {
        while (true) {
            LocalDate fromDate = promptScopeDate(
                    "Enter from date [dd/MM/yyyy] (Enter for all-time, 0 to cancel): ");
            if (fromDate == CANCEL_DATE) {
                break;
            }
            LocalDate toDate = promptScopeEndDate(fromDate);
            if (toDate == CANCEL_DATE) {
                System.out.println("  [INFO] Operation cancelled.");
                printDivider();
                continue;
            }
            Integer limit = ConsoleUtil.readInt(
                    "Enter top N limit [N] (Enter for all-customers, 0 to cancel): ",
                    1, Integer.MAX_VALUE, Integer.MAX_VALUE);
            if (limit == null) {
                System.out.println("  [INFO] Operation cancelled.");
                printDivider();
                continue;
            }
            List<CustomerSpendingSummary> summaries = this.reportController.getHighestSpendingCustomers(
                    fromDate, toDate, limit);
            renderTopCustomersTable(summaries, fromDate, toDate);
            printDivider();
        }
    }

    private void renderTopCustomersTable(List<CustomerSpendingSummary> summaries,
            LocalDate fromDate, LocalDate toDate) {
        if (summaries.isEmpty()) {
            System.out.println("  [INFO] No registered customer orders recorded for this period.");
            return;
        }
        System.out.println("  [INFO] Period: " + formatPeriodLabel(fromDate, toDate));
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
        System.out.printf("  Total customers listed: %d%n", summaries.size());
    }

    private LocalDate promptScopeDate(String prompt) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(prompt);
            if ("0".equals(raw)) {
                return CANCEL_DATE;
            }
            if (raw.isEmpty()) {
                return null;
            }
            try {
                return InputValidator.parseDisplayDate(raw, "From date");
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private LocalDate promptScopeEndDate(LocalDate fromDate) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(
                    "Enter to date [dd/MM/yyyy] (Enter for all-time, 0 to cancel): ");
            if ("0".equals(raw)) {
                return CANCEL_DATE;
            }
            if (raw.isEmpty()) {
                return null;
            }
            try {
                LocalDate toDate = InputValidator.parseDisplayDate(raw, "To date");
                if (fromDate != null && toDate.isBefore(fromDate)) {
                    System.out.println("  [ERROR] End date cannot be before start date ("
                            + InputValidator.formatDisplayDate(fromDate) + ").");
                    continue;
                }
                return toDate;
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
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
