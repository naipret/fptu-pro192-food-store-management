package fptu.pro192.foodstoremanagement.service;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.Transaction;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.repository.OrderRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Service managing transactional checkout engine and sales order operations.
 * Coordinates draft cart lifecycle, inventory stock and expiry validations, VIP discount strategy,
 * and atomic FEFO batch stock reduction upon sale confirmation.
 *
 * BR8: Product must exist and not be marked as deleted.
 * BR9: Sold quantity must be greater than zero.
 * BR10: Sold quantity cannot exceed available active stock.
 * BR11: Expired food products cannot be sold under any circumstances.
 * BR12: An order must contain at least one product item upon checkout.
 * BR13: Total Amount = Sum(Product Unit Price * Quantity).
 * BR14: Stock quantity is reduced immediately after sales transaction confirmation.
 * BR15: Regular customers receive 0% discount.
 * BR16: VIP customers receive 10% discount.
 * BR17: Final Amount = Total Amount - Discount Amount.
 * BR23: Total revenue calculated exclusively from completed sales transactions.
 * BR24: FEFO inventory batch allocation.
 * BR26: Quantities must be positive integers.
 */
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductService productService;
    private final CustomerService customerService;

    /**
     * Default constructor wiring production repository and service dependencies.
     */
    public OrderService() {
        this(new OrderRepository(), new ProductService(), new CustomerService(new CustomerRepository()));
    }

    /**
     * Parameterized constructor enabling dependency injection for testing and customization.
     *
     * @param orderRepo The repository managing order persistence
     * @param productService The service managing products and inventory stock
     * @param customerService The service managing customer operations
     * @throws IllegalArgumentException If any dependency is null
     */
    public OrderService(OrderRepository orderRepo, ProductService productService, CustomerService customerService) {
        if (orderRepo == null) {
            throw new IllegalArgumentException("OrderRepository cannot be null.");
        }
        if (productService == null) {
            throw new IllegalArgumentException("ProductService cannot be null.");
        }
        if (customerService == null) {
            throw new IllegalArgumentException("CustomerService cannot be null.");
        }
        this.orderRepo = orderRepo;
        this.productService = productService;
        this.customerService = customerService;
    }

    /**
     * Task B8: Creates a fresh in-memory draft order.
     * <p>
     * <b>Crucial Invariant:</b> Stock is NEVER deducted and CSV storage is NEVER written
     * while the order remains in draft status.
     *
     * @param orderId Unique order identifier
     * @param customerId Customer ID associated with order (or null/blank for guest customer)
     * @param orderDate Order placement date
     * @return Fresh in-memory Order instance
     * @throws IllegalArgumentException If orderId is null or empty, or already exists, or if customerId does not exist
     */
    public Order createDraftOrder(String orderId, String customerId, LocalDate orderDate) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống.");
        }
        String cleanOrderId = orderId.trim();
        if (orderRepo.findById(cleanOrderId) != null) {
            throw new IllegalArgumentException("Mã đơn hàng đã tồn tại trong hệ thống: " + cleanOrderId);
        }

        String cleanCustomerId = (customerId != null && !customerId.trim().isEmpty()) ? customerId.trim() : null;
        if (cleanCustomerId != null) {
            Customer customer = customerService.findById(cleanCustomerId);
            if (customer == null) {
                throw new IllegalArgumentException("Không tìm thấy khách hàng với mã: " + cleanCustomerId);
            }
        }

        LocalDate date = (orderDate != null) ? orderDate : LocalDate.now();
        return new Order(cleanOrderId, cleanCustomerId, date);
    }

    /**
     * Convenience overload for {@link #createDraftOrder(String, String, LocalDate)} using current system date.
     *
     * @param orderId Unique order identifier
     * @param customerId Customer ID (or null/blank for guest customer)
     * @return Fresh in-memory Order instance
     */
    public Order createDraftOrder(String orderId, String customerId) {
        return createDraftOrder(orderId, customerId, LocalDate.now());
    }

    /**
     * Task B9: Validates active stock (BR10) and expiration (BR11) before adding a line item snapshot to draft.
     * Automatically recomputes order subtotal, customer tier discount, and final payable amount (BR13, BR15, BR16, BR17).
     *
     * BR8: Product must exist and not be marked as deleted.
     * BR9: Sold quantity must be greater than zero.
     * BR10: Quantity sold cannot exceed available active stock.
     * BR11: Expired food products cannot be sold under any circumstances.
     * BR13: Line item amount = unitPrice * quantity.
     * BR26: Quantities must be positive integers.
     *
     * @param draft The in-memory draft order
     * @param productId The 6-character product ID (P00001 - P99999)
     * @param quantity The integer quantity to add (must be greater than zero)
     * @param today Reference date for evaluating batch expiration and shelf life
     * @throws IllegalArgumentException If product does not exist, is deleted, is expired, or quantity is non-positive
     * @throws InsufficientStockException If requested quantity exceeds active stock
     */
    public void addItemToDraft(Order draft, String productId, int quantity, LocalDate today) {
        if (draft == null) {
            throw new IllegalArgumentException("Draft order cannot be null.");
        }
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Failed to add product. Sold quantity must be greater than zero.");
        }

        LocalDate refDate = (today != null) ? today : LocalDate.now();
        Food product = productService.findById(productId);
        if (product == null || product.isDeleted()) {
            throw new IllegalArgumentException("Failed to add product. Product does not exist or has been deleted.");
        }

        // BR11: Expired food products cannot be sold under any circumstances
        List<Batch> activeBatches = product.getActiveBatches(refDate);
        if (activeBatches == null || activeBatches.isEmpty()) {
            throw new IllegalArgumentException("Failed to add product. This food product has expired.");
        }

        // BR10: Sold quantity cannot exceed available active stock
        int activeStock = product.getTotalStock(refDate);
        int existingInCart = 0;
        for (OrderDetail item : draft.getItems()) {
            if (item.getProductId().equalsIgnoreCase(productId)) {
                existingInCart += item.getQuantity();
            }
        }

        if (existingInCart + quantity > activeStock) {
            throw new InsufficientStockException("Failed to add product. Insufficient stock.");
        }

        // Add line item to draft: increment existing line item quantity or append fresh snapshot
        boolean foundInCart = false;
        for (OrderDetail item : draft.getItems()) {
            if (item.getProductId().equalsIgnoreCase(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                foundInCart = true;
                break;
            }
        }
        if (!foundInCart) {
            draft.addItem(product, quantity);
        }

        // BR13, BR15, BR16, BR17: Recompute totals based on attached customer discount tier
        Customer customer = null;
        if (draft.getCustomerId() != null && !draft.getCustomerId().trim().isEmpty()) {
            customer = customerService.findById(draft.getCustomerId());
        }
        draft.calculateTotals(customer);
    }

    /**
     * Convenience overload for {@link #addItemToDraft(Order, String, int, LocalDate)} using current system date.
     *
     * @param draft The in-memory draft order
     * @param productId The product ID
     * @param quantity The quantity to add
     */
    public void addItemToDraft(Order draft, String productId, int quantity) {
        addItemToDraft(draft, productId, quantity, LocalDate.now());
    }

    /**
     * Task B10: Finalizes order, executes atomic FEFO batch stock reduction, records transaction, and persists state.
     *
     * BR12: An order must contain at least one product item upon checkout.
     * BR14: Stock quantity is reduced immediately after sales transaction confirmation.
     * BR23: Total revenue calculated exclusively from completed sales transactions.
     * BR24: FEFO batch stock allocation.
     *
     * @param draft The in-memory draft order to confirm
     * @param today Reference date for stock deduction and transaction recording
     * @param paymentMethod Method used for payment (e.g., CASH, BANK_TRANSFER)
     * @return Completed Transaction record
     * @throws IllegalArgumentException If draft is null, or empty (BR12)
     */
    public synchronized Transaction confirmSale(Order draft, LocalDate today, String paymentMethod) {
        if (draft == null) {
            throw new IllegalArgumentException("Draft order cannot be null.");
        }
        if (draft.getItems() == null || draft.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot confirm empty order. Order must contain at least one item.");
        }

        LocalDate refDate = (today != null) ? today : LocalDate.now();

        // 1. Recompute totals with attached customer to guarantee exact billing
        Customer customer = null;
        if (draft.getCustomerId() != null && !draft.getCustomerId().trim().isEmpty()) {
            customer = customerService.findById(draft.getCustomerId());
        }
        draft.calculateTotals(customer);

        // 2. Execute FEFO stock deduction for each purchased line item (BR14, BR24)
        for (OrderDetail item : draft.getItems()) {
            productService.deductStockFEFO(item.getProductId(), item.getQuantity(), refDate);
        }

        // 3. Mark order as completed
        draft.setCompleted(true);
        draft.setOrderDate(refDate);

        // 4. Create Transaction record (BR23)
        String txId = "TX-" + draft.getOrderId();
        Transaction tx = new Transaction(txId, draft, paymentMethod);
        tx.setTransactionDate(refDate);

        // 5. Persist transaction and order to CSV atomically
        orderRepo.recordTransaction(tx);
        orderRepo.saveOrder(draft);

        return tx;
    }

    /**
     * Convenience overload for {@link #confirmSale(Order, LocalDate, String)} using current date.
     *
     * @param draft The draft order to confirm
     * @param paymentMethod Method of payment
     * @return Completed Transaction record
     */
    public synchronized Transaction confirmSale(Order draft, String paymentMethod) {
        return confirmSale(draft, LocalDate.now(), paymentMethod);
    }

    /**
     * Cancels and discards a draft order without altering physical inventory or CSV persistence.
     *
     * @param draft The draft order to cancel
     */
    public void cancelDraft(Order draft) {
        if (draft != null) {
            draft.clearItems();
            draft.setCompleted(false);
        }
    }

    /**
     * Finds an order by its unique order ID.
     *
     * @param orderId The order ID to search
     * @return Matching Order, or null if not found
     */
    public Order findById(String orderId) {
        return orderRepo.findById(orderId);
    }

    /**
     * Returns an unmodifiable list of all recorded orders.
     *
     * @return List of orders
     */
    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    /**
     * Returns an unmodifiable list of completed orders.
     *
     * BR23: Revenue reports only calculate from completed orders.
     *
     * @return List of completed orders
     */
    public List<Order> getCompletedOrders() {
        return orderRepo.findCompletedOrders();
    }

    /**
     * Returns an unmodifiable list of all financial transactions.
     *
     * BR23: Total revenue is calculated exclusively from completed sales transactions.
     *
     * @return List of transactions
     */
    public List<Transaction> getAllTransactions() {
        return orderRepo.findAllTransactions();
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
     * Returns underlying ProductService instance.
     *
     * @return ProductService
     */
    public ProductService getProductService() {
        return productService;
    }

    /**
     * Returns underlying CustomerService instance.
     *
     * @return CustomerService
     */
    public CustomerService getCustomerService() {
        return customerService;
    }
}

