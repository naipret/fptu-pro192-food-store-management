package fptu.pro192.foodstoremanagement.repository;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.CustomerFactory;
import fptu.pro192.foodstoremanagement.model.VIPCustomer;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import fptu.pro192.foodstoremanagement.util.MiniCsv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Repository managing in-memory customer caching and atomic CSV persistence.
 *
 * BR2: Unique Customer ID format C00001–C99999. BR25: Phone number normalization before
 * lookup/persistence. BR27: Soft delete (isDeleted = true), preserving historical records.
 */
public class CustomerRepository {

    private final String filePath;
    private final List<Customer> customers = new ArrayList<>();
    private static final List<String> CSV_HEADERS =
            Arrays.asList("type", "id", "fullName", "phone", "address", "isDeleted");

    public CustomerRepository() {
        this("data/customers.csv");
    }

    public CustomerRepository(String filePath) {
        this.filePath = filePath;
        loadFromCsv();
    }

    /**
     * Reads CSV records via MiniCsv and populates in-memory cache.
     */
    public synchronized void loadFromCsv() {
        customers.clear();
        List<String[]> rows = MiniCsv.read(filePath);
        if (rows == null || rows.isEmpty()) {
            return;
        }

        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            // Skip CSV header row if present
            if (i == 0 && row.length > 0 && "type".equalsIgnoreCase(row[0].trim())) {
                continue;
            }
            if (row.length >= 6) {
                String type = row[0].trim();
                String id = row[1].trim();
                String fullName = row[2].trim();
                String phone = row[3].trim();
                String address = row[4].trim();
                boolean isDeleted = Boolean.parseBoolean(row[5].trim());

                customers
                        .add(CustomerFactory.create(type, id, fullName, phone, address, isDeleted));
            }
        }
    }

    /**
     * Atomically serializes customers to CSV via MiniCsv.
     */
    public synchronized void saveToCsv() {
        List<String[]> rows = new ArrayList<>();
        for (Customer c : customers) {
            String type = (c instanceof VIPCustomer) ? "VIP" : "REGULAR";
            rows.add(new String[] {type, c.getId() != null ? c.getId() : "",
                    c.getFullName() != null ? c.getFullName() : "",
                    c.getPhone() != null ? c.getPhone() : "",
                    c.getAddress() != null ? c.getAddress() : "", String.valueOf(c.isDeleted())});
        }
        MiniCsv.writeAtomic(filePath, CSV_HEADERS, rows);
    }

    /**
     * Finds active customer by ID.
     *
     * @param id The customer ID
     * @return Active customer matching ID, or null if not found
     */
    public synchronized Customer findById(String id) {
        if (id == null) {
            return null;
        }
        for (Customer customer : customers) {
            if (!customer.isDeleted() && id.trim().equalsIgnoreCase(customer.getId())) {
                return customer;
            }
        }
        return null;
    }

    /**
     * Finds active customer by phone number.
     *
     * @param phone Raw or normalized phone number
     * @return Active customer matching normalized phone, or null if not found
     */
    public synchronized Customer findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        String normalizedInput;
        try {
            normalizedInput = InputValidator.normalizePhone(phone);
        } catch (IllegalArgumentException e) {
            normalizedInput = phone.replaceAll("[^0-9]", "");
        }

        for (Customer customer : customers) {
            if (!customer.isDeleted() && customer.getPhone() != null) {
                if (customer.getPhone().equals(normalizedInput)) {
                    return customer;
                }
            }
        }
        return null;
    }

    /**
     * Returns an unmodifiable list of all active (non-deleted) customers.
     *
     * @return List of active customers
     */
    public synchronized List<Customer> findAll() {
        List<Customer> activeCustomers = new ArrayList<>();
        for (Customer customer : customers) {
            if (!customer.isDeleted()) {
                activeCustomers.add(customer);
            }
        }
        return Collections.unmodifiableList(activeCustomers);
    }

    /**
     * Appends or updates customer and persists to CSV.
     *
     * @param customer The customer to save
     */
    public synchronized void save(Customer customer) {
        if (customer == null || customer.getId() == null) {
            throw new IllegalArgumentException("Customer and Customer ID cannot be null");
        }
        boolean exists = false;
        for (int i = 0; i < customers.size(); i++) {
            if (customer.getId().trim().equalsIgnoreCase(customers.get(i).getId())) {
                customers.set(i, customer);
                exists = true;
                break;
            }
        }
        if (!exists) {
            customers.add(customer);
        }
        saveToCsv();
    }

    /**
     * Soft deletes customer by ID (BR27) and persists to CSV.
     *
     * @param id The customer ID to delete
     */
    public synchronized void delete(String id) {
        if (id == null) {
            return;
        }
        for (Customer customer : customers) {
            if (id.trim().equalsIgnoreCase(customer.getId())) {
                customer.setDeleted(true);
                saveToCsv();
                break;
            }
        }
    }
}
