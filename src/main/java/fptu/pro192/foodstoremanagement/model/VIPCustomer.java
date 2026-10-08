package fptu.pro192.foodstoremanagement.model;

/**
 * Concrete customer class representing a VIP tier customer.
 *
 * BR16: VIP customers receive 10% discount (0.10).
 */
public class VIPCustomer extends Customer {

    public VIPCustomer() {
        super();
    }

    public VIPCustomer(String id, String fullName, String phone, String address) {
        super(id, fullName, phone, address, false);
    }

    public VIPCustomer(String id, String fullName, String phone, String address,
            boolean isDeleted) {
        super(id, fullName, phone, address, isDeleted);
    }

    /**
     * BR16: VIP customers receive 10% discount (0.10).
     *
     * @return 0.10
     */
    @Override
    public double getDiscountRate() {
        return 0.10;
    }
}
