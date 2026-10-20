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
     * @param orderController    the shared OrderController instance
     * @param productController  the shared ProductController instance
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
     * Returns all completed orders whose order date matches the specified calendar
     * date (BR21).
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
     * Calculates the total net revenue (sum of finalAmount) for a specific calendar
     * date (BR21).
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
     * Returns all completed orders whose order date falls within the specified
     * YearMonth (BR21).
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
     * Calculates the total net revenue (sum of finalAmount) for a specific
     * YearMonth (BR21).
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
     * Aggregates and ranks best-selling products across all completed orders,
     * sorted by total quantity sold descending, then gross revenue descending,
     * then product ID ascending (BR18, BR21, BR27).
     *
     * @param limit the maximum number of top products to return (or zero or
     *              negative for all)
     * @return an unmodifiable list of ProductSalesSummary objects
     */
    public List<ProductSalesSummary> getTopSellingProducts(int limit) {
        return getTopSellingProducts(null, null, limit);
    }

    /**
     * Aggregates and ranks best-selling products during an optional calendar period,
     * sorted by total quantity sold descending, then gross revenue descending,
     * then product ID ascending (BR18, BR21, BR27).
     *
     * @param fromDate the inclusive start date, or null for unbounded start
     * @param toDate   the inclusive end date, or null for unbounded end
     * @param limit    the maximum number of top products to return (or zero or negative for all)
     * @return an unmodifiable list of ProductSalesSummary objects
     * @throws IllegalArgumentException if fromDate is after toDate
     */
    public List<ProductSalesSummary> getTopSellingProducts(LocalDate fromDate, LocalDate toDate, int limit) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date.");
        }
        Map<String, Integer> qtyByProduct = new LinkedHashMap<>();
        Map<String, Double> revByProduct = new LinkedHashMap<>();

        for (Order order : this.orderController.getAllOrders()) {
            LocalDate orderDate = order.getOrderDate().toLocalDate();
            if (fromDate != null && orderDate.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && orderDate.isAfter(toDate)) {
                continue;
            }
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
     * Aggregates and ranks registered customer spending up to the specified limit
     * (or all customers with orders if limit is zero or negative) (BR16, BR18,
     * BR22, BR27).
     *
     * @param limit the maximum number of customers to return (or zero or negative
     *              for all)
     * @return an unmodifiable list of CustomerSpendingSummary objects
     */
    public List<CustomerSpendingSummary> getHighestSpendingCustomers(int limit) {
        return getHighestSpendingCustomers(null, null, limit);
    }

    /**
     * Aggregates and ranks registered customer spending during an optional calendar
     * period up to the specified limit (BR16, BR18, BR22, BR27).
     *
     * @param fromDate the inclusive start date, or null for unbounded start
     * @param toDate   the inclusive end date, or null for unbounded end
     * @param limit    the maximum number of customers to return (or zero or negative for all)
     * @return an unmodifiable list of CustomerSpendingSummary objects
     * @throws IllegalArgumentException if fromDate is after toDate
     */
    public List<CustomerSpendingSummary> getHighestSpendingCustomers(LocalDate fromDate, LocalDate toDate, int limit) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date.");
        }
        Map<String, Integer> countByCustomer = new LinkedHashMap<>();
        Map<String, Double> spendByCustomer = new LinkedHashMap<>();

        for (Order order : this.orderController.getAllOrders()) {
            String customerId = order.getCustomerId();
            if (customerId == null || customerId.trim().isEmpty()
                    || Order.GUEST_CUSTOMER_ID.equalsIgnoreCase(customerId.trim())) {
                continue;
            }
            LocalDate orderDate = order.getOrderDate().toLocalDate();
            if (fromDate != null && orderDate.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && orderDate.isAfter(toDate)) {
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
                    ? customer.getFullName()
                    : "Unknown (" + customerId + ")";
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
         * @param productId    the 6-character product ID
         * @param productName  the resolved product name
         * @param category     the resolved product category
         * @param unit         the resolved measurement unit
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
         * @param customerId   the 6-character customer ID
         * @param customerName the resolved customer full name
         * @param customerType the loyalty tier ("Regular" or "VIP")
         * @param orderCount   the number of completed orders placed
         * @param totalSpent   the cumulative net amount spent in VND
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
