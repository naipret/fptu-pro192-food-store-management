package fptu.pro192.foodstoremanagement.model;

/**
 * Concrete customer class representing a regular tier customer.
 *
 * BR15: Regular customers receive no discount (0%).
 */
public class RegularCustomer extends Customer {

    public RegularCustomer() {
        super();
    }

    public RegularCustomer(String id, String fullName, String phone, String address) {
        super(id, fullName, phone, address, false);
    }

    public RegularCustomer(String id, String fullName, String phone, String address,
            boolean isDeleted) {
        super(id, fullName, phone, address, isDeleted);
    }

    /**
     * BR15: Regular customers receive 0% discount.
     *
     * @return 0.0
     */
    @Override
    public double getDiscountRate() {
        return 0.0;
    }
}
