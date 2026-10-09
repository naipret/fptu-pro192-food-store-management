package fptu.pro192.foodstoremanagement.model;

/**
 * Domain model capturing individual line items in a sales transaction.
 * Preserves an immutable unit price and product name snapshot at the moment of sale.
 *
 * BR5: Product price must be greater than zero.
 * BR8: A product must exist and not be marked as deleted before being added to an order.
 * BR9: Sold quantity must be greater than zero.
 * BR13: Line item amount = unitPrice * quantity.
 * BR26: Quantity must be a positive integer (int).
 */
public class OrderDetail {

    private String orderId;
    private String productId;
    private String productName;
    private String unit;
    private double unitPrice;
    private int quantity;

    /**
     * Default constructor for OrderDetail.
     */
    public OrderDetail() {}

    /**
     * Constructs an OrderDetail with explicit line item fields.
     *
     * BR5: Product price must be greater than zero.
     * BR9: Sold quantity must be greater than zero.
     * BR26: Quantity must be a positive integer (int).
     *
     * @param orderId The parent order ID
     * @param productId The product ID
     * @param productName The snapshot of the product name
     * @param unit The snapshot of the product unit
     * @param unitPrice The snapshot of the unit price (must be greater than zero)
     * @param quantity The quantity being purchased (must be greater than zero)
     * @throws IllegalArgumentException If quantity is less than or equal to zero or unitPrice is less than or equal to zero
     */
    public OrderDetail(String orderId, String productId, String productName, String unit, double unitPrice, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Sold quantity must be greater than zero (BR9, BR26)");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Product unit price must be greater than zero (BR5)");
        }
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    /**
     * Snapshot constructor capturing current price and attributes from an active Food entity.
     *
     * BR8: Product must exist and not be deleted.
     * BR9: Sold quantity must be greater than zero.
     * BR26: Quantity must be a positive integer (int).
     *
     * @param orderId The parent order ID
     * @param product The product being purchased
     * @param quantity The quantity being purchased
     * @throws IllegalArgumentException If product is null, deleted, or quantity is less than or equal to zero
     */
    public OrderDetail(String orderId, Food product, int quantity) {
        if (product == null || product.isDeleted()) {
            throw new IllegalArgumentException("Product must exist and not be marked as deleted (BR8)");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Sold quantity must be greater than zero (BR9, BR26)");
        }
        this.orderId = orderId;
        this.productId = product.getId();
        this.productName = product.getName();
        this.unit = product.getUnit();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
    }

    /**
     * Calculates line item subtotal amount.
     *
     * BR13: Total line item amount = unitPrice * quantity.
     *
     * @return Line item subtotal (unitPrice * quantity)
     */
    public double getSubtotal() {
        return unitPrice * quantity;
    }

    /**
     * Retrieves the associated order ID.
     *
     * @return The order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the associated order ID.
     *
     * @param orderId The order ID to set
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * Retrieves the product ID.
     *
     * @return The product ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the product ID.
     *
     * @param productId The product ID to set
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Retrieves the snapshot product name.
     *
     * @return The product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the snapshot product name.
     *
     * @param productName The product name to set
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Retrieves the unit of measurement.
     *
     * @return The product unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the unit of measurement.
     *
     * @param unit The product unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Retrieves the snapshot unit price.
     *
     * @return The unit price
     */
    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Sets the snapshot unit price.
     *
     * BR5: Product price must be greater than zero.
     *
     * @param unitPrice The unit price to set (must be greater than zero)
     * @throws IllegalArgumentException If unitPrice is less than or equal to zero
     */
    public void setUnitPrice(double unitPrice) {
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Product unit price must be greater than zero (BR5)");
        }
        this.unitPrice = unitPrice;
    }

    /**
     * Retrieves the purchased quantity.
     *
     * @return The purchased quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the purchased quantity.
     *
     * BR9: Sold quantity must be greater than zero.
     * BR26: Quantity must be a positive integer (int).
     *
     * @param quantity The quantity to set (must be greater than zero)
     * @throws IllegalArgumentException If quantity is less than or equal to zero
     */
    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Sold quantity must be greater than zero (BR9, BR26)");
        }
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "OrderDetail{" +
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", unit='" + unit + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
