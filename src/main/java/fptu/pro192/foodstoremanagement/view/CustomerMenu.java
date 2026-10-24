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
 * tiers: registering Regular and VIP customers, viewing customers with
 * real-time keyword search, updating customer profiles, and soft-deleting
 * customer records (BR2, BR15, BR16, BR25, BR27, BR33).
 */
public class CustomerMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "Register New Customer",
            "View All Customers",
            "Update Customer",
            "Remove Customer"
    };

    private static final String DIVIDER = TableFormatter.repeat('-', TableFormatter.SCREEN_WIDTH);
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
                handleRegisterCustomer();
                break;
            case 2:
                handleViewAllCustomers();
                break;
            case 3:
                handleUpdateCustomer();
                break;
            case 4:
                handleDeleteCustomer();
                break;
            default:
                System.out.println("  [ERROR] Unsupported menu option.");
                break;
        }
    }

    @Override
    protected boolean shouldPauseAfterAction(int choice) {
        return false;
    }

    private void handleRegisterCustomer() {
        while (true) {
            String fullName = ConsoleUtil.readValidatedCustomerName("Enter customer full name (or 0 to cancel): ");
            if (fullName == null) {
                break;
            }
            executeRegisterCustomer(fullName);
            System.out.println(DIVIDER);
        }
    }

    private void executeRegisterCustomer(String fullName) {
        String phone = promptUniquePhone(null);
        if (phone == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }
        String address = ConsoleUtil.readValidatedAddress("Enter customer address (or 0 to cancel): ");
        if (address == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }
        String tier = promptTierSelection(null);
        if (tier == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }
        System.out.println();
        confirmAndPersistCustomer(fullName, phone, address, tier);
    }

    private void confirmAndPersistCustomer(String fullName, String phone, String address, String tier) {
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

    private void handleViewAllCustomers() {
        renderCustomerTable(this.customerController.getAllActive());
        while (true) {
            System.out.println(DIVIDER);
            String keyword = ConsoleUtil.readOptionalString("Enter search keyword (or 0 to cancel): ");
            if ("0".equals(keyword)) {
                break;
            }
            List<Customer> matches = keyword.isEmpty()
                    ? this.customerController.getAllActive()
                    : this.customerController.search(keyword);
            renderCustomerTable(matches);
        }
    }

    private void handleUpdateCustomer() {
        while (true) {
            String rawId = ConsoleUtil.readCancellableString(
                    "Enter Customer ID to update (or 0 to cancel): ");
            if (rawId == null) {
                break;
            }
            Customer existing = this.customerController.findById(rawId);
            if (existing == null) {
                System.out.println("  [ERROR] Active customer not found with ID: " + rawId);
                System.out.println(DIVIDER);
                continue;
            }
            performCustomerUpdate(existing);
            System.out.println(DIVIDER);
        }
    }

    private void performCustomerUpdate(Customer existing) {
        renderCustomerTable(Collections.singletonList(existing));
        System.out.println();
        System.out.println("  (Press Enter to keep current value, or 0 to cancel)");

        String newName = ConsoleUtil.readOptionalValidatedCustomerName(
                "Enter new full name [" + existing.getFullName() + "]: ", existing.getFullName());
        if (newName == null) {
            System.out.println("  [INFO] Update cancelled.");
            return;
        }
        String newPhone = promptOptionalPhone(
                "Enter new phone [" + existing.getPhone() + "]: ",
                existing.getId(), existing.getPhone());
        if (newPhone == null) {
            System.out.println("  [INFO] Update cancelled.");
            return;
        }
        String newAddress = ConsoleUtil.readOptionalValidatedAddress(
                "Enter new address [" + existing.getAddress() + "]: ", existing.getAddress());
        if (newAddress == null) {
            System.out.println("  [INFO] Update cancelled.");
            return;
        }
        String newTier = promptTierSelection(existing.getCustomerType());
        if (newTier == null) {
            System.out.println("  [INFO] Update cancelled.");
            return;
        }
        System.out.println();
        applyCustomerUpdate(existing, newName, newPhone, newAddress, newTier);
    }

    private void applyCustomerUpdate(Customer existing, String name, String phone, String address, String tier) {
        if (!ConsoleUtil.confirm("Are you sure you want to update this customer?")) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }
        try {
            Customer updated = this.customerController.updateCustomer(
                    existing.getId(), name, phone, address, tier);
            System.out.printf("  [OK] Customer %s updated (%s, Tier: %s).%n",
                    updated.getId(), updated.getFullName(), updated.getCustomerType());
            renderCustomerTable(Collections.singletonList(updated));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleDeleteCustomer() {
        while (true) {
            String rawId = ConsoleUtil.readCancellableString(
                    "Enter Customer ID to remove (or 0 to cancel): ");
            if (rawId == null) {
                break;
            }
            Customer existing = this.customerController.findById(rawId);
            if (existing == null) {
                System.out.println("  [ERROR] Active customer not found with ID: " + rawId);
                System.out.println(DIVIDER);
                continue;
            }
            renderCustomerTable(Collections.singletonList(existing));
            System.out.println();
            if (!ConsoleUtil.confirm("Confirm removing customer " + existing.getId() + "?")) {
                System.out.println("  [INFO] Removal cancelled.");
                System.out.println(DIVIDER);
                continue;
            }
            try {
                this.customerController.deleteCustomer(existing.getId());
                System.out.printf("  [OK] Customer %s has been removed successfully.%n", existing.getId());
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
            System.out.println(DIVIDER);
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
        Integer choice = (currentTier == null)
                ? ConsoleUtil.readInt("Enter tier choice [1-2] (or 0 to cancel): ", 1, 2)
                : ConsoleUtil.readIntWithDefault("Enter new tier choice [1-2]: ", 1, 2, -1);
        if (choice == null) {
            return null;
        }
        if (choice == -1) {
            return currentTier;
        }
        return (choice == 2) ? "VIP" : "Regular";
    }
}
