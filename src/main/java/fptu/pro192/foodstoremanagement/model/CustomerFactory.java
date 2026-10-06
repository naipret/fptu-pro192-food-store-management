package fptu.pro192.foodstoremanagement.model;

public class CustomerFactory {
    public static Customer create(String type, String id, String fullName, String phone, String address, boolean isDeleted) {
        Customer customer;

        if (type != null && type.equalsIgnoreCase("VIP")) {
            customer = new VIPCustomer(id, fullName, phone, address);
        } else {
            customer = new RegularCustomer(id, fullName, phone, address);
        }
        customer.setDeleted(isDeleted);
        return customer;
    }
}
