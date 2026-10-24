package fptu.pro192.foodstoremanagement.controller;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.CsvUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Controller orchestrating the sales order lifecycle: draft cart creation,
 * real-time stock and expiration validation, polymorphic loyalty discount
 * calculation, transactional FEFO stock deduction on checkout completion,
 * and atomic CSV persistence (BR8, BR9, BR10, BR11, BR12, BR13, BR14, BR15,
 * BR16, BR17, BR18, BR24, BR26).
 */
public class OrderController {

    /**
     * Default file path for the orders CSV storage.
     */
    public static final String DEFAULT_ORDERS_CSV = "data/orders.csv";

    /**
     * Default file path for the order details CSV storage.
     */
    public static final String DEFAULT_ORDER_DETAILS_CSV = "data/order_details.csv";

    private static final String[] ORDER_CSV_HEADERS = {
            "orderId", "customerId", "orderDate", "totalAmount", "discount", "finalAmount"
    };

    private static final String[] DETAIL_CSV_HEADERS = {
            "orderId", "productId", "quantity", "unitPrice"
    };

    private final Path ordersCsvPath;
    private final Path detailsCsvPath;
    private final List<Order> orders;
    private final ProductController productController;
    private final CustomerController customerController;

    /**
     * Constructs an OrderController bound to the default CSV paths in data/
     * with explicitly injected ProductController and CustomerController instances.
     *
     * @param productController  the shared ProductController instance
     * @param customerController the shared CustomerController instance
     * @throws IllegalArgumentException if either collaborator is null
     */
    public OrderController(ProductController productController,
            CustomerController customerController) {
        this(Paths.get(DEFAULT_ORDERS_CSV), Paths.get(DEFAULT_ORDER_DETAILS_CSV),
                productController, customerController);
    }

    /**
     * Constructs an OrderController with explicit CSV Paths and shared
     * controller collaborators, immediately loading completed historical
     * orders from disk.
     *
     * @param ordersCsvPath      the Path for orders.csv
     * @param detailsCsvPath     the Path for order_details.csv
     * @param productController  the shared ProductController instance
     * @param customerController the shared CustomerController instance
     * @throws IllegalArgumentException if any argument is null
     */
    public OrderController(Path ordersCsvPath, Path detailsCsvPath,
            ProductController productController, CustomerController customerController) {
        if (ordersCsvPath == null) {
            throw new IllegalArgumentException("Orders CSV path cannot be null.");
        }
        if (detailsCsvPath == null) {
            throw new IllegalArgumentException("Order details CSV path cannot be null.");
        }
        if (productController == null) {
            throw new IllegalArgumentException("ProductController cannot be null.");
        }
        if (customerController == null) {
            throw new IllegalArgumentException("CustomerController cannot be null.");
        }
        this.ordersCsvPath = ordersCsvPath;
        this.detailsCsvPath = detailsCsvPath;
        this.productController = productController;
        this.customerController = customerController;
        this.orders = new ArrayList<>();
        loadFromCsv();
    }

    /**
     * Loads all completed historical orders and their line items from
     * orders.csv and order_details.csv, skipping header rows at line 0 and
     * joining line items to parent orders via a Map (BR8, BR12, BR13, BR16, BR17,
     * BR18).
     */
    public final void loadFromCsv() {
        this.orders.clear();
        List<String[]> orderRows = CsvUtil.readCsv(this.ordersCsvPath);
        List<String[]> detailRows = CsvUtil.readCsv(this.detailsCsvPath);
        if (orderRows.isEmpty()) {
            CsvUtil.writeAtomic(this.ordersCsvPath, Collections.singletonList(ORDER_CSV_HEADERS));
        }
        if (detailRows.isEmpty()) {
            CsvUtil.writeAtomic(this.detailsCsvPath, Collections.singletonList(DETAIL_CSV_HEADERS));
        }

        Map<String, List<OrderDetail>> detailsByOrder = new LinkedHashMap<>();
        for (int i = 0; i < detailRows.size(); i++) {
            String[] row = detailRows.get(i);
            if (row.length == 0 || (row.length == 1 && row[0].trim().isEmpty())) {
                continue;
            }
            if (i == 0 && "orderId".equalsIgnoreCase(row[0].trim())) {
                continue;
            }
            if (row.length < 4) {
                throw new IllegalStateException(
                        "Corrupted order_details CSV row at index " + i + ": expected 4 columns.");
            }
            try {
                String orderId = InputValidator.validateOrderId(row[0]);
                String productId = InputValidator.validateProductId(row[1]);
                int quantity = Integer.parseInt(row[2].trim());
                double unitPrice = Double.parseDouble(row[3].trim());

                OrderDetail detail = new OrderDetail(orderId, productId, quantity, unitPrice);
                String key = orderId.toUpperCase(Locale.ROOT);
                List<OrderDetail> list = detailsByOrder.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    detailsByOrder.put(key, list);
                }
                list.add(detail);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Corrupted record in order_details CSV file at row " + i + ": " + e.getMessage(), e);
            }
        }

        for (int i = 0; i < orderRows.size(); i++) {
            String[] row = orderRows.get(i);
            if (row.length == 0 || (row.length == 1 && row[0].trim().isEmpty())) {
                continue;
            }
            if (i == 0 && "orderId".equalsIgnoreCase(row[0].trim())) {
                continue;
            }
            if (row.length < 6) {
                throw new IllegalStateException(
                        "Corrupted orders CSV row at index " + i + ": expected 6 columns.");
            }
            try {
                String orderId = InputValidator.validateOrderId(row[0]);
                String rawCustomerId = row[1].trim();
                String customerId = (rawCustomerId.isEmpty()
                        || Order.GUEST_CUSTOMER_ID.equalsIgnoreCase(rawCustomerId))
                                ? null
                                : InputValidator.validateCustomerId(rawCustomerId);
                LocalDateTime orderDate = parseOrderDateTime(row[2].trim());
                double totalAmount = Double.parseDouble(row[3].trim());
                double discount = Double.parseDouble(row[4].trim());
                double finalAmount = Double.parseDouble(row[5].trim());

                List<OrderDetail> attachedItems = detailsByOrder.get(orderId.toUpperCase(Locale.ROOT));
                Order order = new Order(orderId, customerId, orderDate,
                        attachedItems, totalAmount, discount, finalAmount);
                this.orders.add(order);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Corrupted record in orders CSV file at row " + i + ": " + e.getMessage(), e);
            }
        }
    }

    private LocalDateTime parseOrderDateTime(String text) {
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(text).atStartOfDay();
        }
    }

    /**
     * Atomically writes all finalized orders and their line items to
     * orders.csv and order_details.csv with standard header rows at line 0
     * (BR14, BR16, BR17).
     */
    public void saveToCsv() {
        List<String[]> orderRows = new ArrayList<>();
        List<String[]> detailRows = new ArrayList<>();
        orderRows.add(ORDER_CSV_HEADERS);
        detailRows.add(DETAIL_CSV_HEADERS);

        for (Order order : this.orders) {
            String serializedCustomerId = (order.getCustomerId() == null)
                    ? Order.GUEST_CUSTOMER_ID
                    : order.getCustomerId();
            orderRows.add(new String[] {
                    order.getOrderId(),
                    serializedCustomerId,
                    order.getOrderDate().toString(),
                    String.valueOf(order.getTotalAmount()),
                    String.valueOf(order.getDiscount()),
                    String.valueOf(order.getFinalAmount())
            });
            for (OrderDetail item : order.getItems()) {
                detailRows.add(new String[] {
                        order.getOrderId(),
                        item.getProductId(),
                        String.valueOf(item.getQuantity()),
                        String.valueOf(item.getUnitPrice())
                });
            }
        }

        CsvUtil.writeAtomic(this.ordersCsvPath, orderRows);
        CsvUtil.writeAtomic(this.detailsCsvPath, detailRows);
    }

    /**
     * Generates the next available 6-character Order ID (O00001-O99999) (BR33).
     *
     * @return the next canonical Order ID
     */
    public String generateNextOrderId() {
        int maxNumber = 0;
        for (Order order : this.orders) {
            String id = order.getOrderId();
            if (id != null && id.length() == 6 && (id.charAt(0) == 'O' || id.charAt(0) == 'o')) {
                int num = Integer.parseInt(id.substring(1));
                if (num > maxNumber) {
                    maxNumber = num;
                }
            }
        }
        if (maxNumber >= 99999) {
            throw new IllegalStateException("Maximum Order ID limit (O99999) reached.");
        }
        return String.format(Locale.US, "O%05d", maxNumber + 1);
    }

    /**
     * Checks whether an Order ID already exists in completed order history (BR8).
     *
     * @param orderId the order ID to check
     * @return true if the order ID already exists
     */
    public boolean existsById(String orderId) {
        return findById(orderId) != null;
    }

    /**
     * Creates a new in-memory draft Order with an automatically generated
     * Order ID for the specified customer (or null/GUEST for walk-in guests)
     * without mutating disk storage (BR8, BR14, BR16, BR18).
     *
     * @param customerId the active customer ID, or null/GUEST for guest checkout
     * @return a new empty draft Order
     * @throws IllegalArgumentException if a non-guest customerId does not exist or
     *                                  is soft-deleted
     */
    public Order createDraftOrder(String customerId) {
        String normalizedOrderId = generateNextOrderId();
        String normalizedCustomerId = null;
        if (customerId != null && !customerId.trim().isEmpty()
                && !Order.GUEST_CUSTOMER_ID.equalsIgnoreCase(customerId.trim())) {
            Customer customer = this.customerController.findById(customerId);
            if (customer == null) {
                throw new IllegalArgumentException("Active customer not found with ID: " + customerId);
            }
            normalizedCustomerId = customer.getId();
        }
        return new Order(normalizedOrderId, normalizedCustomerId, LocalDateTime.now());
    }

    /**
     * Returns the remaining non-expired sellable stock of a product after
     * subtracting any quantity already placed in the current draft cart (BR10,
     * BR11).
     *
     * @param draft     the current draft Order, may be null
     * @param productId the product ID to inspect
     * @return the remaining available stock that can still be added to the draft
     *         cart
     */
    public int getRemainingAvailableStock(Order draft, String productId) {
        Product product = this.productController.findById(productId);
        if (product == null) {
            return 0;
        }
        int available = product.getAvailableStock();
        int inCart = (draft == null) ? 0 : draft.getQuantityForProduct(product.getId());
        return Math.max(0, available - inCart);
    }

    /**
     * Validates and adds a product quantity to an in-memory draft Order
     * relative to today without deducting inventory batches yet, then
     * recalculates order totals and VIP discounts (BR9, BR10, BR11, BR12,
     * BR13, BR14, BR15, BR16, BR26).
     *
     * @param draft     the draft Order being assembled
     * @param productId the product ID to add
     * @param quantity  the positive integer quantity to add
     * @throws IllegalArgumentException   if draft/product is invalid, quantity is
     *                                    not positive, or all stock is expired
     * @throws InsufficientStockException if cumulative cart quantity exceeds
     *                                    available non-expired stock
     */
    public void addItemToDraft(Order draft, String productId, int quantity)
            throws InsufficientStockException {
        if (draft == null) {
            throw new IllegalArgumentException("Draft order cannot be null.");
        }
        InputValidator.validatePositiveQuantity(quantity, "Order item quantity");

        Product product = this.productController.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException(
                    "Product does not exist or has been deleted: " + productId);
        }

        int totalPhysicalStock = product.getTotalStock();
        int availableStock = product.getAvailableStock();
        if (availableStock == 0 && totalPhysicalStock > 0) {
            throw new IllegalArgumentException(
                    "Cannot add product '" + product.getName() + "': all batches in stock have expired.");
        }

        int existingInCart = draft.getQuantityForProduct(product.getId());
        int newCumulativeQuantity = existingInCart + quantity;
        if (newCumulativeQuantity > availableStock) {
            throw new InsufficientStockException(
                    "Insufficient stock for '" + product.getName() + "'. Available: "
                            + availableStock + ", already in cart: " + existingInCart
                            + ", requested to add: " + quantity + ".");
        }

        draft.addItem(new OrderDetail(draft.getOrderId(), product, quantity));
        Customer customer = (draft.getCustomerId() == null)
                ? null
                : this.customerController.findByIdIncludeDeleted(draft.getCustomerId());
        draft.calculateTotals(customer);
    }

    /**
     * Discards all items in an uncompleted draft Order without touching
     * product inventory or CSV files (BR14).
     *
     * @param draft the draft Order to cancel
     */
    public void cancelDraft(Order draft) {
        if (draft != null) {
            draft.clearItems();
        }
    }

    /**
     * Finalizes a draft Order relative to today by verifying stock sufficiency
     * across all items, deducting batch quantities via FEFO, and persisting
     * both updated inventory and the completed order to CSV (BR8, BR10, BR11,
     * BR13, BR14, BR15, BR16, BR17, BR24).
     *
     * @param draft the non-empty draft Order to finalize
     * @return the finalized and persisted Order
     * @throws IllegalArgumentException   if draft is null, empty, or has a
     *                                    duplicate orderId
     * @throws InsufficientStockException if any line item exceeds available
     *                                    non-expired stock
     */
    public Order finalizeOrder(Order draft) throws InsufficientStockException {
        if (draft == null) {
            throw new IllegalArgumentException("Draft order cannot be null.");
        }
        if (draft.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item before checkout.");
        }
        if (existsById(draft.getOrderId())) {
            throw new IllegalArgumentException(
                    "Order ID '" + draft.getOrderId() + "' has already been finalized.");
        }

        for (OrderDetail item : draft.getItems()) {
            Product product = this.productController.findById(item.getProductId());
            if (product == null) {
                throw new IllegalArgumentException(
                        "Product '" + item.getProductId() + "' is no longer active.");
            }
            int available = product.getAvailableStock();
            if (available < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for '" + product.getName() + "'. Requested: "
                                + item.getQuantity() + ", Available: " + available + ".");
            }
        }

        Customer customer = (draft.getCustomerId() == null)
                ? null
                : this.customerController.findByIdIncludeDeleted(draft.getCustomerId());
        draft.calculateTotals(customer);

        for (OrderDetail item : draft.getItems()) {
            this.productController.deductStockFEFO(item.getProductId(), item.getQuantity());
        }

        this.orders.add(draft);
        saveToCsv();
        return draft;
    }

    /**
     * Finds a completed Order by its identifier (case-insensitive, supporting
     * shorthand like "O1" to "O00001") (BR33).
     *
     * @param orderId the order ID to look up
     * @return the matching Order, or null if not found
     */
    public Order findById(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return null;
        }
        String lookupId = InputValidator.tryNormalizeOrderId(orderId);
        if (lookupId == null) {
            lookupId = orderId.trim();
        }
        for (Order order : this.orders) {
            if (order.getOrderId().equalsIgnoreCase(lookupId)) {
                return order;
            }
        }
        return null;
    }

    /**
     * Searches completed orders by matching keyword against Order ID, Customer ID,
     * or order date string (e.g. "2026", "10/2026", "19/10/2026").
     *
     * @param keyword the search query
     * @return an unmodifiable list of matching orders
     */
    public List<Order> searchOrders(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllOrders();
        }
        String lower = keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedOrderId = InputValidator.tryNormalizeOrderId(keyword.trim());
        String normalizedCustomerId = InputValidator.tryNormalizeCustomerId(keyword.trim());
        List<Order> result = new ArrayList<>();
        for (Order order : this.orders) {
            boolean matchOrderId = order.getOrderId().toLowerCase(Locale.ROOT).contains(lower)
                    || (normalizedOrderId != null && order.getOrderId().equalsIgnoreCase(normalizedOrderId));
            Customer cust = (order.getCustomerId() == null) ? null
                    : this.customerController.findByIdIncludeDeleted(order.getCustomerId());
            boolean matchCustName = cust != null && cust.getFullName().toLowerCase(Locale.ROOT).contains(lower);
            boolean matchCustomerId = (order.getCustomerId() != null
                    && (order.getCustomerId().toLowerCase(Locale.ROOT).contains(lower)
                            || (normalizedCustomerId != null
                                    && order.getCustomerId().equalsIgnoreCase(normalizedCustomerId))))
                    || (order.getCustomerId() == null && "guest".contains(lower));
            String dateDisplay = InputValidator.formatDisplayDate(order.getOrderDate().toLocalDate());
            String dateIso = order.getOrderDate().toLocalDate().toString();
            boolean matchDate = dateDisplay.contains(lower) || dateIso.contains(lower);
            if (matchOrderId || matchCustomerId || matchCustName || matchDate) {
                result.add(order);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an unmodifiable list of all completed orders in the system (BR23).
     *
     * @return an unmodifiable list of Order objects
     */
    public List<Order> getAllOrders() {
        return Collections.unmodifiableList(this.orders);
    }
}
