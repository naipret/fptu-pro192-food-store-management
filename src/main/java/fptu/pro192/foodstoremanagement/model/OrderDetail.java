package fptu.pro192.foodstoremanagement.model;

import fptu.pro192.foodstoremanagement.util.InputValidator;

/**
 * Represents a single line item inside a sales Order, capturing an immutable
 * snapshot of the product's selling price at checkout time (BR8, BR9, BR12,
 * BR18, BR26).
 */
public class OrderDetail {

    private String orderId;
    private String productId;
    private int quantity;
    private double unitPrice;

    /**
     * Constructs a validated OrderDetail line item (BR8, BR9, BR12, BR18, BR26).
     *
     * @param orderId   the 6-character order identifier (O00001-O99999)
     * @param productId the 6-character product identifier (P00001-P99999)
     * @param quantity  the positive integer quantity purchased
     * @param unitPrice the positive snapshot unit selling price
     * @throws IllegalArgumentException if any parameter violates domain invariants
     */
    public OrderDetail(String orderId, String productId, int quantity, double unitPrice) {
        setOrderId(orderId);
        setProductId(productId);
        setQuantity(quantity);
        setUnitPrice(unitPrice);
    }

    /**
     * Constructs an OrderDetail by capturing the id and current selling price
     * of an active Product instance (BR8, BR9, BR12, BR26, BR27).
     *
     * @param orderId  the 6-character order identifier (O00001-O99999)
     * @param product  the active Product being purchased
     * @param quantity the positive integer quantity purchased
     * @throws IllegalArgumentException if product is null, soft-deleted, or
     *                                  quantity is invalid
     */
    public OrderDetail(String orderId, Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (product.isDeleted()) {
            throw new IllegalArgumentException(
                    "Deleted product (" + product.getId() + ") cannot be added to an order.");
        }
        setOrderId(orderId);
        setProductId(product.getId());
        setQuantity(quantity);
        setUnitPrice(product.getPrice());
    }

    /**
     * Computes the line-item subtotal as quantity multiplied by unitPrice (BR12).
     *
     * @return the line-item subtotal
     */
    public double getSubTotal() {
        return this.quantity * this.unitPrice;
    }

    /**
     * Alias for getSubTotal for caller compatibility (BR12).
     *
     * @return the line-item subtotal
     */
    public double getSubtotal() {
        return getSubTotal();
    }

    /**
     * Returns the parent order identifier.
     *
     * @return the 6-character order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets and validates the parent order identifier (BR8).
     *
     * @param orderId the 6-character order ID (O00001-O99999)
     * @throws IllegalArgumentException if orderId is invalid
     */
    public final void setOrderId(String orderId) {
        this.orderId = InputValidator.validateOrderId(orderId);
    }

    /**
     * Returns the purchased product identifier.
     *
     * @return the 6-character product ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets and validates the purchased product identifier (BR1, BR18).
     *
     * @param productId the 6-character product ID (P00001-P99999)
     * @throws IllegalArgumentException if productId is invalid
     */
    public final void setProductId(String productId) {
        this.productId = InputValidator.validateProductId(productId);
    }

    /**
     * Returns the integer quantity purchased.
     *
     * @return the positive integer quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets and validates the integer quantity purchased (BR9, BR26).
     *
     * @param quantity the positive integer quantity
     * @throws IllegalArgumentException if quantity is less than or equal to zero
     */
    public final void setQuantity(int quantity) {
        this.quantity = InputValidator.validatePositiveQuantity(quantity, "Order item quantity");
    }

    /**
     * Returns the historical unit selling price captured at checkout.
     *
     * @return the positive unit selling price
     */
    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Sets and validates the captured unit selling price (BR4, BR12).
     *
     * @param unitPrice the positive unit selling price
     * @throws IllegalArgumentException if unitPrice is not greater than zero
     */
    public final void setUnitPrice(double unitPrice) {
        this.unitPrice = InputValidator.validatePositivePrice(unitPrice);
    }
}
