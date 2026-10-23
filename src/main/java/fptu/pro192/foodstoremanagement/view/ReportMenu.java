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
 * (BR18, BR21, BR22, BR23, BR27).
 */
public class ReportMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "Daily Revenue Report (by Date)",
            "Monthly Sales & Revenue Report (by Month/Year)",
            "Best-Selling Products Ranking",
            "Customer Spending Summary Ranking"
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
        LocalDate targetDate = ConsoleUtil.readDate(
                "Enter report date (dd/MM/yyyy, or 0 to cancel): ");
        if (targetDate == null) {
            System.out.println("  [INFO] Operation cancelled. Returning to menu.");
            return;
        }
        List<Order> dailyOrders = this.reportController.getOrdersByDate(targetDate);
        double dailyRevenue = this.reportController.getDailyRevenue(targetDate);

        System.out.println();
        System.out.println("  [INFO] Report Date      : "
                + InputValidator.formatDisplayDate(targetDate));
        System.out.println("  [INFO] Completed Orders : " + dailyOrders.size());
        System.out.println("  [INFO] Net Daily Revenue: "
                + InputValidator.formatCurrency(dailyRevenue));

        if (!dailyOrders.isEmpty()) {
            SalesMenu.renderOrdersSummaryTable(dailyOrders);
        }
    }

    /**
     * Prompts for a month (1-12) and four-digit year and displays the monthly
     * sales and revenue report (BR21).
     */
    public void handleMonthlyRevenueReport() {
        int month = ConsoleUtil.readInt("Enter month [1-12] (or 0 to cancel): ", 0, 12);
        if (month == 0) {
            System.out.println("  [INFO] Operation cancelled. Returning to menu.");
            return;
        }
        int year;
        while (true) {
            String raw = ConsoleUtil.readOptionalString("Enter year [2000-2100] (or 0 to cancel): ");
            if ("0".equals(raw)) {
                System.out.println("  [INFO] Operation cancelled. Returning to menu.");
                return;
            }
            try {
                int val = Integer.parseInt(raw);
                if (val >= 2000 && val <= 2100) {
                    year = val;
                    break;
                }
                System.out.println("  [ERROR] Value must be between 2000 and 2100.");
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid number. Please enter an integer.");
            }
        }
        YearMonth ym = YearMonth.of(year, month);
        List<Order> monthlyOrders = this.reportController.getOrdersByMonth(ym);
        double monthlyRevenue = this.reportController.getMonthlyRevenue(ym);

        System.out.println();
        System.out.printf("  [INFO] Report Period       : %02d/%04d%n", month, year);
        System.out.println("  [INFO] Completed Orders    : " + monthlyOrders.size());
        System.out.println("  [INFO] Net Monthly Revenue : "
                + InputValidator.formatCurrency(monthlyRevenue));

        if (!monthlyOrders.isEmpty()) {
            SalesMenu.renderOrdersSummaryTable(monthlyOrders);
        }
    }

    /**
     * Displays the ranked table of best-selling products by quantity sold and
     * gross revenue for an optional period (BR21, BR27).
     */
    public void handleBestSellingProductsReport() {
        LocalDate fromDate = promptOptionalFilterDate(
                "Enter from date (dd/MM/yyyy, Enter for all-time, or 0 to cancel): ");
        if (fromDate == CANCEL_DATE) {
            System.out.println("  [INFO] Operation cancelled. Returning to menu.");
            return;
        }
        LocalDate toDate = null;
        if (fromDate != null) {
            while (true) {
                toDate = promptOptionalFilterDate(
                        "Enter to date (dd/MM/yyyy, Enter for no end limit, or 0 to cancel): ");
                if (toDate == CANCEL_DATE) {
                    System.out.println("  [INFO] Operation cancelled. Returning to menu.");
                    return;
                }
                if (toDate == null || !toDate.isBefore(fromDate)) {
                    break;
                }
                System.out.println("  [ERROR] To date cannot be before from date ("
                        + InputValidator.formatDisplayDate(fromDate) + ").");
            }
        } else {
            toDate = promptOptionalFilterDate(
                    "Enter to date (dd/MM/yyyy, Enter for all-time, or 0 to cancel): ");
            if (toDate == CANCEL_DATE) {
                System.out.println("  [INFO] Operation cancelled. Returning to menu.");
                return;
            }
        }
        int limit = promptTopNLimit(
                "Enter top N limit (N, or Enter for all-product): ");
        List<ProductSalesSummary> rankings = this.reportController.getTopSellingProducts(fromDate, toDate, limit);
        if (rankings.isEmpty()) {
            System.out.println("  [INFO] No completed product sales recorded for this period.");
            return;
        }

        String periodLabel = formatPeriodLabel(fromDate, toDate);
        System.out.println("  [INFO] Period: " + periodLabel);
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
     * spending for an optional period (BR22, BR27).
     */
    public void handleCustomerSpendingReport() {
        LocalDate fromDate = promptOptionalFilterDate(
                "Enter from date (dd/MM/yyyy, Enter for all-time, or 0 to cancel): ");
        if (fromDate == CANCEL_DATE) {
            System.out.println("  [INFO] Operation cancelled. Returning to menu.");
            return;
        }
        LocalDate toDate = null;
        if (fromDate != null) {
            while (true) {
                toDate = promptOptionalFilterDate(
                        "Enter to date (dd/MM/yyyy, Enter for no end limit, or 0 to cancel): ");
                if (toDate == CANCEL_DATE) {
                    System.out.println("  [INFO] Operation cancelled. Returning to menu.");
                    return;
                }
                if (toDate == null || !toDate.isBefore(fromDate)) {
                    break;
                }
                System.out.println("  [ERROR] To date cannot be before from date ("
                        + InputValidator.formatDisplayDate(fromDate) + ").");
            }
        } else {
            toDate = promptOptionalFilterDate(
                    "Enter to date (dd/MM/yyyy, Enter for all-time, or 0 to cancel): ");
            if (toDate == CANCEL_DATE) {
                System.out.println("  [INFO] Operation cancelled. Returning to menu.");
                return;
            }
        }
        int limit = promptTopNLimit(
                "Enter top N limit (N, or Enter for all-customers): ");
        List<CustomerSpendingSummary> summaries = this.reportController.getHighestSpendingCustomers(fromDate, toDate,
                limit);
        if (summaries.isEmpty()) {
            System.out.println("  [INFO] No registered customer orders recorded for this period.");
            return;
        }

        String periodLabel = formatPeriodLabel(fromDate, toDate);
        System.out.println("  [INFO] Period: " + periodLabel);
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

    private LocalDate promptOptionalFilterDate(String prompt) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(prompt);
            if ("0".equals(raw)) {
                return CANCEL_DATE;
            }
            if (raw.isEmpty()) {
                return null;
            }
            try {
                return InputValidator.parseDisplayDate(raw, "Date");
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private int promptTopNLimit(String prompt) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(prompt);
            if (raw.isEmpty()) {
                return 0;
            }
            try {
                int val = Integer.parseInt(raw);
                if (val >= 0) {
                    return val;
                }
                System.out.println("  [ERROR] Limit must be greater than or equal to 0.");
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid number. Please enter an integer or press Enter.");
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
