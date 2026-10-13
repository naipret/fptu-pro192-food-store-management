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
     * @param type     The customer type string (e.g., "VIP", "REGULAR")
     * @param id       The customer ID (BR2)
     * @param fullName The full name
     * @param phone    The normalized phone number (BR25)
     * @param address  The address
     * @return Concrete Customer instance (VIPCustomer or RegularCustomer)
     */
    // Hàm 1: Hàm tạo đầy đủ tham số (Dành cho việc đọc dữ liệu từ file CSV)
    public static Customer create(String type, String id, String fullName, String phone, String address, boolean isDeleted) {
        Customer customer;
        if (type != null && type.trim().equalsIgnoreCase("VIP")) {
            customer = new VIPCustomer(id, fullName, phone, address, isDeleted);
        } else {
            customer = new RegularCustomer(id, fullName, phone, address, isDeleted);
        }
        return customer;
    }

    // Hàm 2: Hàm tạo nạp chồng (Overloaded) rút gọn (Dành cho thêm khách hàng mới)
    public static Customer create(String type, String id, String fullName, String phone, String address) {
        // Tái sử dụng Hàm 1 bằng cách gọi lại nó và truyền mặc định tham số cuối là false
        return create(type, id, fullName, phone, address, false);
    }
}
