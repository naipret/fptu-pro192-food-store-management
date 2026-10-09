package fptu.pro192.foodstoremanagement.repository;

import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.Transaction;
import fptu.pro192.foodstoremanagement.util.MiniCsv;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Lightweight repository managing in-memory order caching and coordinating atomic CSV
 * persistence for orders and line details.
 *
 * BR13: Subtotal aggregation persistence.
 * BR17: Final amount calculation persistence.
 * BR23: Completed transaction tracking for revenue calculation.
 */
public class OrderRepository {

    private final String ordersFilePath;
    private final String detailsFilePath;
    private final List<Order> orders = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private static final List<String> ORDERS_CSV_HEADERS =
            Arrays.asList("orderId", "customerId", "orderDate", "subtotal", "discountAmount", "finalAmount", "isCompleted");

    private static final List<String> DETAILS_CSV_HEADERS =
            Arrays.asList("orderId", "productId", "productName", "unit", "unitPrice", "quantity");

    /**
     * Default constructor targeting default production CSV files.
     */
    public OrderRepository() {
        this("data/orders.csv", "data/order_details.csv");
    }

    /**
     * Constructs an OrderRepository with custom CSV file paths (supports isolated unit testing).
     *
     * @param ordersFilePath Path to orders CSV file
     * @param detailsFilePath Path to order details CSV file
     * @throws IllegalArgumentException If either file path is null or empty
     */
    public OrderRepository(String ordersFilePath, String detailsFilePath) {
        if (ordersFilePath == null || ordersFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Orders file path cannot be null or empty");
        }
        if (detailsFilePath == null || detailsFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Details file path cannot be null or empty");
        }
        this.ordersFilePath = ordersFilePath;
        this.detailsFilePath = detailsFilePath;
        loadFromCsv();
    }

    /**
     * Loads orders and associated line item details from CSV files into memory.
     * Reconstructs transactions for completed orders. Synchronized for thread-safety.
     */
    public synchronized void loadFromCsv() {
        orders.clear();
        transactions.clear();

        // 1. Read orders from orders.csv
        List<String[]> ordersData = MiniCsv.read(ordersFilePath);
        for (int i = 0; i < ordersData.size(); i++) {
            String[] row = ordersData.get(i);
            if (row == null || row.length < 6) {
                continue;
            }
            // Skip CSV header if present
            if (i == 0 && "orderId".equalsIgnoreCase(row[0].trim())) {
                continue;
            }

            try {
                String orderId = row[0].trim();
                String customerId = row[1].trim();
                LocalDate orderDate = LocalDate.parse(row[2].trim());
                double subtotal = Double.parseDouble(row[3].trim());
                double discountAmount = Double.parseDouble(row[4].trim());
                double finalAmount = Double.parseDouble(row[5].trim());
                boolean isCompleted = (row.length >= 7) ? Boolean.parseBoolean(row[6].trim()) : true;

                Order order = new Order(orderId, customerId, orderDate, null,
                        subtotal, discountAmount, finalAmount, isCompleted);
                orders.add(order);
            } catch (Exception e) {
                throw new RuntimeException("Corrupted record in orders CSV file: " + String.join(",", row), e);
            }
        }

        // 2. Read order details from order_details.csv and attach to parent orders
        List<String[]> detailsData = MiniCsv.read(detailsFilePath);
        for (int i = 0; i < detailsData.size(); i++) {
            String[] row = detailsData.get(i);
            if (row == null || row.length < 6) {
                continue;
            }
            // Skip CSV header if present
            if (i == 0 && "orderId".equalsIgnoreCase(row[0].trim())) {
                continue;
            }

            try {
                String orderId = row[0].trim();
                String productId = row[1].trim();
                String productName = row[2].trim();
                String unit = row[3].trim();
                double unitPrice = Double.parseDouble(row[4].trim());
                int quantity = Integer.parseInt(row[5].trim());

                OrderDetail detail = new OrderDetail(orderId, productId, productName, unit, unitPrice, quantity);

                // Attach line item to corresponding parent order
                for (Order order : orders) {
                    if (order.getOrderId().equalsIgnoreCase(orderId)) {
                        order.addItem(detail);
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Corrupted record in order details CSV file: " + String.join(",", row), e);
            }
        }

        // 3. Reconstruct in-memory transactions for completed orders (BR23)
        for (Order order : orders) {
            if (order.isCompleted()) {
                transactions.add(new Transaction("TX-" + order.getOrderId(), order, "CASH"));
            }
        }
    }

    /**
     * Atomically serializes in-memory orders and line details to flat CSV storage.
     * Uses MiniCsv.writeAtomic to guarantee transactional file replacement without partial corruption.
     */
    public synchronized void saveToCsv() {
        List<String[]> orderRows = new ArrayList<>();
        List<String[]> detailRows = new ArrayList<>();

        for (Order o : orders) {
            String custId = (o.getCustomerId() != null) ? o.getCustomerId() : "";
            String dateStr = (o.getOrderDate() != null) ? o.getOrderDate().toString() : LocalDate.now().toString();

            orderRows.add(new String[] {
                    o.getOrderId(),
                    custId,
                    dateStr,
                    String.valueOf(o.getSubtotal()),
                    String.valueOf(o.getDiscountAmount()),
                    String.valueOf(o.getFinalAmount()),
                    String.valueOf(o.isCompleted())
            });

            for (OrderDetail d : o.getItems()) {
                String itemOrderId = (d.getOrderId() != null && !d.getOrderId().trim().isEmpty())
                        ? d.getOrderId() : o.getOrderId();
                detailRows.add(new String[] {
                        itemOrderId,
                        d.getProductId(),
                        d.getProductName(),
                        d.getUnit(),
                        String.valueOf(d.getUnitPrice()),
                        String.valueOf(d.getQuantity())
                });
            }
        }

        MiniCsv.writeAtomic(ordersFilePath, ORDERS_CSV_HEADERS, orderRows);
        MiniCsv.writeAtomic(detailsFilePath, DETAILS_CSV_HEADERS, detailRows);
    }

    /**
     * Saves or updates an order in memory and triggers atomic CSV persistence.
     * Also records a transaction if the order is marked as completed.
     *
     * @param order The order to persist
     * @throws IllegalArgumentException If order or orderId is null
     */
    public synchronized void saveOrder(Order order) {
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Order and Order ID cannot be null");
        }
        boolean exists = false;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getOrderId().equalsIgnoreCase(order.getOrderId())) {
                orders.set(i, order);
                exists = true;
                break;
            }
        }
        if (!exists) {
            orders.add(order);
        }

        // If order is completed, ensure transaction exists in memory (BR23)
        if (order.isCompleted()) {
            boolean hasTx = false;
            for (Transaction tx : transactions) {
                if (tx.getOrderId() != null && tx.getOrderId().equalsIgnoreCase(order.getOrderId())) {
                    hasTx = true;
                    break;
                }
            }
            if (!hasTx) {
                transactions.add(new Transaction("TX-" + order.getOrderId(), order, "CASH"));
            }
        }

        saveToCsv();
    }

    /**
     * Records or updates a transaction in memory.
     *
     * @param transaction The transaction to record
     * @throws IllegalArgumentException If transaction or transactionId is null
     */
    public synchronized void recordTransaction(Transaction transaction) {
        if (transaction == null || transaction.getTransactionId() == null) {
            throw new IllegalArgumentException("Transaction and Transaction ID cannot be null");
        }
        boolean exists = false;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getTransactionId().equalsIgnoreCase(transaction.getTransactionId())) {
                transactions.set(i, transaction);
                exists = true;
                break;
            }
        }
        if (!exists) {
            transactions.add(transaction);
        }
    }

    /**
     * Convenience method to save a transaction (alias for recordTransaction).
     *
     * @param transaction The transaction to save
     */
    public synchronized void saveTransaction(Transaction transaction) {
        recordTransaction(transaction);
    }

    /**
     * Finds an order by its unique order ID.
     *
     * @param orderId The order ID to search for (case-insensitive)
     * @return Matching Order, or null if not found
     */
    public synchronized Order findById(String orderId) {
        if (orderId == null) {
            return null;
        }
        for (Order order : orders) {
            if (orderId.trim().equalsIgnoreCase(order.getOrderId())) {
                return order;
            }
        }
        return null;
    }

    /**
     * Returns an unmodifiable list of all orders.
     *
     * @return Unmodifiable list of orders
     */
    public synchronized List<Order> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }

    /**
     * Returns an unmodifiable list of all recorded transactions.
     *
     * BR23: Total revenue is calculated exclusively from completed sales transactions.
     *
     * @return Unmodifiable list of transactions
     */
    public synchronized List<Transaction> findAllTransactions() {
        return Collections.unmodifiableList(new ArrayList<>(transactions));
    }

    /**
     * Finds all orders placed by a specific customer ID.
     *
     * @param customerId The customer ID to search for
     * @return Unmodifiable list of orders matching the customer ID
     */
    public synchronized List<Order> findByCustomerId(String customerId) {
        List<Order> result = new ArrayList<>();
        if (customerId == null) {
            return Collections.unmodifiableList(result);
        }
        for (Order order : orders) {
            if (customerId.trim().equalsIgnoreCase(order.getCustomerId())) {
                result.add(order);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an unmodifiable list of all completed orders.
     *
     * BR23: Revenue reports only query completed orders.
     *
     * @return Unmodifiable list of completed orders
     */
    public synchronized List<Order> findCompletedOrders() {
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.isCompleted()) {
                result.add(order);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
