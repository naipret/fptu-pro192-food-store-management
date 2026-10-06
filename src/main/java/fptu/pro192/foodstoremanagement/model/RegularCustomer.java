package fptu.pro192.foodstoremanagement.model;

public class RegularCustomer extends Customer {

    public RegularCustomer() {
        super();
    }

    public RegularCustomer(String id, String fullName, String phone, String address) {
        super(id, fullName, phone, address);
    }

    // BR15: Regular customers receive no discount
    @Override
    public double getDiscountRate() {
        return 0.0;
    }
}
