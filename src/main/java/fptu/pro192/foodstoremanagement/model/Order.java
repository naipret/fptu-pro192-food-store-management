package fptu.pro192.foodstoremanagement.model;

import fptu.pro192.foodstoremanagement.util.InputValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a customer sales order header and its aggregated OrderDetail
 * line items (BR8, BR9, BR12, BR13, BR15, BR16, BR17, BR28, BR33).
 */
public class Order {

    /**
     * Canonical CSV discriminator for walk-in guest customers (BR28).
     */
    public static final String GUEST_CUSTOMER_ID = "GUEST";

    private String orderId;
    private String customerId;
    private LocalDateTime orderDate;
    private final List<OrderDetail> items;
    private double totalAmount;
    private double discount;
    private double finalAmount;

    /**
     * Constructs a new empty draft Order at the specified date and time (BR28,
     * BR33).
     *
     * @param orderId    the 6-character order identifier (O00001-O99999)
     * @param customerId the 6-character customer ID (C00001-C99999), or null/GUEST
     *                   for walk-in guests
     * @param orderDate  the timestamp when the order was created
     * @throws IllegalArgumentException if orderId or orderDate is invalid
     */
    public Order(String orderId, String customerId, LocalDateTime orderDate) {
        this(orderId, customerId, orderDate, new ArrayList<OrderDetail>(), 0.0, 0.0, 0.0);
    }

    /**
     * Reconstructs a persisted Order with pre-loaded line items and financial
     * totals (BR13, BR17, BR28, BR33).
     *
     * @param orderId     the 6-character order identifier (O00001-O99999)
     * @param customerId  the 6-character customer ID, or null/GUEST for walk-in
     *                    guests
     * @param orderDate   the timestamp of the order
     * @param items       the list of OrderDetail line items, may be null or empty
     *                    for drafts
     * @param totalAmount the gross pre-discount total
     * @param discount    the loyalty discount amount
     * @param finalAmount the net payable amount
     * @throws IllegalArgumentException if any identifier, date, or monetary amount
     *                                  is invalid
     */
    public Order(String orderId, String customerId, LocalDateTime orderDate,
            List<OrderDetail> items, double totalAmount, double discount, double finalAmount) {
        setOrderId(orderId);
        setCustomerId(customerId);
        setOrderDate(orderDate);
        this.items = new ArrayList<>();
        if (items != null) {
            for (OrderDetail item : items) {
                addItem(item);
            }
        }
        setTotalAmount(totalAmount);
        setDiscount(discount);
        setFinalAmount(finalAmount);
    }

    /**
     * Adds an OrderDetail line item to this order. If a line item for the same
     * productId already exists in the order, the quantities are merged into the
     * existing line item (BR8, BR9, BR12).
     *
     * @param item the OrderDetail line item to add
     * @throws IllegalArgumentException if item is null
     */
    public void addItem(OrderDetail item) {
        if (item == null) {
            throw new IllegalArgumentException("Order detail item cannot be null.");
        }
        item.setOrderId(this.orderId);
        for (OrderDetail existing : this.items) {
            if (existing.getProductId().equalsIgnoreCase(item.getProductId())) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                existing.setUnitPrice(item.getUnitPrice());
                recomputeRawTotal();
                return;
            }
        }
        this.items.add(item);
        recomputeRawTotal();
    }

    /**
     * Clears all line items and resets financial totals to zero when cancelling a
     * draft.
     */
    public void clearItems() {
        this.items.clear();
        this.totalAmount = 0.0;
        this.discount = 0.0;
        this.finalAmount = 0.0;
    }

    /**
     * Recomputes totalAmount, discount, and finalAmount using the polymorphic
     * discount strategy of the associated Customer (or 0.0 for guest orders)
     * (BR12, BR13, BR15, BR16, BR17, BR28).
     *
     * @param customer the associated Customer instance, or null for guest checkout
     */
    public void calculateTotals(Customer customer) {
        double sum = 0.0;
        for (OrderDetail item : this.items) {
            sum += item.getSubTotal();
        }
        this.totalAmount = sum;
        double calculatedDiscount = (customer == null) ? 0.0 : customer.calculateDiscount(sum);
        if (calculatedDiscount < 0.0) {
            calculatedDiscount = 0.0;
        }
        if (calculatedDiscount > sum) {
            calculatedDiscount = sum;
        }
        this.discount = calculatedDiscount;
        this.finalAmount = Math.max(0.0, this.totalAmount - this.discount);
    }

    private void recomputeRawTotal() {
        double sum = 0.0;
        for (OrderDetail item : this.items) {
            sum += item.getSubTotal();
        }
        this.totalAmount = sum;
        this.finalAmount = Math.max(0.0, this.totalAmount - this.discount);
    }

    /**
     * Returns the total integer quantity of a specific product currently in this
     * order.
     *
     * @param productId the product ID to inspect
     * @return the quantity already in the order, or 0 if not present
     */
    public int getQuantityForProduct(String productId) {
        if (productId == null) {
            return 0;
        }
        for (OrderDetail item : this.items) {
            if (item.getProductId().equalsIgnoreCase(productId.trim())) {
                return item.getQuantity();
            }
        }
        return 0;
    }

    /**
     * Returns the unique order identifier.
     *
     * @return the 6-character order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets and validates the order identifier, cascading the update to any
     * attached line items (BR33).
     *
     * @param orderId the 6-character order ID (O00001-O99999)
     * @throws IllegalArgumentException if orderId is invalid
     */
    public final void setOrderId(String orderId) {
        String normalized = InputValidator.validateOrderId(orderId);
        this.orderId = normalized;
        if (this.items != null) {
            for (OrderDetail item : this.items) {
                item.setOrderId(normalized);
            }
        }
    }

    /**
     * Returns the associated customer identifier, or null for walk-in guest orders.
     *
     * @return the 6-character customer ID, or null if guest
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets the associated customer identifier (BR2, BR28, BR33). Null, blank, or
     * "GUEST" values are stored as null in memory to represent a walk-in guest.
     *
     * @param customerId the 6-character customer ID, or null/GUEST
     * @throws IllegalArgumentException if non-guest customerId is invalid
     */
    public final void setCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()
                || GUEST_CUSTOMER_ID.equalsIgnoreCase(customerId.trim())) {
            this.customerId = null;
            return;
        }
        this.customerId = InputValidator.validateCustomerId(customerId);
    }

    /**
     * Returns the timestamp when the order was created.
     *
     * @return the order LocalDateTime
     */
    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    /**
     * Sets the timestamp of the order.
     *
     * @param orderDate the non-null order LocalDateTime
     * @throws IllegalArgumentException if orderDate is null
     */
    public final void setOrderDate(LocalDateTime orderDate) {
        if (orderDate == null) {
            throw new IllegalArgumentException("Order date cannot be null.");
        }
        this.orderDate = orderDate;
    }

    /**
     * Returns an unmodifiable list of the line items in this order.
     *
     * @return an unmodifiable list of OrderDetail objects
     */
    public List<OrderDetail> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    /**
     * Returns the pre-discount gross total amount (BR13).
     *
     * @return the gross total amount
     */
    public double getTotalAmount() {
        return totalAmount;
    }

    /**
     * Sets the pre-discount gross total amount (BR13).
     *
     * @param totalAmount the non-negative gross total amount
     * @throws IllegalArgumentException if totalAmount is negative
     */
    public final void setTotalAmount(double totalAmount) {
        if (Double.isNaN(totalAmount) || Double.isInfinite(totalAmount) || totalAmount < 0.0) {
            throw new IllegalArgumentException("Order total amount cannot be negative.");
        }
        this.totalAmount = totalAmount;
    }

    /**
     * Returns the loyalty discount amount (BR13, BR15, BR16).
     *
     * @return the discount amount
     */
    public double getDiscount() {
        return discount;
    }

    /**
     * Sets the loyalty discount amount (BR13, BR15, BR16).
     *
     * @param discount the non-negative discount amount
     * @throws IllegalArgumentException if discount is negative
     */
    public final void setDiscount(double discount) {
        if (Double.isNaN(discount) || Double.isInfinite(discount) || discount < 0.0) {
            throw new IllegalArgumentException("Order discount cannot be negative.");
        }
        this.discount = discount;
    }

    /**
     * Returns the final net payable amount after discount (BR17).
     *
     * @return the net payable amount
     */
    public double getFinalAmount() {
        return finalAmount;
    }

    /**
     * Sets the final net payable amount (BR17).
     *
     * @param finalAmount the non-negative final payable amount
     * @throws IllegalArgumentException if finalAmount is negative
     */
    public final void setFinalAmount(double finalAmount) {
        if (Double.isNaN(finalAmount) || Double.isInfinite(finalAmount) || finalAmount < 0.0) {
            throw new IllegalArgumentException("Order final amount cannot be negative.");
        }
        this.finalAmount = finalAmount;
    }
}
