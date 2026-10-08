package fptu.pro192.foodstoremanagement.model;

/**
 * Factory class for creating polymorphic Customer instances based on tier type.
 */
public final class CustomerFactory {

    private CustomerFactory() {
        // Prevent instantiation
    }

    /**
     * Creates a Customer instance (VIPCustomer if type is "VIP", otherwise RegularCustomer).
     *
     * @param type The customer type string (e.g., "VIP", "REGULAR")
     * @param id The customer ID (BR2)
     * @param fullName The full name
     * @param phone The normalized phone number (BR25)
     * @param address The address
     * @param isDeleted The soft deletion status (BR27)
     * @return Concrete Customer instance (VIPCustomer or RegularCustomer)
     */
    public static Customer create(String type, String id, String fullName, String phone,
            String address, boolean isDeleted) {
        Customer customer;
        if (type != null && type.trim().equalsIgnoreCase("VIP")) {
            customer = new VIPCustomer(id, fullName, phone, address, isDeleted);
        } else {
            customer = new RegularCustomer(id, fullName, phone, address, isDeleted);
        }
        return customer;
    }
}
