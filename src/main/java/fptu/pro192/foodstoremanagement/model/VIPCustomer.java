package fptu.pro192.foodstoremanagement.model;

/**
 * Concrete customer subclass representing a VIP loyalty tier customer
 * eligible for a 10% discount on orders (BR15).
 */
public class VIPCustomer extends Customer {

    /**
     * Fractional discount rate for VIP tier customers (10%) (BR15).
     */
    public static final double DISCOUNT_RATE = 0.10;

    /**
     * Constructs an active VIPCustomer (BR2, BR3, BR15, BR25).
     *
     * @param id the 6-character customer ID (C00001-C99999)
     * @param fullName the customer's full name
     * @param phone the customer's phone number
     * @param address the customer's address
     */
    public VIPCustomer(String id, String fullName, String phone, String address) {
        super(id, fullName, phone, address, false);
    }

    /**
     * Constructs a VIPCustomer with an explicit soft-delete state (BR2, BR3, BR15, BR25, BR27).
     *
     * @param id the 6-character customer ID (C00001-C99999)
     * @param fullName the customer's full name
     * @param phone the customer's phone number
     * @param address the customer's address
     * @param isDeleted true if soft-deleted, false if active
     */
    public VIPCustomer(String id, String fullName, String phone, String address, boolean isDeleted) {
        super(id, fullName, phone, address, isDeleted);
    }

    /**
     * Calculates the 10% loyalty discount for a VIP customer (BR15).
     *
     * @param amount the pre-discount order subtotal
     * @return 10% of the subtotal
     * @throws IllegalArgumentException if amount is negative
     */
    @Override
    public double calculateDiscount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0.0) {
            throw new IllegalArgumentException("Order subtotal cannot be negative.");
        }
        return amount * DISCOUNT_RATE;
    }

    /**
     * Returns the fractional discount rate for VIP customers (0.10) (BR15).
     *
     * @return 0.10
     */
    @Override
    public double getDiscountRate() {
        return DISCOUNT_RATE;
    }

    /**
     * Returns the canonical customer tier discriminator ("VIP") (BR15).
     *
     * @return "VIP"
     */
    @Override
    public String getCustomerType() {
        return "VIP";
    }
}
