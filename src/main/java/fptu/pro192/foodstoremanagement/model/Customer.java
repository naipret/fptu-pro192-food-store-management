package fptu.pro192.foodstoremanagement.model;

/**
 * Abstract domain model representing a customer entity.
 *
 * BR2: Unique Customer ID format C00001–C99999 (immutable). BR15: Regular customers receive 0%
 * discount. BR16: VIP customers receive 10% discount. BR27: Soft delete (isDeleted = true).
 */
public abstract class Customer {

    protected String id;
    protected String fullName;
    protected String phone;
    protected String address;
    protected boolean isDeleted;

    public Customer() {}

    public Customer(String id, String fullName, String phone, String address) {
        this(id, fullName, phone, address, false);
    }

    public Customer(String id, String fullName, String phone, String address, boolean isDeleted) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.isDeleted = isDeleted;
    }

    /**
     * Strategy contract: Returns discount rate (0.0 to 1.0).
     *
     * @return Discount rate as a double
     */
    public abstract double getDiscountRate();

    /**
     * BR15 & BR16: Calculates discount amount based on subtotal.
     *
     * @param subtotal The subtotal purchase amount
     * @return The computed discount amount
     */
    public double calculateDiscount(double subtotal) {
        if (subtotal < 0) {
            return 0.0;
        }
        return subtotal * getDiscountRate();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
