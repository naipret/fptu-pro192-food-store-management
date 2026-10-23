package fptu.pro192.foodstoremanagement.view;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.controller.InsufficientStockException;
import fptu.pro192.foodstoremanagement.controller.OrderController;
import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import fptu.pro192.foodstoremanagement.util.TableFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Console view for Sales and Checkout Management: creating interactive customer
 * or guest shopping carts, validating real-time stock and expiration, executing
 * transactional FEFO checkout, and inspecting historical sales invoices
 * (BR8, BR9, BR10, BR11, BR12, BR13, BR14, BR15, BR16, BR17, BR24, BR26, BR28,
 * BR33, BR34).
 */
public class SalesMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "New Sale (Checkout)",
            "View All Sales Orders",
            "Search Sales Orders",
            "View Order Invoice by ID"
    };

    private static final String INVOICE_BORDER = "+-----+--------+-------------------------+-------+-------------+---------------+";
    private static final String ORDERS_BORDER = "+----------+----------+------------+-------------+-------------+---------------+";

    private final OrderController orderController;
    private final ProductController productController;
    private final CustomerController customerController;

    /**
     * Constructs a SalesMenu bound to the shared OrderController,
     * ProductController, and CustomerController instances.
     *
     * @param orderController    the shared OrderController
     * @param productController  the shared ProductController
     * @param customerController the shared CustomerController
     * @throws IllegalArgumentException if any controller is null
     */
    public SalesMenu(OrderController orderController,
            ProductController productController,
            CustomerController customerController) {
        super("FOOD STORE MANAGEMENT SYSTEM > [3] SALES", MENU_OPTIONS);
        if (orderController == null) {
            throw new IllegalArgumentException("OrderController cannot be null.");
        }
        if (productController == null) {
            throw new IllegalArgumentException("ProductController cannot be null.");
        }
        if (customerController == null) {
            throw new IllegalArgumentException("CustomerController cannot be null.");
        }
        this.orderController = orderController;
        this.productController = productController;
        this.customerController = customerController;
    }

    @Override
    protected void handleChoice(int choice) {
        switch (choice) {
            case 1:
                handleCreateOrderFlow();
                break;
            case 2:
                handleViewAllOrders();
                break;
            case 3:
                handleSearchOrders();
                break;
            case 4:
                handleViewOrderById();
                break;
            default:
                System.out.println("  [ERROR] Unsupported menu choice: " + choice);
                break;
        }
    }

    @Override
    protected boolean shouldPauseAfterAction(int choice) {
        return choice != 3 && choice != 4;
    }

    /**
     * Interactive workflow for assembling a draft shopping cart, adding items
     * with real-time stock and expiration checks, and completing or cancelling
     * the transaction (BR8, BR9, BR10, BR11, BR12, BR13, BR14, BR15, BR16, BR17,
     * BR24, BR26, BR28, BR34).
     */
    public void handleCreateOrderFlow() {
        List<Product> activeProducts = this.productController.getAllProducts();
        if (activeProducts.isEmpty()) {
            System.out.println("  [WARN] No active products in catalog. Cannot create an order.");
            return;
        }

        String resolvedCustomerId = promptCustomerOrGuest();
        if ("CANCEL".equals(resolvedCustomerId)) {
            System.out.println("  [INFO] Order creation cancelled. Returning to menu.");
            return;
        }

        Order draft = this.orderController.createDraftOrder(resolvedCustomerId);
        displayDraftHeader(draft);

        boolean shopping = true;
        while (shopping) {
            int action = promptCartAction();
            if (action == 0) {
                this.orderController.cancelDraft(draft);
                System.out.println("  [INFO] Draft order " + draft.getOrderId()
                        + " cancelled. Inventory was not modified.");
                shopping = false;
            } else if (action == 1) {
                promptAddItemToCart(draft);
            } else if (action == 2) {
                ConsoleUtil.clearScreen();
                renderInvoiceTable(draft);
            } else if (action == 3) {
                shopping = !handleCheckout(draft);
            }
        }
    }

    private void displayDraftHeader(Order draft) {
        Customer customer = (draft.getCustomerId() == null)
                ? null
                : this.customerController.findById(draft.getCustomerId());
        String customerDisplay = (customer == null)
                ? "Walk-in Guest (Regular - 0% discount)"
                : customer.getId() + " - " + customer.getFullName()
                        + " (" + customer.getCustomerType() + ")";
        System.out.println("  [INFO] Draft Order ID: " + draft.getOrderId()
                + " | Customer: " + customerDisplay);
    }

    private int promptCartAction() {
        System.out.println();
        System.out.println("  [1] Add Product to Cart");
        System.out.println("  [2] View Current Cart Summary");
        System.out.println("  [3] Confirm & Finalize Checkout");
        System.out.println("  [0] Cancel Draft Order");
        return ConsoleUtil.readInt("Select cart action [0-3]: ", 0, 3);
    }

    private boolean handleCheckout(Order draft) {
        if (draft.getItems().isEmpty()) {
            System.out.println(
                    "  [ERROR] Cart is empty. Add at least one item before checkout or press 0 to cancel.");
            return false;
        }
        ConsoleUtil.clearScreen();
        renderInvoiceTable(draft);
        if (!ConsoleUtil.confirm("Confirm payment and deduct stock via FEFO?")) {
            System.out.println("  [INFO] Checkout confirmation aborted. Returning to cart.");
            return false;
        }
        try {
            Order finalized = this.orderController.finalizeOrder(draft);
            System.out.println("  [OK] Order " + finalized.getOrderId()
                    + " finalized! Final amount: "
                    + InputValidator.formatCurrency(finalized.getFinalAmount()));
            renderInvoiceTable(finalized);
            return true;
        } catch (InsufficientStockException | IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
            return false;
        }
    }

    private String promptCustomerOrGuest() {
        while (true) {
            String raw = ConsoleUtil.readCancellableString(
                    "Enter Customer ID (e.g., C00001), 'GUEST' for walk-in, or 0 to cancel: ");
            if (raw == null) {
                return "CANCEL";
            }
            if (Order.GUEST_CUSTOMER_ID.equalsIgnoreCase(raw)) {
                return null;
            }
            Customer customer = this.customerController.findById(raw);
            if (customer != null) {
                return customer.getId();
            }
            System.out.println("  [ERROR] Active customer not found for ID '" + raw
                    + "'. Enter a valid Customer ID, 'GUEST', or 0 to cancel.");
        }
    }

    private void promptAddItemToCart(Order draft) {
        String rawInput = ConsoleUtil.readCancellableString(
                "Enter Product ID or search keyword (or 0 to cancel): ");
        if (rawInput == null) {
            return;
        }

        Product product = this.productController.findById(rawInput);
        if (product == null) {
            List<Product> matches = this.productController.search(rawInput);
            if (matches.isEmpty()) {
                System.out.println("  [ERROR] No active products match keyword: " + rawInput);
                return;
            }
            System.out.println("  Matching products found:");
            for (Product p : matches) {
                System.out.printf("    - %s | %s | %s | Available Stock: %d%n",
                        p.getId(), p.getName(),
                        InputValidator.formatCurrency(p.getPrice()),
                        p.getAvailableStock());
            }
            String confirmedId = ConsoleUtil.readCancellableString(
                    "Enter exact Product ID from list above to add (or 0 to cancel): ");
            if (confirmedId == null) {
                return;
            }
            product = this.productController.findById(confirmedId);
            if (product == null) {
                System.out.println("  [ERROR] Active product not found with ID: " + confirmedId);
                return;
            }
        }

        int remainingStock = this.orderController.getRemainingAvailableStock(draft, product.getId());
        if (remainingStock <= 0) {
            if (product.getTotalStock() > 0 && product.getAvailableStock() == 0) {
                System.out.println("  [ERROR] Cannot add '" + product.getName()
                        + "': all batches in stock have expired (BR11).");
            } else {
                System.out.println("  [ERROR] Product '" + product.getName()
                        + "' has 0 remaining sellable stock available.");
            }
            return;
        }

        System.out.println("  Selected: " + product.getId() + " - " + product.getName()
                + " | Price: " + InputValidator.formatCurrency(product.getPrice())
                + " | Available to add: " + remainingStock + " " + product.getUnit());

        while (true) {
            Integer qty = ConsoleUtil.readPositiveInt(
                    "Enter quantity to add (1-" + remainingStock + ", or 0 to cancel): ", remainingStock);
            if (qty == null) {
                return;
            }
            if (qty > remainingStock) {
                System.out.printf("  [ERROR] Quantity cannot exceed available stock (%d).%n", remainingStock);
                continue;
            }
            try {
                this.orderController.addItemToDraft(draft, product.getId(), qty);
                System.out.println("  [OK] Added " + qty + " x " + product.getName()
                        + " to cart. Current total: "
                        + InputValidator.formatCurrency(draft.getFinalAmount()));
                return;
            } catch (InsufficientStockException | IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
                return;
            }
        }
    }

    /**
     * Displays a summary table of all completed historical sales orders (BR23).
     */
    public void handleViewAllOrders() {
        List<Order> orders = this.orderController.getAllOrders();
        renderOrdersSummaryTable(orders);
    }

    /**
     * Prompts for search criteria and displays matching sales orders repeatedly
     * until 0 is entered.
     */
    public void handleSearchOrders() {
        while (true) {
            String keyword = ConsoleUtil.readOptionalString("Enter search keyword (or 0 to cancel): ");
            if ("0".equals(keyword)) {
                break;
            }
            List<Order> matching = this.orderController.searchOrders(keyword);
            renderOrdersSummaryTable(matching);
        }
    }

    /**
     * Prompts for an Order ID and renders its detailed invoice table repeatedly
     * until 0 is entered (BR33).
     */
    public void handleViewOrderById() {
        while (true) {
            String rawOrderId = ConsoleUtil.readCancellableString(
                    "Enter Order ID (e.g., O00001, or 0 to cancel): ");
            if (rawOrderId == null) {
                break;
            }
            Order order = this.orderController.findById(rawOrderId);
            if (order == null) {
                System.out.println("  [ERROR] Order not found with ID: " + rawOrderId);
                continue;
            }
            renderInvoiceTable(order);
        }
    }

    /**
     * Renders the 78-column Sales Cart and Invoice Table for an Order.
     *
     * @param order the draft or finalized Order to display
     */
    public void renderInvoiceTable(Order order) {
        if (order == null) {
            System.out.println("  [INFO] No order selected.");
            return;
        }
        Customer customer = (order.getCustomerId() == null)
                ? null
                : this.customerController.findByIdIncludeDeleted(order.getCustomerId());
        String customerText = (customer == null)
                ? Order.GUEST_CUSTOMER_ID
                : customer.getId() + " - " + customer.getFullName()
                        + " (" + customer.getCustomerType() + ")";

        System.out.println(INVOICE_BORDER);
        System.out.printf("  Order ID   : %-22s Date : %s%n",
                order.getOrderId(),
                InputValidator.formatDisplayDate(order.getOrderDate().toLocalDate()));
        System.out.printf("  Customer   : %s%n", TableFormatter.fit(customerText, 60));
        System.out.println(INVOICE_BORDER);
        System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                TableFormatter.fit("No.", 3),
                TableFormatter.fit("ProdID", 6),
                TableFormatter.fit("Product Name", 23),
                TableFormatter.fitRight("Qty", 5),
                TableFormatter.fitRight("Unit Price", 11),
                TableFormatter.fitRight("Subtotal", 13));
        System.out.println(INVOICE_BORDER);

        List<OrderDetail> items = order.getItems();
        if (items.isEmpty()) {
            System.out.printf("| %s |%n", TableFormatter.center("(Cart is empty)", 76));
        } else {
            for (int i = 0; i < items.size(); i++) {
                OrderDetail item = items.get(i);
                Product p = this.productController.findByIdIncludeDeleted(item.getProductId());
                String productName = (p != null) ? p.getName() : "Unknown (" + item.getProductId() + ")";
                String unitPriceStr = String.format(Locale.US, "%,.0f", item.getUnitPrice());
                String subTotalStr = String.format(Locale.US, "%,.0f", item.getSubTotal());
                System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                        TableFormatter.fitRight(String.valueOf(i + 1), 3),
                        TableFormatter.fit(item.getProductId(), 6),
                        TableFormatter.fit(productName, 23),
                        TableFormatter.fitRight(String.valueOf(item.getQuantity()), 5),
                        TableFormatter.fitRight(unitPriceStr, 11),
                        TableFormatter.fitRight(subTotalStr, 13));
            }
        }
        System.out.println(INVOICE_BORDER);
        System.out.printf("| Gross Total  | %61s |%n",
                InputValidator.formatCurrency(order.getTotalAmount()));
        System.out.printf("| Discount     | %61s |%n",
                InputValidator.formatCurrency(order.getDiscount()));
        System.out.printf("| Final Total  | %61s |%n",
                InputValidator.formatCurrency(order.getFinalAmount()));
        System.out.println(INVOICE_BORDER);
    }

    /**
     * Renders a summary table of completed orders within 80 columns.
     *
     * @param orders the list of orders to render
     */
    public static void renderOrdersSummaryTable(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            System.out.println("  [INFO] No sales orders found.");
            return;
        }
        System.out.println(ORDERS_BORDER);
        System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                TableFormatter.fit("OrdID", 8),
                TableFormatter.fit("CustID", 8),
                TableFormatter.fit("Date", 10),
                TableFormatter.fitRight("Gross", 11),
                TableFormatter.fitRight("Discount", 11),
                TableFormatter.fitRight("Final", 13));
        System.out.println(ORDERS_BORDER);
        for (Order order : orders) {
            String custId = (order.getCustomerId() == null)
                    ? Order.GUEST_CUSTOMER_ID
                    : order.getCustomerId();
            String dateStr = InputValidator.formatDisplayDate(order.getOrderDate().toLocalDate());
            String grossStr = String.format(Locale.US, "%,.0f", order.getTotalAmount());
            String discStr = String.format(Locale.US, "%,.0f", order.getDiscount());
            String finalStr = String.format(Locale.US, "%,.0f", order.getFinalAmount());
            System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                    TableFormatter.fit(order.getOrderId(), 8),
                    TableFormatter.fit(custId, 8),
                    TableFormatter.fit(dateStr, 10),
                    TableFormatter.fitRight(grossStr, 11),
                    TableFormatter.fitRight(discStr, 11),
                    TableFormatter.fitRight(finalStr, 13));
        }
        System.out.println(ORDERS_BORDER);
        System.out.println("  Total orders displayed: " + orders.size());
    }
}
