package fptu.pro192.foodstoremanagement.controller;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.Product;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Controller generating analytical reports across orders, products, batches,
 * and customers: daily and monthly revenue summaries, best-selling product
 * rankings, customer spending summaries, and inventory health alerts
 * (BR11, BR18, BR19, BR20, BR21, BR22, BR23, BR27).
 */
public class ReportController {

    private final OrderController orderController;
    private final ProductController productController;
    private final CustomerController customerController;

    /**
     * Constructs a ReportController with explicitly injected domain controllers.
     *
     * @param orderController the shared OrderController instance
     * @param productController the shared ProductController instance
     * @param customerController the shared CustomerController instance
     * @throws IllegalArgumentException if any controller argument is null
     */
    public ReportController(OrderController orderController,
            ProductController productController,
            CustomerController customerController) {
        if (orderController == null) {
            throw new IllegalArgumentException("OrderController cannot be null.");
        }
        if (productController == null) {
            throw new IllegalArgumentException("ProductController cannot be null.");
        }
        if (customerController == null) {
            throw new IllegalArgumentException("CustomerController cannot be null.");
        }
        this.orderController = orderController;
        this.productController = productController;
        this.customerController = customerController;
    }

    /**
     * Returns all completed orders whose order date matches the specified calendar date (BR21).
     *
     * @param date the target calendar date
     * @return an unmodifiable list of orders placed on date
     * @throws IllegalArgumentException if date is null
     */
    public List<Order> getOrdersByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Report date cannot be null.");
        }
        List<Order> matches = new ArrayList<>();
        for (Order order : this.orderController.getAllOrders()) {
            if (order.getOrderDate().toLocalDate().equals(date)) {
                matches.add(order);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    /**
     * Calculates the total net revenue (sum of finalAmount) for a specific calendar date (BR21).
     *
     * @param date the target calendar date
     * @return the net revenue in VND for that date
     */
    public double getDailyRevenue(LocalDate date) {
        double total = 0.0;
        for (Order order : getOrdersByDate(date)) {
            total += order.getFinalAmount();
        }
        return total;
    }

    /**
     * Returns all completed orders whose order date falls within the specified YearMonth (BR21).
     *
     * @param month the target YearMonth
     * @return an unmodifiable list of orders placed in that month
     * @throws IllegalArgumentException if month is null
     */
    public List<Order> getOrdersByMonth(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("Report month cannot be null.");
        }
        List<Order> matches = new ArrayList<>();
        for (Order order : this.orderController.getAllOrders()) {
            if (YearMonth.from(order.getOrderDate()).equals(month)) {
                matches.add(order);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    /**
     * Calculates the total net revenue (sum of finalAmount) for a specific YearMonth (BR21).
     *
     * @param month the target YearMonth
     * @return the net revenue in VND for that month
     */
    public double getMonthlyRevenue(YearMonth month) {
        double total = 0.0;
        for (Order order : getOrdersByMonth(month)) {
            total += order.getFinalAmount();
        }
        return total;
    }

    /**
     * Builds a comprehensive monthly sales summary for the specified month and year (BR21).
     *
     * @param month the calendar month (1 through 12)
     * @param year the positive four-digit year
     * @return a MonthlySalesSummary containing order count, units sold, and net revenue
     * @throws IllegalArgumentException if month is outside 1..12 or year is not positive
     */
    public MonthlySalesSummary getMonthlySalesReport(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month + ". Expected 1 through 12.");
        }
        if (year <= 0) {
            throw new IllegalArgumentException("Invalid year: " + year + ". Year must be positive.");
        }
        YearMonth target = YearMonth.of(year, month);
        List<Order> monthlyOrders = getOrdersByMonth(target);
        int totalUnits = 0;
        double totalGross = 0.0;
        double totalDiscount = 0.0;
        double totalNet = 0.0;

        for (Order order : monthlyOrders) {
            totalGross += order.getTotalAmount();
            totalDiscount += order.getDiscount();
            totalNet += order.getFinalAmount();
            for (OrderDetail item : order.getItems()) {
                totalUnits += item.getQuantity();
            }
        }
        return new MonthlySalesSummary(month, year, monthlyOrders.size(),
                totalUnits, totalGross, totalDiscount, totalNet);
    }

    /**
     * Aggregates and ranks best-selling products across all completed orders,
     * sorted by total quantity sold descending, then gross revenue descending,
     * then product ID ascending (BR18, BR22, BR27).
     *
     * @param limit the maximum number of top products to return (or zero or negative for all)
     * @return an unmodifiable list of ProductSalesSummary objects
     */
    public List<ProductSalesSummary> getTopSellingProducts(int limit) {
        Map<String, Integer> qtyByProduct = new LinkedHashMap<>();
        Map<String, Double> revByProduct = new LinkedHashMap<>();

        for (Order order : this.orderController.getAllOrders()) {
            for (OrderDetail item : order.getItems()) {
                String key = item.getProductId().toUpperCase(Locale.ROOT);
                Integer prevQty = qtyByProduct.get(key);
                Double prevRev = revByProduct.get(key);
                qtyByProduct.put(key, (prevQty == null ? 0 : prevQty) + item.getQuantity());
                revByProduct.put(key, (prevRev == null ? 0.0 : prevRev) + item.getSubTotal());
            }
        }

        List<ProductSalesSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : qtyByProduct.entrySet()) {
            String productId = entry.getKey();
            int totalQty = entry.getValue();
            double totalRev = revByProduct.get(productId);

            Product product = this.productController.findByIdIncludeDeleted(productId);
            String productName = (product != null) ? product.getName() : "Unknown (" + productId + ")";
            String category = (product != null) ? product.getCategory() : "N/A";
            String unit = (product != null) ? product.getUnit() : "N/A";

            summaries.add(new ProductSalesSummary(productId, productName, category, unit, totalQty, totalRev));
        }

        Collections.sort(summaries, new Comparator<ProductSalesSummary>() {
            @Override
            public int compare(ProductSalesSummary a, ProductSalesSummary b) {
                int qtyCmp = Integer.compare(b.getQuantitySold(), a.getQuantitySold());
                if (qtyCmp != 0) {
                    return qtyCmp;
                }
                int revCmp = Double.compare(b.getGrossRevenue(), a.getGrossRevenue());
                if (revCmp != 0) {
                    return revCmp;
                }
                return a.getProductId().compareToIgnoreCase(b.getProductId());
            }
        });

        if (limit > 0 && summaries.size() > limit) {
            return Collections.unmodifiableList(new ArrayList<>(summaries.subList(0, limit)));
        }
        return Collections.unmodifiableList(summaries);
    }

    /**
     * Alias for getTopSellingProducts maintained for caller compatibility (BR22).
     *
     * @param limit the maximum number of products to return
     * @return an unmodifiable list of ProductSalesSummary objects
     */
    public List<ProductSalesSummary> getBestSellingProducts(int limit) {
        return getTopSellingProducts(limit);
    }

    /**
     * Aggregates and ranks registered customer spending across all completed
     * orders (excluding walk-in GUEST orders), sorted by total net spending
     * descending, then order count descending, then customer ID ascending
     * (BR16, BR18, BR23, BR27).
     *
     * @return an unmodifiable list of CustomerSpendingSummary objects
     */
    public List<CustomerSpendingSummary> getCustomerSpendingSummary() {
        return getHighestSpendingCustomers(0);
    }

    /**
     * Aggregates and ranks registered customer spending up to the specified limit
     * (or all customers with orders if limit is zero or negative) (BR16, BR18, BR23, BR27).
     *
     * @param limit the maximum number of customers to return (or zero or negative for all)
     * @return an unmodifiable list of CustomerSpendingSummary objects
     */
    public List<CustomerSpendingSummary> getHighestSpendingCustomers(int limit) {
        Map<String, Integer> countByCustomer = new LinkedHashMap<>();
        Map<String, Double> spendByCustomer = new LinkedHashMap<>();

        for (Order order : this.orderController.getAllOrders()) {
            String customerId = order.getCustomerId();
            if (customerId == null || customerId.trim().isEmpty()
                    || Order.GUEST_CUSTOMER_ID.equalsIgnoreCase(customerId.trim())) {
                continue;
            }
            String key = customerId.toUpperCase(Locale.ROOT);
            Integer prevCount = countByCustomer.get(key);
            Double prevSpend = spendByCustomer.get(key);
            countByCustomer.put(key, (prevCount == null ? 0 : prevCount) + 1);
            spendByCustomer.put(key, (prevSpend == null ? 0.0 : prevSpend) + order.getFinalAmount());
        }

        List<CustomerSpendingSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countByCustomer.entrySet()) {
            String customerId = entry.getKey();
            int orderCount = entry.getValue();
            double totalSpent = spendByCustomer.get(customerId);

            Customer customer = this.customerController.findByIdIncludeDeleted(customerId);
            String customerName = (customer != null)
                    ? customer.getFullName() : "Unknown (" + customerId + ")";
            String tier = (customer != null) ? customer.getCustomerType() : "Regular";

            summaries.add(new CustomerSpendingSummary(customerId, customerName, tier, orderCount, totalSpent));
        }

        Collections.sort(summaries, new Comparator<CustomerSpendingSummary>() {
            @Override
            public int compare(CustomerSpendingSummary a, CustomerSpendingSummary b) {
                int spendCmp = Double.compare(b.getTotalSpent(), a.getTotalSpent());
                if (spendCmp != 0) {
                    return spendCmp;
                }
                int countCmp = Integer.compare(b.getOrderCount(), a.getOrderCount());
                if (countCmp != 0) {
                    return countCmp;
                }
                return a.getCustomerId().compareToIgnoreCase(b.getCustomerId());
            }
        });

        if (limit > 0 && summaries.size() > limit) {
            return Collections.unmodifiableList(new ArrayList<>(summaries.subList(0, limit)));
        }
        return Collections.unmodifiableList(summaries);
    }

    /**
     * Returns all active products whose available non-expired stock is at or
     * below the default threshold of 5 units (BR19).
     *
     * @return an unmodifiable list of low-stock products
     */
    public List<Product> getLowStockReport() {
        return this.productController.getLowStockProducts();
    }

    /**
     * Returns all active products whose available non-expired stock is at or
     * below the specified threshold (BR19).
     *
     * @param threshold the non-negative stock threshold
     * @return an unmodifiable list of low-stock products
     */
    public List<Product> getLowStockProducts(int threshold) {
        return this.productController.getLowStockProducts(threshold);
    }

    /**
     * Returns all expired batches with positive stock across active products (BR11).
     *
     * @return an unmodifiable list of expired batches
     */
    public List<Batch> getExpiredBatches() {
        return this.productController.getExpiredBatches();
    }

    /**
     * Returns all near-expiry batches within the specified day threshold across
     * active products (BR20).
     *
     * @param daysThreshold the non-negative warning window in days
     * @return an unmodifiable list of near-expiry batches
     */
    public List<Batch> getNearExpiryBatches(int daysThreshold) {
        return this.productController.getNearExpiryBatches(daysThreshold);
    }

    /**
     * Returns the shared OrderController collaborator.
     *
     * @return the OrderController instance
     */
    public OrderController getOrderController() {
        return orderController;
    }

    /**
     * Returns the shared ProductController collaborator.
     *
     * @return the ProductController instance
     */
    public ProductController getProductController() {
        return productController;
    }

    /**
     * Returns the shared CustomerController collaborator.
     *
     * @return the CustomerController instance
     */
    public CustomerController getCustomerController() {
        return customerController;
    }

    /**
     * Immutable summary value object representing monthly sales metrics (BR21).
     */
    public static final class MonthlySalesSummary {

        private final int month;
        private final int year;
        private final int totalTransactions;
        private final int totalItemsSold;
        private final double grossAmount;
        private final double totalDiscount;
        private final double totalRevenue;

        /**
         * Constructs a MonthlySalesSummary instance.
         *
         * @param month the calendar month (1-12)
         * @param year the four-digit year
         * @param totalTransactions the count of completed orders
         * @param totalItemsSold the total integer units sold
         * @param grossAmount the pre-discount gross sales total
         * @param totalDiscount the total VIP discounts granted
         * @param totalRevenue the net revenue collected
         */
        public MonthlySalesSummary(int month, int year, int totalTransactions,
                int totalItemsSold, double grossAmount, double totalDiscount, double totalRevenue) {
            this.month = month;
            this.year = year;
            this.totalTransactions = totalTransactions;
            this.totalItemsSold = totalItemsSold;
            this.grossAmount = grossAmount;
            this.totalDiscount = totalDiscount;
            this.totalRevenue = totalRevenue;
        }

        /**
         * Returns the calendar month (1-12).
         *
         * @return the month
         */
        public int getMonth() {
            return month;
        }

        /**
         * Returns the four-digit year.
         *
         * @return the year
         */
        public int getYear() {
            return year;
        }

        /**
         * Returns the number of completed orders in the month.
         *
         * @return the completed order count
         */
        public int getTotalTransactions() {
            return totalTransactions;
        }

        /**
         * Returns the total integer units sold across all orders in the month.
         *
         * @return the total units sold
         */
        public int getTotalItemsSold() {
            return totalItemsSold;
        }

        /**
         * Returns the pre-discount gross total in VND.
         *
         * @return the gross amount
         */
        public double getGrossAmount() {
            return grossAmount;
        }

        /**
         * Returns the total discount deducted in VND.
         *
         * @return the total discount
         */
        public double getTotalDiscount() {
            return totalDiscount;
        }

        /**
         * Returns the net revenue (sum of finalAmount) in VND.
         *
         * @return the net revenue
         */
        public double getTotalRevenue() {
            return totalRevenue;
        }
    }

    /**
     * Immutable summary value object representing a product's cumulative sales
     * volume and gross revenue (BR22).
     */
    public static final class ProductSalesSummary {

        private final String productId;
        private final String productName;
        private final String category;
        private final String unit;
        private final int quantitySold;
        private final double grossRevenue;

        /**
         * Constructs a ProductSalesSummary instance.
         *
         * @param productId the 6-character product ID
         * @param productName the resolved product name
         * @param category the resolved product category
         * @param unit the resolved measurement unit
         * @param quantitySold the total integer units sold
         * @param grossRevenue the cumulative line-item subtotal revenue
         */
        public ProductSalesSummary(String productId, String productName,
                String category, String unit, int quantitySold, double grossRevenue) {
            this.productId = productId;
            this.productName = productName;
            this.category = category;
            this.unit = unit;
            this.quantitySold = quantitySold;
            this.grossRevenue = grossRevenue;
        }

        /**
         * Returns the product identifier.
         *
         * @return the 6-character product ID
         */
        public String getProductId() {
            return productId;
        }

        /**
         * Returns the product display name.
         *
         * @return the product name
         */
        public String getProductName() {
            return productName;
        }

        /**
         * Returns the product category.
         *
         * @return the category
         */
        public String getCategory() {
            return category;
        }

        /**
         * Returns the product measurement unit.
         *
         * @return the unit
         */
        public String getUnit() {
            return unit;
        }

        /**
         * Returns the total integer quantity sold across all completed orders.
         *
         * @return the total units sold
         */
        public int getQuantitySold() {
            return quantitySold;
        }

        /**
         * Returns the cumulative gross revenue generated by this product in VND.
         *
         * @return the gross revenue
         */
        public double getGrossRevenue() {
            return grossRevenue;
        }
    }

    /**
     * Immutable summary value object representing a registered customer's
     * cumulative order count and net spending (BR23).
     */
    public static final class CustomerSpendingSummary {

        private final String customerId;
        private final String customerName;
        private final String customerType;
        private final int orderCount;
        private final double totalSpent;

        /**
         * Constructs a CustomerSpendingSummary instance.
         *
         * @param customerId the 6-character customer ID
         * @param customerName the resolved customer full name
         * @param customerType the loyalty tier ("Regular" or "VIP")
         * @param orderCount the number of completed orders placed
         * @param totalSpent the cumulative net amount spent in VND
         */
        public CustomerSpendingSummary(String customerId, String customerName,
                String customerType, int orderCount, double totalSpent) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.customerType = customerType;
            this.orderCount = orderCount;
            this.totalSpent = totalSpent;
        }

        /**
         * Returns the customer identifier.
         *
         * @return the 6-character customer ID
         */
        public String getCustomerId() {
            return customerId;
        }

        /**
         * Returns the customer's full name.
         *
         * @return the customer full name
         */
        public String getCustomerName() {
            return customerName;
        }

        /**
         * Returns the customer's loyalty tier ("Regular" or "VIP").
         *
         * @return the customer tier
         */
        public String getCustomerType() {
            return customerType;
        }

        /**
         * Returns the number of completed orders placed by this customer.
         *
         * @return the completed order count
         */
        public int getOrderCount() {
            return orderCount;
        }

        /**
         * Returns the cumulative net spending (sum of finalAmount) in VND.
         *
         * @return the total amount spent
         */
        public double getTotalSpent() {
            return totalSpent;
        }
    }
}
