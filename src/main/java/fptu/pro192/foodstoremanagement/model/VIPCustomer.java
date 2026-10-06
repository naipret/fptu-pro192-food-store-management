package fptu.pro192.foodstoremanagement.model;

public class VIPCustomer extends Customer {

    public VIPCustomer() {
        super();
    }

    public VIPCustomer(String id, String fullName, String phone, String address) {
        super(id, fullName, phone, address);
    }

    // BR16: VIP customers receive 10% discount
    @Override
    public double getDiscountRate() {
        return 0.10;
    }
}
