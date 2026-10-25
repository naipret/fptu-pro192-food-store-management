package fptu.pro192.foodstoremanagement.view;

import fptu.pro192.foodstoremanagement.controller.CustomerController;
import fptu.pro192.foodstoremanagement.controller.OrderController;
import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import fptu.pro192.foodstoremanagement.util.TableFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Console view for Sales and Checkout Management: interactive shopping cart
 * dashboard, real-time FEFO stock validation, transactional checkout, and
 * sales history search with full invoice expansion (BR8, BR9, BR10, BR11,
 * BR12, BR13, BR14, BR15, BR16, BR17, BR24, BR26, BR28, BR33, BR34).
 */
public class SalesMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "New Sale",
            "View All Sales Orders"
    };

    private static final String INVOICE_BORDER = "+-----+--------+-------------------------+-------+-------------+---------------+";
    private static final String ORDERS_BORDER = "+----------+----------+------------+-------------+-------------+---------------+";

    private final OrderController orderController;
    private final ProductController productController;
    private final CustomerController customerController;

    /**
     * Constructs a SalesMenu bound to the shared controller collaborators.
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
            default:
                System.out.println("  [ERROR] Unsupported menu choice: " + choice);
                break;
        }
    }

    /**
     * Interactive workflow for assembling a draft shopping cart, adding items
     * with real-time stock and expiration checks, and completing or cancelling
     * the transaction in a continuous loop.
     */
    public void handleCreateOrderFlow() {
        while (true) {
            List<Product> activeProducts = this.productController.getAllProducts();
            if (activeProducts.isEmpty()) {
                System.out.println("  [WARN] No active products in catalog. Cannot create an order.");
                break;
            }
            String resolvedCustomerId = promptCustomerOrGuest();
            if ("CANCEL".equals(resolvedCustomerId)) {
                break;
            }
            Order draft = this.orderController.createDraftOrder(resolvedCustomerId);
            executeLiveCartDashboard(draft);
            printDivider();
        }
    }

    private void executeLiveCartDashboard(Order draft) {
        boolean shopping = true;
        while (shopping) {
            ConsoleUtil.clearScreen();
            System.out.println(TableFormatter.repeat('=', TableFormatter.SCREEN_WIDTH));
            System.out.println(TableFormatter.center(
                    "FOOD STORE MANAGEMENT SYSTEM > [3] SALES > [1] New Sale",
                    TableFormatter.SCREEN_WIDTH));
            System.out.println(TableFormatter.repeat('=', TableFormatter.SCREEN_WIDTH));
            renderInvoiceTable(draft);

            System.out.println("\n  [1] Add Product to Cart");
            System.out.println("  [2] Confirm & Finalize Checkout");

            int action = ConsoleUtil.readInt("\nSelect cart action [1-2] (or 0 to cancel): ", 0, 2);
            if (action == 0) {
                if (draft.getItems().isEmpty() || ConsoleUtil.confirm("Discard draft order and return?")) {
                    this.orderController.cancelDraft(draft);
                    System.out.println("  [INFO] Draft order cancelled.");
                    shopping = false;
                }
            } else if (action == 1) {
                promptAddItemToCart(draft);
            } else if (action == 2) {
                shopping = !handleCheckout(draft);
            }
        }
    }

    private boolean handleCheckout(Order draft) {
        if (draft.getItems().isEmpty()) {
            System.out.println("  [ERROR] Cart is empty. Add at least one item before checkout or select 0 to cancel.");
            return false;
        }
        if (!ConsoleUtil.confirm("Confirm payment and deduct stock via FEFO?")) {
            System.out.println("  [INFO] Checkout cancelled. Returning to cart.");
            return false;
        }
        try {
            Order finalized = this.orderController.finalizeOrder(draft);
            System.out.printf("  [OK] Order %s finalized! Total paid: %s%n",
                    finalized.getOrderId(),
                    InputValidator.formatCurrency(finalized.getFinalAmount()));
            renderInvoiceTable(finalized);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
            return false;
        }
    }

    private String promptCustomerOrGuest() {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(
                    "Enter Customer ID (e.g., C00001, Enter for GUEST, 0 to cancel): ");
            if ("0".equals(raw)) {
                return "CANCEL";
            }
            if (raw.isEmpty() || Order.GUEST_CUSTOMER_ID.equalsIgnoreCase(raw)) {
                return null;
            }
            Customer customer = this.customerController.findById(raw);
            if (customer != null) {
                return customer.getId();
            }
            System.out.println("  [ERROR] Active customer not found with ID: " + raw);
        }
    }

    private void promptAddItemToCart(Order draft) {
        while (true) {
            Product selected = selectProductForCart();
            if (selected == null) {
                return;
            }
            int remainingStock = this.orderController.getRemainingAvailableStock(draft, selected.getId());
            if (remainingStock <= 0) {
                displayStockDepletedError(selected);
                continue;
            }
            ProductMenu.renderProductTable(Collections.singletonList(selected));
            System.out.println();
            Integer qty = ConsoleUtil.readPositiveInt(
                    "Enter quantity to add (1-" + remainingStock + ", or 0 to cancel): ", remainingStock);
            if (qty == null) {
                System.out.println("  [INFO] Operation cancelled.");
                continue;
            }
            try {
                this.orderController.addItemToDraft(draft, selected.getId(), qty);
                System.out.printf("  [OK] Added %d x %s to cart.%n", qty, selected.getName());
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private Product selectProductForCart() {
        while (true) {
            printDivider();
            String input = ConsoleUtil.readOptionalString(
                    "Enter search keyword or Product ID (Enter for all, 0 to cancel): ");
            if ("0".equals(input)) {
                return null;
            }
            if (input.isEmpty()) {
                ProductMenu.renderProductTable(this.productController.getAllProducts());
                continue;
            }
            Product direct = resolveProductByIdOrKeyword(input);
            if (direct != null) {
                return direct;
            }
        }
    }

    private Product resolveProductByIdOrKeyword(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        String clean = input.trim();
        String lower = clean.toLowerCase(Locale.ROOT);
        if (lower.startsWith("product ")) {
            clean = clean.substring(8).trim();
        } else if (lower.startsWith("prod ")) {
            clean = clean.substring(5).trim();
        }

        String possibleId = InputValidator.tryNormalizeProductId(clean);
        Product direct = (possibleId != null) ? this.productController.findById(possibleId) : null;
        if (direct == null) {
            direct = this.productController.findById(clean);
        }
        if (direct != null) {
            return direct;
        }

        List<Product> matches = this.productController.search(clean);
        if (matches.isEmpty()) {
            System.out.println("  [ERROR] No active products match keyword or ID: " + input);
            return null;
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        ProductMenu.renderProductTable(matches);
        return null;
    }

    private void displayStockDepletedError(Product product) {
        if (product.getTotalStock() > 0 && product.getAvailableStock() == 0) {
            System.out.println("  [ERROR] Cannot add '" + product.getName()
                    + "': all batches in stock have expired (BR11).");
        } else {
            System.out.println("  [ERROR] Product '" + product.getName()
                    + "' has 0 remaining sellable stock available.");
        }
    }

    /**
     * Displays a summary table of all completed historical sales orders and
     * supports interactive live search and detailed invoice lookup (BR23, BR33).
     */
    public void handleViewAllOrders() {
        renderOrdersSummaryTable(this.orderController.getAllOrders());
        while (true) {
            printDivider();
            String input = ConsoleUtil.readOptionalString("Enter search keyword or Order ID (or 0 to cancel): ");
            if ("0".equals(input)) {
                break;
            }
            if (input.isEmpty()) {
                renderOrdersSummaryTable(this.orderController.getAllOrders());
                continue;
            }
            inspectOrderOrSearch(input);
        }
    }

    private void inspectOrderOrSearch(String input) {
        String possibleOrderId = InputValidator.tryNormalizeOrderId(input);
        Order directMatch = (possibleOrderId != null)
                ? this.orderController.findById(possibleOrderId)
                : null;
        if (directMatch != null) {
            renderInvoiceTable(directMatch);
            System.out.println("  Total orders displayed: 1");
        } else {
            List<Order> matching = this.orderController.searchOrders(input);
            renderOrdersSummaryTable(matching);
        }
    }

    /**
     * Renders the 80-column Sales Cart and Invoice Table for an Order.
     *
     * @param order the draft or finalized Order to display
     */
    public void renderInvoiceTable(Order order) {
        if (order == null) {
            System.out.println("  [INFO] No order selected.");
            return;
        }
        renderInvoiceHeader(order);
        renderInvoiceItems(order.getItems());
        renderInvoiceFooter(order);
    }

    private void renderInvoiceHeader(Order order) {
        Customer customer = (order.getCustomerId() == null)
                ? null
                : this.customerController.findByIdIncludeDeleted(order.getCustomerId());
        String customerText = (customer == null)
                ? Order.GUEST_CUSTOMER_ID
                : customer.getId() + " - " + customer.getFullName()
                        + " (" + customer.getCustomerType() + ")";

        System.out.println(INVOICE_BORDER);
        System.out.printf("| Order ID: %s - %-57s |%n",
                order.getOrderId(),
                InputValidator.formatDisplayDate(order.getOrderDate().toLocalDate()));
        System.out.printf("| Customer: %-66s |%n", TableFormatter.fit(customerText, 66));
        System.out.println(INVOICE_BORDER);
        System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                TableFormatter.fit("No.", 3),
                TableFormatter.fit("ProdID", 6),
                TableFormatter.fit("Product Name", 23),
                TableFormatter.fitRight("Qty", 5),
                TableFormatter.fitRight("Unit Price", 11),
                TableFormatter.fitRight("Subtotal", 13));
        System.out.println(INVOICE_BORDER);
    }

    private void renderInvoiceFooter(Order order) {
        System.out.println(INVOICE_BORDER);
        System.out.printf("| Gross Total  | %61s |%n",
                InputValidator.formatCurrency(order.getTotalAmount()));
        System.out.printf("| Discount     | %61s |%n",
                InputValidator.formatCurrency(order.getDiscount()));
        System.out.printf("| Final Total  | %61s |%n",
                InputValidator.formatCurrency(order.getFinalAmount()));
        System.out.println(INVOICE_BORDER);
    }

    private void renderInvoiceItems(List<OrderDetail> items) {
        if (items.isEmpty()) {
            System.out.printf("| %s |%n", TableFormatter.center("(Cart is empty)", 76));
            return;
        }
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
