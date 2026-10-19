package fptu.pro192.foodstoremanagement.model;

import fptu.pro192.foodstoremanagement.util.InputValidator;

/**
 * Abstract base class representing a registered store customer (BR2, BR3,
 * BR15, BR25, BR27). Subclasses define loyalty-tier discount strategies via
 * polymorphism, and the static create method implements the Factory Method
 * pattern.
 */
public abstract class Customer {

    private String id;
    private String fullName;
    private String phone;
    private String address;
    private boolean isDeleted;

    /**
     * Constructs an active Customer instance (BR2, BR3, BR25).
     *
     * @param id       the unique 6-character customer identifier (C00001-C99999)
     * @param fullName the customer's full name
     * @param phone    the customer's Vietnamese mobile phone number
     * @param address  the customer's residential or delivery address
     * @throws IllegalArgumentException if any field violates domain invariants
     */
    protected Customer(String id, String fullName, String phone, String address) {
        this(id, fullName, phone, address, false);
    }

    /**
     * Constructs a Customer instance with an explicit soft-delete flag (BR2, BR3,
     * BR25, BR27).
     *
     * @param id        the unique 6-character customer identifier (C00001-C99999)
     * @param fullName  the customer's full name
     * @param phone     the customer's Vietnamese mobile phone number
     * @param address   the customer's residential or delivery address
     * @param isDeleted true if soft-deleted, false if active
     * @throws IllegalArgumentException if any field violates domain invariants
     */
    protected Customer(String id, String fullName, String phone, String address, boolean isDeleted) {
        setId(id);
        setFullName(fullName);
        setPhone(phone);
        setAddress(address);
        this.isDeleted = isDeleted;
    }

    /**
     * Factory Method that instantiates the concrete Customer subclass matching
     * the provided loyalty tier discriminator (BR15).
     *
     * @param type     the customer tier ("Regular" or "VIP", case-insensitive)
     * @param id       the 6-character customer ID (C00001-C99999)
     * @param fullName the customer's full name
     * @param phone    the customer's phone number
     * @param address  the customer's address
     * @return a RegularCustomer or VIPCustomer instance
     * @throws IllegalArgumentException if type or any attribute is invalid
     */
    public static Customer create(String type, String id, String fullName,
            String phone, String address) {
        return create(type, id, fullName, phone, address, false);
    }

    /**
     * Factory Method that instantiates the concrete Customer subclass matching
     * the provided loyalty tier discriminator and soft-delete state (BR15, BR27).
     *
     * @param type      the customer tier ("Regular" or "VIP", case-insensitive)
     * @param id        the 6-character customer ID (C00001-C99999)
     * @param fullName  the customer's full name
     * @param phone     the customer's phone number
     * @param address   the customer's address
     * @param isDeleted true if soft-deleted, false if active
     * @return a RegularCustomer or VIPCustomer instance
     * @throws IllegalArgumentException if type or any attribute is invalid
     */
    public static Customer create(String type, String id, String fullName,
            String phone, String address, boolean isDeleted) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer type cannot be empty.");
        }
        String normalizedType = type.trim();
        if ("Regular".equalsIgnoreCase(normalizedType)) {
            return new RegularCustomer(id, fullName, phone, address, isDeleted);
        }
        if ("VIP".equalsIgnoreCase(normalizedType)) {
            return new VIPCustomer(id, fullName, phone, address, isDeleted);
        }
        throw new IllegalArgumentException(
                "Unsupported customer type: '" + type + "'. Allowed values are 'Regular' or 'VIP'.");
    }

    /**
     * Calculates the monetary discount amount for a given pre-discount order
     * subtotal (BR15).
     *
     * @param amount the pre-discount order subtotal in VND
     * @return the discount amount in VND
     * @throws IllegalArgumentException if amount is negative
     */
    public abstract double calculateDiscount(double amount);

    /**
     * Returns the fractional discount rate for this customer tier (BR15).
     *
     * @return the discount rate between 0.0 and 1.0
     */
    public abstract double getDiscountRate();

    /**
     * Returns the canonical loyalty tier discriminator ("Regular" or "VIP") (BR15).
     *
     * @return the customer tier string
     */
    public abstract String getCustomerType();

    /**
     * Returns the unique customer identifier.
     *
     * @return the 6-character customer ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets and validates the customer identifier (BR2).
     *
     * @param id the 6-character customer ID (C00001-C99999)
     * @throws IllegalArgumentException if id is invalid
     */
    public final void setId(String id) {
        this.id = InputValidator.validateCustomerId(id);
    }

    /**
     * Returns the customer's full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets and validates the customer's full name (BR3).
     *
     * @param fullName the non-empty customer name
     * @throws IllegalArgumentException if fullName is blank or invalid
     */
    public final void setFullName(String fullName) {
        this.fullName = InputValidator.normalizeText(fullName, "Customer full name");
    }

    /**
     * Returns the customer's normalized 10-digit phone number.
     *
     * @return the 10-digit phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Normalizes and sets the customer's Vietnamese mobile phone number (BR25).
     *
     * @param phone the raw phone number string
     * @throws IllegalArgumentException if phone cannot be normalized to a valid
     *                                  10-digit number
     */
    public final void setPhone(String phone) {
        this.phone = InputValidator.normalizePhone(phone);
    }

    /**
     * Returns the customer's residential or delivery address.
     *
     * @return the customer address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets and validates the customer's address (BR3).
     *
     * @param address the non-empty address string
     * @throws IllegalArgumentException if address is blank or invalid
     */
    public final void setAddress(String address) {
        this.address = InputValidator.normalizeText(address, "Customer address");
    }

    /**
     * Checks whether this customer record has been soft-deleted (BR27).
     *
     * @return true if soft-deleted, false if active
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Sets the soft-delete flag for this customer record (BR27).
     *
     * @param isDeleted true to mark as soft-deleted, false to mark active
     */
    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
