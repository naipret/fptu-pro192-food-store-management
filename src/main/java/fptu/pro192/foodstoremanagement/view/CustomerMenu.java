package fptu.pro192.foodstoremanagement.view;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import fptu.pro192.foodstoremanagement.util.TableFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Interactive 80-column console menu for managing store customers and loyalty
 * tiers: registering Regular and VIP customers, updating customer details,
 * soft-deleting customer records, viewing the customer directory, and
 * searching by name or phone number (BR2, BR3, BR15, BR25, BR27).
 */
public class CustomerMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "View All Customers",
            "Search Customers",
            "Register New Customer",
            "Update Customer Information",
            "Remove Customer"
    };

    private static final String CUSTOMER_TABLE_BORDER = "+--------+---------------------+------------+------------------------+---------+";
    private static final String CUSTOMER_TABLE_HEADER = "| ID     | Full Name           | Phone      | Address                | Tier    |";

    private final CustomerController customerController;

    /**
     * Constructs a CustomerMenu bound to the shared CustomerController.
     *
     * @param customerController the non-null CustomerController collaborator
     * @throws IllegalArgumentException if customerController is null
     */
    public CustomerMenu(CustomerController customerController) {
        super("FOOD STORE MANAGEMENT SYSTEM > [2] CUSTOMERS", MENU_OPTIONS);
        if (customerController == null) {
            throw new IllegalArgumentException("CustomerController cannot be null.");
        }
        this.customerController = customerController;
    }

    @Override
    protected void handleChoice(int choice) {
        switch (choice) {
            case 1:
                handleViewAllCustomers();
                break;
            case 2:
                handleSearchCustomers();
                break;
            case 3:
                handleRegisterCustomer();
                break;
            case 4:
                handleUpdateCustomer();
                break;
            case 5:
                handleDeleteCustomer();
                break;
            default:
                System.out.println("  [ERROR] Unsupported menu option.");
                break;
        }
    }

    private void handleViewAllCustomers() {
        List<Customer> customers = this.customerController.getAllActive();
        renderCustomerTable(customers);
    }

    private void handleSearchCustomers() {
        while (true) {
            String keyword = ConsoleUtil
                    .readOptionalString("Enter search keyword (0 to cancel): ");
            if ("0".equals(keyword.trim())) {
                break;
            }
            List<Customer> matches = this.customerController.search(keyword);
            renderCustomerTable(matches);
        }
    }

    private void handleRegisterCustomer() {
        String suggestedId = this.customerController.generateNextCustomerId();
        String customerId = promptCustomerId(suggestedId);
        if (customerId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        String fullName = promptValidatedText("Enter customer full name: ", "Customer full name");
        String phone = promptUniquePhone(null);
        String address = promptValidatedText("Enter customer address: ", "Customer address");
        String tier = promptTierSelection();

        try {
            Customer created = this.customerController.registerCustomer(
                    customerId, fullName, phone, address, tier);
            System.out.printf("  [OK] Registered %s customer %s ('%s', Phone: %s).%n",
                    created.getCustomerType(), created.getId(),
                    created.getFullName(), created.getPhone());
            renderCustomerTable(Collections.singletonList(created));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleUpdateCustomer() {
        String rawId = ConsoleUtil.readNonEmptyString("Enter Customer ID to update (e.g., C00001, or 0 to cancel): ");
        if ("0".equals(rawId)) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Customer existing = this.customerController.findById(rawId);
        if (existing == null) {
            System.out.println("  [ERROR] Active customer not found with ID: " + rawId);
            return;
        }

        System.out.printf("  Current Customer: %s - %s (%s)%n",
                existing.getId(), existing.getFullName(), existing.getCustomerType());
        System.out.printf("  Current Phone   : %s | Address: %s%n",
                existing.getPhone(), existing.getAddress());
        System.out.println("  (Leave input blank and press Enter to keep current value)");

        String newName = promptOptionalText(
                "Enter new full name [" + existing.getFullName() + "]: ", "Customer full name");
        String newPhone = promptOptionalPhone(
                "Enter new phone [" + existing.getPhone() + "]: ", existing.getId());
        String newAddress = promptOptionalText(
                "Enter new address [" + existing.getAddress() + "]: ", "Customer address");
        String newTier = null;
        if (ConsoleUtil.confirm("Do you want to change loyalty tier (" + existing.getCustomerType() + ")?")) {
            newTier = promptTierSelection();
        }

        try {
            Customer updated = this.customerController.updateCustomer(
                    existing.getId(), newName, newPhone, newAddress, newTier);
            System.out.printf("  [OK] Customer %s updated (%s, Tier: %s).%n",
                    updated.getId(), updated.getFullName(), updated.getCustomerType());
            renderCustomerTable(Collections.singletonList(updated));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleDeleteCustomer() {
        String rawId = ConsoleUtil.readNonEmptyString("Enter Customer ID to remove (e.g., C00001, or 0 to cancel): ");
        if ("0".equals(rawId)) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Customer existing = this.customerController.findById(rawId);
        if (existing == null) {
            System.out.println("  [ERROR] Active customer not found with ID: " + rawId);
            return;
        }

        System.out.printf("  Selected Customer: %s - %s (%s, Phone: %s)%n",
                existing.getId(), existing.getFullName(),
                existing.getCustomerType(), existing.getPhone());
        if (!ConsoleUtil.confirm("Confirm removing customer " + existing.getId() + "?")) {
            System.out.println("  [INFO] Deletion cancelled.");
            return;
        }

        try {
            this.customerController.deleteCustomer(existing.getId());
            System.out.printf("  [OK] Customer %s has been removed successfully.%n", existing.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    /**
     * Renders a formatted 80-column ASCII table of customer records.
     *
     * @param customers the list of Customer objects to display
     */
    public void renderCustomerTable(List<Customer> customers) {
        if (customers == null || customers.isEmpty()) {
            System.out.println("  [INFO] No matching customers found.");
            return;
        }
        System.out.println(CUSTOMER_TABLE_BORDER);
        System.out.println(CUSTOMER_TABLE_HEADER);
        System.out.println(CUSTOMER_TABLE_BORDER);
        for (Customer c : customers) {
            System.out.printf("| %s | %s | %s | %s | %s |%n",
                    TableFormatter.fit(c.getId(), 6),
                    TableFormatter.fit(c.getFullName(), 19),
                    TableFormatter.fit(c.getPhone(), 10),
                    TableFormatter.fit(c.getAddress(), 22),
                    TableFormatter.fit(c.getCustomerType(), 7));
        }
        System.out.println(CUSTOMER_TABLE_BORDER);
        System.out.printf("  Total customers listed: %d%n", customers.size());
    }

    private String promptCustomerId(String suggestedId) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(
                    "Enter Customer ID [Press Enter for " + suggestedId + ", or 0 to cancel]: ");
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return suggestedId;
            }
            try {
                String normalized = InputValidator.normalizeCustomerId(raw);
                if (this.customerController.existsById(normalized)) {
                    System.out.println("  [ERROR] Customer ID '" + normalized
                            + "' already exists (including soft-deleted records).");
                    continue;
                }
                return normalized;
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private String promptValidatedText(String prompt, String fieldName) {
        while (true) {
            String raw = ConsoleUtil.readNonEmptyString(prompt);
            try {
                return InputValidator.normalizeText(raw, fieldName);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private String promptOptionalText(String prompt, String fieldName) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(prompt);
            if (raw.isEmpty()) {
                return null;
            }
            try {
                return InputValidator.normalizeText(raw, fieldName);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private String promptUniquePhone(String currentCustomerId) {
        while (true) {
            String raw = ConsoleUtil.readNonEmptyString(
                    "Enter Vietnamese mobile phone: ");
            try {
                String normalized = InputValidator.normalizePhone(raw);
                Customer owner = this.customerController.findByPhone(normalized);
                if (owner != null
                        && (currentCustomerId == null || !owner.getId().equalsIgnoreCase(currentCustomerId))) {
                    System.out.println("  [ERROR] Phone number '" + normalized
                            + "' is already registered to customer " + owner.getId() + ".");
                    continue;
                }
                return normalized;
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private String promptOptionalPhone(String prompt, String currentCustomerId) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(prompt);
            if (raw.isEmpty()) {
                return null;
            }
            try {
                String normalized = InputValidator.normalizePhone(raw);
                Customer owner = this.customerController.findByPhone(normalized);
                if (owner != null && !owner.getId().equalsIgnoreCase(currentCustomerId)) {
                    System.out.println("  [ERROR] Phone number '" + normalized
                            + "' is already registered to customer " + owner.getId() + ".");
                    continue;
                }
                return normalized;
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private String promptTierSelection() {
        System.out.println("  Select Customer Loyalty Tier:");
        System.out.println("    [1] Regular (0% Discount)");
        System.out.println("    [2] VIP     (10% Discount)");
        int choice = ConsoleUtil.readInt("Enter tier choice [1-2]: ", 1, 2);
        return (choice == 2) ? "VIP" : "Regular";
    }
}
