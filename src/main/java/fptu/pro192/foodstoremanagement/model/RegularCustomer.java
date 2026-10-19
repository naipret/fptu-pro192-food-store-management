package fptu.pro192.foodstoremanagement.model;

/**
 * Concrete customer subclass representing a standard Regular tier customer
 * eligible for a 0% discount on orders (BR15).
 */
public class RegularCustomer extends Customer {

    /**
     * Fractional discount rate for Regular tier customers (0%) (BR15).
     */
    public static final double DISCOUNT_RATE = 0.0;

    /**
     * Constructs an active RegularCustomer (BR2, BR3, BR15, BR25).
     *
     * @param id the 6-character customer ID (C00001-C99999)
     * @param fullName the customer's full name
     * @param phone the customer's phone number
     * @param address the customer's address
     */
    public RegularCustomer(String id, String fullName, String phone, String address) {
        super(id, fullName, phone, address, false);
    }

    /**
     * Constructs a RegularCustomer with an explicit soft-delete state (BR2, BR3, BR15, BR25, BR27).
     *
     * @param id the 6-character customer ID (C00001-C99999)
     * @param fullName the customer's full name
     * @param phone the customer's phone number
     * @param address the customer's address
     * @param isDeleted true if soft-deleted, false if active
     */
    public RegularCustomer(String id, String fullName, String phone, String address, boolean isDeleted) {
        super(id, fullName, phone, address, isDeleted);
    }

    /**
     * Calculates the discount for a Regular customer, which is always 0.0 VND (BR15).
     *
     * @param amount the pre-discount order subtotal
     * @return 0.0
     * @throws IllegalArgumentException if amount is negative
     */
    @Override
    public double calculateDiscount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0.0) {
            throw new IllegalArgumentException("Order subtotal cannot be negative.");
        }
        return 0.0;
    }

    /**
     * Returns the fractional discount rate for Regular customers (0.0) (BR15).
     *
     * @return 0.0
     */
    @Override
    public double getDiscountRate() {
        return DISCOUNT_RATE;
    }

    /**
     * Returns the canonical customer tier discriminator ("Regular") (BR15).
     *
     * @return "Regular"
     */
    @Override
    public String getCustomerType() {
        return "Regular";
    }
}
