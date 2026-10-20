package fptu.pro192.foodstoremanagement.controller;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.util.CsvUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Controller managing customer registration, loyalty tier updates, phone
 * normalization and uniqueness checks, soft deletion, search queries, and
 * atomic CSV persistence (BR2, BR3, BR15, BR25, BR27).
 */
public class CustomerController {

    /**
     * Default file path for the customers CSV storage.
     */
    public static final String DEFAULT_CUSTOMERS_CSV = "data/customers.csv";

    private static final String[] CSV_HEADERS = {
            "id", "fullName", "phone", "address", "customerType", "isDeleted"
    };

    private final Path csvPath;
    private final List<Customer> customers;

    /**
     * Constructs a CustomerController bound to the default data/customers.csv path.
     */
    public CustomerController() {
        this(Paths.get(DEFAULT_CUSTOMERS_CSV));
    }

    /**
     * Constructs a CustomerController bound to the specified Path and loads
     * existing customer records from disk.
     *
     * @param csvPath the Path for customers.csv
     * @throws IllegalArgumentException if csvPath is null
     */
    public CustomerController(Path csvPath) {
        if (csvPath == null) {
            throw new IllegalArgumentException("Customer CSV path cannot be null.");
        }
        this.csvPath = csvPath;
        this.customers = new ArrayList<>();
        loadFromCsv();
    }

    /**
     * Loads all customer records from customers.csv, skipping the header row
     * at line 0 and instantiating RegularCustomer or VIPCustomer via
     * Customer.create (BR2, BR3, BR15, BR25, BR27).
     */
    public final void loadFromCsv() {
        this.customers.clear();
        List<String[]> rows = CsvUtil.readCsv(this.csvPath);
        if (rows.isEmpty()) {
            saveToCsv();
            return;
        }

        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length == 0 || (row.length == 1 && row[0].trim().isEmpty())) {
                continue;
            }
            if (i == 0 && ("id".equalsIgnoreCase(row[0].trim()) || "type".equalsIgnoreCase(row[0].trim()))) {
                continue;
            }
            if (row.length < 6) {
                throw new IllegalStateException(
                        "Corrupted customer CSV row at index " + i + ": expected 6 columns.");
            }
            try {
                String id = InputValidator.validateCustomerId(row[0]);
                String fullName = InputValidator.normalizeText(row[1], "Customer full name");
                String phone = InputValidator.normalizePhone(row[2]);
                String address = InputValidator.normalizeText(row[3], "Customer address");
                String type = row[4].trim();
                boolean isDeleted = Boolean.parseBoolean(row[5].trim());

                Customer customer = Customer.create(type, id, fullName, phone, address, isDeleted);
                this.customers.add(customer);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Corrupted record in customer CSV file at row " + i + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * Atomically writes all customer records (including soft-deleted ones) to
     * customers.csv with a standard header row at line 0 (BR27).
     */
    public final void saveToCsv() {
        List<String[]> rows = new ArrayList<>();
        rows.add(CSV_HEADERS);

        for (Customer customer : this.customers) {
            rows.add(new String[] {
                    customer.getId(),
                    customer.getFullName(),
                    customer.getPhone(),
                    customer.getAddress(),
                    customer.getCustomerType(),
                    String.valueOf(customer.isDeleted())
            });
        }
        CsvUtil.writeAtomic(this.csvPath, rows);
    }

    /**
     * Generates the next available 6-character Customer ID (C00001-C99999),
     * checking both active and soft-deleted records so deleted IDs are never
     * reused (BR2, BR27).
     *
     * @return the next canonical Customer ID
     */
    public String generateNextCustomerId() {
        int maxNumber = 0;
        for (Customer customer : this.customers) {
            String id = customer.getId();
            if (id != null && id.length() == 6 && (id.charAt(0) == 'C' || id.charAt(0) == 'c')) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (maxNumber >= 99999) {
            throw new IllegalStateException("Maximum Customer ID limit (C99999) reached.");
        }
        return String.format(Locale.US, "C%05d", maxNumber + 1);
    }

    /**
     * Checks whether a Customer ID already exists in the system across both
     * active and soft-deleted records (BR2, BR27).
     *
     * @param id the customer ID to check
     * @return true if the ID is already used
     */
    public boolean existsById(String id) {
        return findByIdIncludeDeleted(id) != null;
    }

    /**
     * Registers a new customer after verifying ID uniqueness across all records
     * and phone uniqueness across active customers, then persists to CSV
     * (BR2, BR3, BR15, BR25, BR27).
     *
     * @param id       the 6-character customer ID (C00001-C99999)
     * @param fullName the customer's full name
     * @param phone    the customer's Vietnamese mobile phone number
     * @param address  the customer's residential address
     * @param type     the loyalty tier ("Regular" or "VIP")
     * @return the newly created and persisted Customer instance
     * @throws IllegalArgumentException if any field is invalid or ID/phone is
     *                                  duplicate
     */
    public Customer registerCustomer(String id, String fullName, String phone,
            String address, String type) {
        String validatedId = InputValidator.validateCustomerId(id);
        if (existsById(validatedId)) {
            throw new IllegalArgumentException(
                    "Customer ID '" + validatedId + "' already exists in the system.");
        }
        String normalizedPhone = InputValidator.normalizePhone(phone);
        Customer existingByPhone = findByPhone(normalizedPhone);
        if (existingByPhone != null) {
            throw new IllegalArgumentException(
                    "Phone number '" + normalizedPhone + "' is already registered to customer "
                            + existingByPhone.getId() + ".");
        }

        Customer created = Customer.create(type, validatedId, fullName,
                normalizedPhone, address, false);
        this.customers.add(created);
        saveToCsv();
        return created;
    }

    /**
     * Updates an active customer's attributes selectively and persists to CSV
     * (BR3, BR15, BR25). Passing null or blank for any parameter retains the
     * customer's existing value for that attribute.
     *
     * @param id         the customer ID to update
     * @param newName    the new full name, or null/blank to keep existing
     * @param newPhone   the new phone number, or null/blank to keep existing
     * @param newAddress the new address, or null/blank to keep existing
     * @param newType    the new loyalty tier ("Regular" or "VIP"), or null/blank to
     *                   keep existing
     * @return the updated Customer instance
     * @throws IllegalArgumentException if the customer is not found or any updated
     *                                  field is invalid/duplicate
     */
    public Customer updateCustomer(String id, String newName, String newPhone,
            String newAddress, String newType) {
        String lookupId = InputValidator.normalizeCustomerId(id);
        int targetIndex = -1;
        Customer existing = null;

        for (int i = 0; i < this.customers.size(); i++) {
            Customer c = this.customers.get(i);
            if (!c.isDeleted() && c.getId().equalsIgnoreCase(lookupId)) {
                targetIndex = i;
                existing = c;
                break;
            }
        }
        if (existing == null) {
            throw new IllegalArgumentException("Active customer not found with ID: " + id);
        }

        String updatedName = (newName != null && !newName.trim().isEmpty())
                ? InputValidator.normalizeText(newName, "Customer full name")
                : existing.getFullName();

        String updatedPhone = existing.getPhone();
        if (newPhone != null && !newPhone.trim().isEmpty()) {
            updatedPhone = InputValidator.normalizePhone(newPhone);
            Customer owner = findByPhone(updatedPhone);
            if (owner != null && !owner.getId().equalsIgnoreCase(existing.getId())) {
                throw new IllegalArgumentException(
                        "Phone number '" + updatedPhone + "' is already used by customer "
                                + owner.getId() + ".");
            }
        }

        String updatedAddress = (newAddress != null && !newAddress.trim().isEmpty())
                ? InputValidator.normalizeText(newAddress, "Customer address")
                : existing.getAddress();

        String updatedType = (newType != null && !newType.trim().isEmpty())
                ? newType.trim()
                : existing.getCustomerType();

        Customer replacement = Customer.create(updatedType, existing.getId(),
                updatedName, updatedPhone, updatedAddress, false);
        this.customers.set(targetIndex, replacement);
        saveToCsv();
        return replacement;
    }

    /**
     * Soft-deletes an active customer by setting isDeleted to true and
     * persisting to CSV (BR27).
     *
     * @param id the customer ID to soft-delete
     * @throws IllegalArgumentException if no active customer with id exists
     */
    public void deleteCustomer(String id) {
        Customer existing = findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Active customer not found with ID: " + id);
        }
        existing.setDeleted(true);
        saveToCsv();
    }

    /**
     * Finds an active (non-deleted) Customer by ID (BR2, BR27).
     *
     * @param id the customer ID (supports shorthand like "C1" or "C00001")
     * @return the matching active Customer, or null if not found or soft-deleted
     */
    public Customer findById(String id) {
        Customer customer = findByIdIncludeDeleted(id);
        if (customer != null && !customer.isDeleted()) {
            return customer;
        }
        return null;
    }

    /**
     * Finds a Customer by ID across both active and soft-deleted records so
     * historical orders and reports can resolve customer names (BR18, BR27).
     *
     * @param id the customer ID to look up
     * @return the matching Customer, or null if not found
     */
    public Customer findByIdIncludeDeleted(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        String lookupId;
        try {
            lookupId = InputValidator.normalizeCustomerId(id);
        } catch (IllegalArgumentException e) {
            lookupId = id.trim();
        }
        for (Customer customer : this.customers) {
            if (customer.getId().equalsIgnoreCase(lookupId)) {
                return customer;
            }
        }
        return null;
    }

    /**
     * Finds an active Customer by phone number, normalizing the query first (BR25,
     * BR27).
     *
     * @param rawPhone the raw phone string to look up
     * @return the matching active Customer, or null if not found or invalid phone
     *         format
     */
    public Customer findByPhone(String rawPhone) {
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            return null;
        }
        String normalized;
        try {
            normalized = InputValidator.normalizePhone(rawPhone);
        } catch (IllegalArgumentException e) {
            return null;
        }
        for (Customer customer : this.customers) {
            if (!customer.isDeleted() && customer.getPhone().equals(normalized)) {
                return customer;
            }
        }
        return null;
    }

    /**
     * Returns an unmodifiable list of all active (non-deleted) customers (BR27).
     *
     * @return an unmodifiable list of active Customer objects
     */
    public List<Customer> getAllActive() {
        List<Customer> active = new ArrayList<>();
        for (Customer customer : this.customers) {
            if (!customer.isDeleted()) {
                active.add(customer);
            }
        }
        return Collections.unmodifiableList(active);
    }

    /**
     * Searches active customers by matching the keyword against customer ID,
     * full name, or phone number (case-insensitive). Returns all active
     * customers if keyword is null or blank.
     *
     * @param keyword the search keyword
     * @return an unmodifiable list of matching active customers
     */
    public List<Customer> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActive();
        }
        String lower = keyword.trim().toLowerCase(Locale.ROOT);
        String digitsOnly = keyword.trim().replaceAll("[\\s.\\-()]", "");
        List<Customer> matches = new ArrayList<>();

        for (Customer customer : this.customers) {
            if (customer.isDeleted()) {
                continue;
            }
            boolean matchId = customer.getId().toLowerCase(Locale.ROOT).contains(lower);
            boolean matchName = customer.getFullName().toLowerCase(Locale.ROOT).contains(lower);
            boolean matchPhone = customer.getPhone().contains(lower)
                    || (!digitsOnly.isEmpty() && customer.getPhone().contains(digitsOnly));
            boolean matchAddress = customer.getAddress().toLowerCase(Locale.ROOT).contains(lower);
            if (matchId || matchName || matchPhone || matchAddress) {
                matches.add(customer);
            }
        }
        return Collections.unmodifiableList(matches);
    }
}
