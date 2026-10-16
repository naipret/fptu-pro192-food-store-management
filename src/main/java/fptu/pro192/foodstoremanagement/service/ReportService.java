package fptu.pro192.foodstoremanagement.service;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.repository.OrderRepository;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analytical query service generating business reports and performance rankings.
 *
 * BR19: Low stock threshold (&lt;= 5 units across active inventory batches).
 * BR21: Best-selling food products ranking sorted by total quantity sold.
 * BR22: Highest-spending customers ranking sorted by total purchase value across completed transactions.
 * BR23: Total revenue calculated exclusively from completed sales transactions.
 */
public class ReportService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;

    /**
     * DTO encapsulating monthly financial metrics and volume statistics.
     */
    public static class MonthlyReport {
        private final int month;
        private final int year;
        private final int totalTransactions;
        private final int totalItemsSold;
        private final double totalRevenue;

        public MonthlyReport(int month, int year, int totalTransactions, int totalItemsSold, double totalRevenue) {
            this.month = month;
            this.year = year;
            this.totalTransactions = totalTransactions;
            this.totalItemsSold = totalItemsSold;
            this.totalRevenue = totalRevenue;
        }

        public int getMonth() {
            return month;
        }

        public int getYear() {
            return year;
        }

        public int getTotalTransactions() {
            return totalTransactions;
        }

        public int getTotalItemsSold() {
            return totalItemsSold;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }

        @Override
        public String toString() {
            return "MonthlyReport{" +
                    "month=" + month +
                    ", year=" + year +
                    ", totalTransactions=" + totalTransactions +
                    ", totalItemsSold=" + totalItemsSold +
                    ", totalRevenue=" + totalRevenue +
                    '}';
        }
    }

    /**
     * DTO capturing aggregated sales statistics for a specific product.
     */
    public static class ProductSalesStat {
        private final String productId;
        private final String productName;
        private final int quantitySold;
        private final double totalRevenue;

        public ProductSalesStat(String productId, String productName, int quantitySold, double totalRevenue) {
            this.productId = productId;
            this.productName = productName;
            this.quantitySold = quantitySold;
            this.totalRevenue = totalRevenue;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantitySold() {
            return quantitySold;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }

        @Override
        public String toString() {
            return "ProductSalesStat{" +
                    "productId='" + productId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", quantitySold=" + quantitySold +
                    ", totalRevenue=" + totalRevenue +
                    '}';
        }
    }

    /**
     * DTO capturing aggregated spending statistics for a specific customer.
     */
    public static class CustomerSpendStat {
        private final String customerId;
        private final String customerName;
        private final double totalSpent;
        private final int orderCount;

        public CustomerSpendStat(String customerId, String customerName, double totalSpent, int orderCount) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.totalSpent = totalSpent;
            this.orderCount = orderCount;
        }

        public String getCustomerId() {
            return customerId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public double getTotalSpent() {
            return totalSpent;
        }

        public int getOrderCount() {
            return orderCount;
        }

        @Override
        public String toString() {
            return "CustomerSpendStat{" +
                    "customerId='" + customerId + '\'' +
                    ", customerName='" + customerName + '\'' +
                    ", totalSpent=" + totalSpent +
                    ", orderCount=" + orderCount +
                    '}';
        }
    }

    /**
     * Default constructor wiring production repositories.
     */
    public ReportService() {
        this(new OrderRepository(), new ProductRepository(), new CustomerRepository());
    }

    /**
     * Parameterized constructor enabling dependency injection.
     *
     * @param orderRepo The repository managing order and transaction data
     * @param productRepo The repository managing product catalog data
     * @param customerRepo The repository managing customer data
     * @throws IllegalArgumentException If any repository is null
     */
    public ReportService(OrderRepository orderRepo, ProductRepository productRepo, CustomerRepository customerRepo) {
        if (orderRepo == null) {
            throw new IllegalArgumentException("OrderRepository cannot be null.");
        }
        if (productRepo == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null.");
        }
        if (customerRepo == null) {
            throw new IllegalArgumentException("CustomerRepository cannot be null.");
        }
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
    }

    /**
     * Task B15: Calculates monthly sales report for the specified month and year.
     *
     * BR23: Total revenue and metrics are calculated exclusively from completed sales transactions.
     * Unconfirmed drafts or cancelled orders are strictly excluded.
     *
     * @param month Target month (1 - 12)
     * @param year Target year (&gt; 0)
     * @return MonthlyReport containing transaction count, total units sold, and total revenue
     * @throws IllegalArgumentException If month is outside [1, 12] or year is non-positive
     */
    public MonthlyReport getMonthlySalesReport(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng không hợp lệ: " + month + ". Giá trị phải từ 1 đến 12.");
        }
        if (year <= 0) {
            throw new IllegalArgumentException("Năm không hợp lệ: " + year);
        }

        int transactionsCount = 0;
        int totalItemsSold = 0;
        double totalRevenue = 0.0;

        List<Order> orders = orderRepo.findAll();
        for (Order order : orders) {
            // BR23: Must only calculate from completed transactions
            if (!order.isCompleted()) {
                continue;
            }

            LocalDate orderDate = order.getOrderDate();
            if (orderDate != null && orderDate.getMonthValue() == month && orderDate.getYear() == year) {
                transactionsCount++;
                totalRevenue += order.getFinalAmount();

                for (OrderDetail detail : order.getItems()) {
                    totalItemsSold += detail.getQuantity();
                }
            }
        }

        return new MonthlyReport(month, year, transactionsCount, totalItemsSold, totalRevenue);
    }

    /**
     * Task B16: Returns Top N best-selling food products ranked by total quantity sold.
     *
     * BR21: Best-selling food products ranking is determined by total quantity sold across completed transactions.
     * BR23: Revenue and quantities are aggregated exclusively from completed orders.
     *
     * @param limit Maximum number of products to return (&gt; 0, defaults to 10 if non-positive)
     * @return List of ProductSalesStat sorted in descending order of quantity sold
     */
    public List<ProductSalesStat> getBestSellingProducts(int limit) {
        int maxResults = (limit <= 0) ? 10 : limit;

        Map<String, Integer> qtyMap = new HashMap<>();
        Map<String, Double> revenueMap = new HashMap<>();
        Map<String, String> nameMap = new HashMap<>();

        List<Order> orders = orderRepo.findAll();
        for (Order order : orders) {
            // BR23: Exclusively completed sales transactions
            if (!order.isCompleted()) {
                continue;
            }

            for (OrderDetail detail : order.getItems()) {
                String productId = detail.getProductId();
                if (productId == null || productId.trim().isEmpty()) {
                    continue;
                }

                int qty = detail.getQuantity();
                double lineTotal = detail.getSubtotal();

                int currentQty = qtyMap.containsKey(productId) ? qtyMap.get(productId) : 0;
                qtyMap.put(productId, currentQty + qty);

                double currentRev = revenueMap.containsKey(productId) ? revenueMap.get(productId) : 0.0;
                revenueMap.put(productId, currentRev + lineTotal);

                if (!nameMap.containsKey(productId)) {
                    nameMap.put(productId, detail.getProductName());
                }
            }
        }

        List<ProductSalesStat> stats = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : qtyMap.entrySet()) {
            String pId = entry.getKey();
            int totalQty = entry.getValue();
            double rev = revenueMap.containsKey(pId) ? revenueMap.get(pId) : 0.0;

            // Fetch live product name from catalog if available, fallback to snapshot name
            Food food = productRepo.findById(pId);
            String pName = (food != null) ? food.getName() : nameMap.get(pId);

            stats.add(new ProductSalesStat(pId, pName, totalQty, rev));
        }

        // BR21: Sort in descending order by quantity sold
        Collections.sort(stats, new Comparator<ProductSalesStat>() {
            @Override
            public int compare(ProductSalesStat o1, ProductSalesStat o2) {
                int cmp = Integer.compare(o2.getQuantitySold(), o1.getQuantitySold());
                if (cmp != 0) {
                    return cmp;
                }
                return o1.getProductId().compareToIgnoreCase(o2.getProductId());
            }
        });

        if (stats.size() > maxResults) {
            stats = stats.subList(0, maxResults);
        }

        return Collections.unmodifiableList(stats);
    }

    /**
     * Task B17: Returns Top N highest-spending customers ranked by total purchase value across completed transactions.
     *
     * BR22: Highest-spending customers ranking is determined based on total completed purchase value.
     * BR23: Revenue is aggregated exclusively from completed orders.
     *
     * @param limit Maximum number of customers to return (&gt; 0, defaults to 10 if non-positive)
     * @return List of CustomerSpendStat sorted in descending order of total money spent
     */
    public List<CustomerSpendStat> getHighestSpendingCustomers(int limit) {
        int maxResults = (limit <= 0) ? 10 : limit;

        Map<String, Double> spendMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();

        List<Order> orders = orderRepo.findAll();
        for (Order order : orders) {
            // BR23: Must only calculate from completed transactions
            if (!order.isCompleted()) {
                continue;
            }

            String customerId = order.getCustomerId();
            if (customerId == null || customerId.trim().isEmpty()) {
                continue; // Skip guest / anonymous orders
            }

            double finalAmt = order.getFinalAmount();
            double currentSpend = spendMap.containsKey(customerId) ? spendMap.get(customerId) : 0.0;
            spendMap.put(customerId, currentSpend + finalAmt);

            int currentCount = countMap.containsKey(customerId) ? countMap.get(customerId) : 0;
            countMap.put(customerId, currentCount + 1);
        }

        List<CustomerSpendStat> stats = new ArrayList<>();
        for (Map.Entry<String, Double> entry : spendMap.entrySet()) {
            String cId = entry.getKey();
            double totalSpent = entry.getValue();
            int orderCount = countMap.containsKey(cId) ? countMap.get(cId) : 1;

            Customer customer = customerRepo.findById(cId);
            String customerName = (customer != null) ? customer.getFullName() : "Khách hàng " + cId;

            stats.add(new CustomerSpendStat(cId, customerName, totalSpent, orderCount));
        }

        // BR22: Sort in descending order by totalSpent
        Collections.sort(stats, new Comparator<CustomerSpendStat>() {
            @Override
            public int compare(CustomerSpendStat o1, CustomerSpendStat o2) {
                int cmp = Double.compare(o2.getTotalSpent(), o1.getTotalSpent());
                if (cmp != 0) {
                    return cmp;
                }
                return o1.getCustomerId().compareToIgnoreCase(o2.getCustomerId());
            }
        });

        if (stats.size() > maxResults) {
            stats = stats.subList(0, maxResults);
        }

        return Collections.unmodifiableList(stats);
    }

    /**
     * Task B18: Returns all active food products whose total available inventory stock is &lt;= 5 units.
     *
     * BR19: A food product is considered low stock when its total available quantity across active batches is &lt;= 5.
     *
     * @return List of Food items meeting the low-stock threshold
     */
    public List<Food> getLowStockReport() {
        LocalDate today = LocalDate.now();
        List<Food> lowStockList = new ArrayList<>();

        for (Food food : productRepo.findAll()) {
            if (food.isDeleted()) {
                continue;
            }

            // BR19: Low stock threshold <= 5 units
            if (food.getTotalStock(today) <= 5) {
                lowStockList.add(food);
            }
        }

        return Collections.unmodifiableList(lowStockList);
    }

    /**
     * Returns underlying OrderRepository instance.
     *
     * @return OrderRepository
     */
    public OrderRepository getOrderRepo() {
        return orderRepo;
    }

    /**
     * Returns underlying ProductRepository instance.
     *
     * @return ProductRepository
     */
    public ProductRepository getProductRepo() {
        return productRepo;
    }

    /**
     * Returns underlying CustomerRepository instance.
     *
     * @return CustomerRepository
     */
    public CustomerRepository getCustomerRepo() {
        return customerRepo;
    }
}
