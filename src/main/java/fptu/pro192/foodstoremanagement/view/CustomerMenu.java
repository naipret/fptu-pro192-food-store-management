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
 * searching by name, phone, or ID (BR2, BR15, BR16, BR25, BR27, BR33).
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

    @Override
    protected boolean shouldPauseAfterAction(int choice) {
        return choice != 2;
    }

    private void handleViewAllCustomers() {
        List<Customer> customers = this.customerController.getAllActive();
        renderCustomerTable(customers);
    }

    private void handleSearchCustomers() {
        while (true) {
            String keyword = ConsoleUtil.readOptionalString("Enter search keyword (or 0 to cancel): ");
            if ("0".equals(keyword)) {
                break;
            }
            List<Customer> matches = this.customerController.search(keyword);
            renderCustomerTable(matches);
        }
    }

    private void handleRegisterCustomer() {
        String fullName = ConsoleUtil.readValidatedText("Enter customer full name (or 0 to cancel): ",
                "Customer full name");
        if (fullName == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        String phone = promptUniquePhone(null);
        if (phone == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        String address = ConsoleUtil.readValidatedText("Enter customer address (or 0 to cancel): ", "Customer address");
        if (address == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        String tier = promptTierSelection(null);
        if (tier == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        if (!ConsoleUtil.confirm("Are you sure you want to register this customer?")) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        try {
            Customer created = this.customerController.registerCustomer(fullName, phone, address, tier);
            System.out.printf("  [OK] Registered %s customer %s ('%s', Phone: %s).%n",
                    created.getCustomerType(), created.getId(),
                    created.getFullName(), created.getPhone());
            renderCustomerTable(Collections.singletonList(created));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleUpdateCustomer() {
        String rawId = ConsoleUtil.readCancellableString("Enter Customer ID to update (or 0 to cancel): ");
        if (rawId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Customer existing = this.customerController.findById(rawId);
        if (existing == null) {
            System.out.println("  [ERROR] Active customer not found with ID: " + rawId);
            return;
        }

        System.out.println("  Current Customer Details:");
        renderCustomerTable(Collections.singletonList(existing));
        System.out.println("  (Press Enter to keep current value, or 0 to cancel)");

        String newName = ConsoleUtil.readOptionalValidatedText(
                "Enter new full name [" + existing.getFullName() + "]: ",
                "Customer full name", existing.getFullName());
        if (newName == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        String newPhone = promptOptionalPhone(
                "Enter new phone [" + existing.getPhone() + "]: ",
                existing.getId(), existing.getPhone());
        if (newPhone == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        String newAddress = ConsoleUtil.readOptionalValidatedText(
                "Enter new address [" + existing.getAddress() + "]: ",
                "Customer address", existing.getAddress());
        if (newAddress == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        System.out.println("  Current Loyalty Tier: " + existing.getCustomerType());
        String newTier = promptTierSelection(existing.getCustomerType());
        if (newTier == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        if (!ConsoleUtil.confirm("Are you sure you want to update this customer?")) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
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
        String rawId = ConsoleUtil.readCancellableString("Enter Customer ID to remove (or 0 to cancel): ");
        if (rawId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Customer existing = this.customerController.findById(rawId);
        if (existing == null) {
            System.out.println("  [ERROR] Active customer not found with ID: " + rawId);
            return;
        }

        System.out.println("  Customer to delete:");
        renderCustomerTable(Collections.singletonList(existing));

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

    private String promptUniquePhone(String currentCustomerId) {
        while (true) {
            String raw = ConsoleUtil.readCancellableString(
                    "Enter Vietnamese mobile phone (or 0 to cancel): ");
            if (raw == null) {
                return null;
            }
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

    private String promptOptionalPhone(String prompt, String currentCustomerId, String currentPhone) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(prompt);
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return currentPhone;
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

    private String promptTierSelection(String currentTier) {
        System.out.println("  Select Customer Loyalty Tier:");
        System.out.println("    [1] Regular (0% Discount)");
        System.out.println("    [2] VIP     (10% Discount)");
        Integer choice;
        if (currentTier == null) {
            choice = ConsoleUtil.readInt("Enter tier choice [1-2] (or 0 to cancel): ", 1, 2);
        } else {
            choice = ConsoleUtil.readIntWithDefault(
                    "Enter new tier choice [1-2]: ", 1, 2, -1);
        }
        if (choice == null) {
            return null;
        }
        if (choice == -1) {
            return currentTier;
        }
        return (choice == 2) ? "VIP" : "Regular";
    }
}
