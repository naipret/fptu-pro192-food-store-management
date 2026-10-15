package fptu.pro192.foodstoremanagement.service;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.CustomerFactory;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.util.InputValidator;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service class handling customer management business rules.
 *
 * BR2: Unique Customer ID format C00001–C99999.
 * BR18: User input validation prior to persistence.
 * BR25: Vietnamese phone normalization before persistence/lookup.
 * BR27: Soft delete (isDeleted = true), preserving historical records.
 */
public class CustomerService {

    private final CustomerRepository customerRepo;

    public CustomerService(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    /**
     * Adds a new customer with validation and phone normalization.
     *
     * BR2: ID format C00001–C99999, unique and immutable.
     * BR18: Input validation.
     * BR25: Normalize phone number and verify uniqueness among active customers.
     *
     * @param id The customer ID (Cxxxxx)
     * @param fullName The full name
     * @param rawPhone The raw input phone string
     * @param address The address
     * @param type Customer tier type ("VIP" or "Regular")
     * @throws IllegalArgumentException If any input is invalid or duplicated
     */
    public void addCustomer(String id, String fullName, String rawPhone, String address, String type) {
        if (id == null || !id.matches("^C\\d{5}$")) {
            throw new IllegalArgumentException("ID không hợp lệ. Định dạng yêu cầu: Cxxxxx (x là số).");
        }

        if (customerRepo.findById(id) != null) {
            throw new IllegalArgumentException("ID khách hàng đã tồn tại.");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }

        String normalizedPhone = InputValidator.normalizePhone(rawPhone);

        boolean phoneExists = getAllActive().stream()
                .filter(Objects::nonNull)
                .anyMatch(c -> Objects.equals(c.getPhone(), normalizedPhone));
        if (phoneExists) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi một khách hàng đang hoạt động.");
        }

        Customer newCustomer = CustomerFactory.create(type, id, fullName.trim(), normalizedPhone, address != null ? address.trim() : "");
        customerRepo.save(newCustomer);
    }

    /**
     * Selectively updates customer phone and address.
     * Task B6: Selective update - blank inputs retain existing values.
     *
     * @param id The customer ID to update
     * @param newPhone New phone number (blank to skip)
     * @param newAddress New address (blank to skip)
     * @throws IllegalArgumentException If customer not found or new phone is duplicated
     */
    public void updateCustomer(String id, String newPhone, String newAddress) {
        Customer customer = customerRepo.findById(id);
        if (customer == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id);
        }

        if (newPhone != null && !newPhone.trim().isEmpty()) {
            String normalizedPhone = InputValidator.normalizePhone(newPhone);
            boolean phoneExists = getAllActive().stream()
                    .filter(c -> c != null && !c.getId().equalsIgnoreCase(id))
                    .anyMatch(c -> Objects.equals(c.getPhone(), normalizedPhone));
            if (phoneExists) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi một khách hàng đang hoạt động.");
            }
            customer.setPhone(normalizedPhone);
        }

        if (newAddress != null && !newAddress.trim().isEmpty()) {
            customer.setAddress(newAddress.trim());
        }

        customerRepo.saveToCsv();
    }

    /**
     * Soft deletes customer by ID (BR27).
     *
     * @param id The customer ID to delete
     * @throws IllegalArgumentException If customer not found
     */
    public void deleteCustomer(String id) {
        Customer customer = customerRepo.findById(id);
        if (customer == null) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id);
        }
        customerRepo.delete(id);
    }

    /**
     * Returns all active (non-deleted) customers.
     *
     * @return List of active customers
     */
    public List<Customer> getAllActive() {
        return customerRepo.findAll();
    }

    /**
     * Searches active customers by name substring (case-insensitive) or phone number.
     *
     * @param keyword The search keyword
     * @return List of matching active customers
     */
    public List<Customer> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActive();
        }

        String lowerKeyword = keyword.trim().toLowerCase();
        return getAllActive().stream()
                .filter(c -> (c.getFullName() != null && c.getFullName().toLowerCase().contains(lowerKeyword))
                        || (c.getPhone() != null && c.getPhone().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }

    /**
     * Finds active customer by ID.
     *
     * @param id The customer ID
     * @return Customer if found and active, null otherwise
     */
    public Customer findById(String id) {
        return customerRepo.findById(id);
    }

    /**
     * Returns underlying CustomerRepository instance.
     *
     * @return CustomerRepository
     */
    public CustomerRepository getCustomerRepo() {
        return customerRepo;
    }
}
