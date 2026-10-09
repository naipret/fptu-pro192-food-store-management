package fptu.pro192.foodstoremanagement.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain model representing a customer order transaction header and item aggregation.
 *
 * BR8: A product must exist and not be marked as deleted before being added to an order.
 * BR9: Sold quantity must be greater than zero.
 * BR12: An order must contain at least one product item upon checkout.
 * BR13: Total Amount (Subtotal) = Sum(Product Unit Price * Quantity).
 * BR15: Regular customers receive 0% discount.
 * BR16: VIP customers receive 10% discount.
 * BR17: Final Amount = Subtotal - Discount Amount.
 * BR26: All quantities must be positive integers.
 */
public class Order {

    private String orderId;
    private String customerId;
    private LocalDate orderDate;
    private final List<OrderDetail> items = new ArrayList<>();
    private double subtotal;
    private double discountAmount;
    private double finalAmount;
    private boolean isCompleted;

    /**
     * Default constructor initializing order date to current system date.
     */
    public Order() {
        this.orderDate = LocalDate.now();
        this.isCompleted = false;
    }

    /**
     * Constructs a new Order with ID, customer ID, and order date.
     *
     * @param orderId Unique order identifier
     * @param customerId Customer ID associated with order (or null for guest customer)
     * @param orderDate Date the order was placed
     */
    public Order(String orderId, String customerId, LocalDate orderDate) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = (orderDate != null) ? orderDate : LocalDate.now();
        this.isCompleted = false;
    }

    /**
     * Full constructor initializing all order state.
     *
     * @param orderId Unique order identifier
     * @param customerId Customer ID associated with order (or null for guest customer)
     * @param orderDate Date the order was placed
     * @param items List of order line items
     * @param subtotal Subtotal amount before discount
     * @param discountAmount Discount amount deducted
     * @param finalAmount Final payable amount after discount
     * @param isCompleted Status flag indicating completed transaction
     */
    public Order(String orderId, String customerId, LocalDate orderDate, List<OrderDetail> items,
                 double subtotal, double discountAmount, double finalAmount, boolean isCompleted) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = (orderDate != null) ? orderDate : LocalDate.now();
        if (items != null) {
            this.items.addAll(items);
        }
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.isCompleted = isCompleted;
    }

    /**
     * Appends a product item line snapshot to the order.
     *
     * BR8: Product must exist and not be marked as deleted.
     * BR9: Sold quantity must be greater than zero.
     * BR26: All quantities must be positive integers.
     *
     * @param product Active non-deleted food product
     * @param quantity Quantity to purchase (must be greater than zero)
     * @throws IllegalArgumentException If product is null, deleted, or quantity is less than or equal to zero
     */
    public void addItem(Food product, int quantity) {
        if (product == null || product.isDeleted()) {
            throw new IllegalArgumentException("Product must exist and not be deleted (BR8)");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Sold quantity must be greater than zero (BR9, BR26)");
        }
        OrderDetail detail = new OrderDetail(this.orderId, product, quantity);
        this.items.add(detail);
    }

    /**
     * Appends an OrderDetail line item to the order.
     *
     * @param detail Line item to append
     * @throws IllegalArgumentException If detail is null
     */
    public void addItem(OrderDetail detail) {
        if (detail == null) {
            throw new IllegalArgumentException("OrderDetail item cannot be null");
        }
        if (this.orderId != null && (detail.getOrderId() == null || detail.getOrderId().trim().isEmpty())) {
            detail.setOrderId(this.orderId);
        }
        this.items.add(detail);
    }

    /**
     * Recomputes subtotal, discount, and final amount based on customer discount strategy.
     *
     * BR13: Total Amount = Sum(Product Unit Price * Quantity).
     * BR15: Regular customers receive 0% discount.
     * BR16: VIP customers receive 10% discount.
     * BR17: Final Amount = Total Amount - Discount Amount.
     *
     * @param customer Customer associated with the order (or null for guest customer)
     */
    public void calculateTotals(Customer customer) {
        double sum = 0.0;
        for (OrderDetail item : items) {
            sum += item.getSubtotal(); // BR13
        }
        this.subtotal = sum;

        if (customer != null && !customer.isDeleted()) {
            this.discountAmount = customer.calculateDiscount(this.subtotal); // BR15, BR16
        } else {
            this.discountAmount = 0.0;
        }

        this.finalAmount = Math.max(0.0, this.subtotal - this.discountAmount); // BR17
    }

    /**
     * Retrieves the unique order identifier.
     *
     * @return The order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the unique order identifier and cascades to items if unset.
     *
     * @param orderId The order ID to set
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
        if (orderId != null) {
            for (OrderDetail item : items) {
                if (item.getOrderId() == null || item.getOrderId().trim().isEmpty()) {
                    item.setOrderId(orderId);
                }
            }
        }
    }

    /**
     * Retrieves the associated customer ID.
     *
     * @return The customer ID (or null for guest)
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets the associated customer ID.
     *
     * @param customerId The customer ID to set
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * Retrieves the order placement date.
     *
     * @return The order date
     */
    public LocalDate getOrderDate() {
        return orderDate;
    }

    /**
     * Sets the order placement date.
     *
     * @param orderDate The order date to set
     */
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * Returns an unmodifiable list of line items in this order.
     *
     * @return Unmodifiable list of OrderDetail items
     */
    public List<OrderDetail> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Replaces line items in this order.
     *
     * @param newItems List of line items to set
     */
    public void setItems(List<OrderDetail> newItems) {
        this.items.clear();
        if (newItems != null) {
            for (OrderDetail item : newItems) {
                addItem(item);
            }
        }
    }

    /**
     * Clears all line items from the order.
     */
    public void clearItems() {
        this.items.clear();
    }

    /**
     * Retrieves the subtotal amount before discount.
     *
     * @return The subtotal amount
     */
    public double getSubtotal() {
        return subtotal;
    }

    /**
     * Sets the subtotal amount.
     *
     * @param subtotal The subtotal to set
     */
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    /**
     * Retrieves the calculated discount amount.
     *
     * @return The discount amount
     */
    public double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Sets the discount amount.
     *
     * @param discountAmount The discount amount to set
     */
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Retrieves the final payable amount after discount.
     *
     * @return The final amount
     */
    public double getFinalAmount() {
        return finalAmount;
    }

    /**
     * Sets the final payable amount.
     *
     * @param finalAmount The final amount to set
     */
    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }

    /**
     * Checks if the order has been completed and confirmed.
     *
     * @return True if completed, false otherwise
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Sets the completion status of the order.
     *
     * @param completed True if completed, false otherwise
     */
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", orderDate=" + orderDate +
                ", itemsCount=" + items.size() +
                ", subtotal=" + subtotal +
                ", discountAmount=" + discountAmount +
                ", finalAmount=" + finalAmount +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
