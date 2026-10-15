package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.model.Customer;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.Order;
import fptu.pro192.foodstoremanagement.model.OrderDetail;
import fptu.pro192.foodstoremanagement.model.Transaction;
import fptu.pro192.foodstoremanagement.model.VIPCustomer;
import fptu.pro192.foodstoremanagement.repository.CustomerRepository;
import fptu.pro192.foodstoremanagement.service.CustomerService;
import fptu.pro192.foodstoremanagement.service.InsufficientStockException;
import fptu.pro192.foodstoremanagement.service.OrderService;
import fptu.pro192.foodstoremanagement.service.ProductService;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import fptu.pro192.foodstoremanagement.util.TableFormatter;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Console user interface menu managing sales checkout workflows (Tasks B8, B9, B10).
 * Strictly conforms to the 80-column ASCII terminal display standard and Template Method Pattern.
 */
public class SalesMenu extends AbstractMenu {

    private final OrderService orderService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final Scanner scanner;

    // Architecture Model Section 5.2.3: Exactly 80-column Cart / Bill Summary Table Border
    // +-----+------------------------------+-------+----------------+----------------+
    private static final String CART_TABLE_BORDER =
            "+-----+------------------------------+-------+----------------+----------------+";

    // 80-column Order History Table Border
    // +--------+--------+------------+---------------+--------------+----------------+
    private static final String ORDER_TABLE_BORDER =
            "+--------+--------+------------+---------------+--------------+----------------+";

    /**
     * Default constructor initializing dependencies with default instances.
     */
    public SalesMenu() {
        this(new OrderService());
    }

    /**
     * Constructs SalesMenu with an injected OrderService.
     *
     * @param orderService The order service
     */
    public SalesMenu(OrderService orderService) {
        this(orderService,
                (orderService != null) ? orderService.getProductService() : new ProductService(),
                (orderService != null) ? orderService.getCustomerService() : new CustomerService(new CustomerRepository()),
                ScannerManager.getInstance().getScanner());
    }

    /**
     * Constructs SalesMenu with injected services.
     *
     * @param orderService The order service
     * @param productService The product service
     * @param customerService The customer service
     */
    public SalesMenu(OrderService orderService, ProductService productService, CustomerService customerService) {
        this(orderService, productService, customerService, ScannerManager.getInstance().getScanner());
    }

    /**
     * Constructs SalesMenu with full dependency injection.
     *
     * @param orderService The order service
     * @param productService The product service
     * @param customerService The customer service
     * @param scanner The input scanner
     */
    public SalesMenu(OrderService orderService, ProductService productService, CustomerService customerService, Scanner scanner) {
        super("QUẢN LÝ BÁN HÀNG");
        if (orderService == null) {
            throw new IllegalArgumentException("OrderService cannot be null.");
        }
        this.orderService = orderService;
        this.productService = productService != null ? productService : orderService.getProductService();
        this.customerService = customerService != null ? customerService : orderService.getCustomerService();
        this.scanner = scanner != null ? scanner : ScannerManager.getInstance().getScanner();
    }

    @Override
    protected void renderBody() {
        System.out.println("  1. Tạo đơn hàng mới & Thanh toán (Task B8 - B10)");
        System.out.println("  2. Xem danh sách đơn hàng đã bán (Task B3)");
        System.out.println("  3. Xem chi tiết đơn hàng theo mã");
    }

    @Override
    protected int getMaxOption() {
        return 3;
    }

    @Override
    protected boolean handleOption(int choice) {
        switch (choice) {
            case 1:
                createNewSalesOrder();
                break;
            case 2:
                showOrderHistory();
                break;
            case 3:
                showOrderDetail();
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Interactive sales workflow coordinating Tasks B8, B9, and B10.
     */
    private void createNewSalesOrder() {
        System.out.println("\n--- BƯỚC 1: TẠO GIAO DỊCH BÁN HÀNG (TASK B8) ---");

        String orderId = ConsoleUtil.readString("Nhập mã đơn hàng [0 để hủy]: ", false);
        if ("0".equals(orderId)) {
            System.out.println("-> Đã hủy tạo đơn hàng.");
            return;
        }

        if (orderService.findById(orderId) != null) {
            System.out.println("Lỗi: Mã đơn hàng đã tồn tại trong hệ thống: " + orderId);
            return;
        }

        String customerId = ConsoleUtil.readString("Nhập mã khách hàng (bỏ trống nếu là khách vãng lai): ", true);
        Customer customer = null;
        if (!customerId.isEmpty()) {
            customer = customerService.findById(customerId);
            if (customer == null) {
                System.out.println("Khách hàng không tồn tại trong hệ thống. Đơn hàng sẽ được tạo cho khách vãng lai.");
                customerId = null;
            } else {
                String tier = (customer instanceof VIPCustomer) ? "VIP (Giảm 10%)" : "Thường (Giảm 0%)";
                System.out.printf("-> Khách hàng: %s | Hạng: %s\n", customer.getFullName(), tier);
            }
        }

        Order draft = orderService.createDraftOrder(orderId, customerId, LocalDate.now());
        System.out.println("-> Đã khởi tạo đơn hàng nháp: " + orderId);

        // Task B9: Interactive Item Addition Loop
        System.out.println("\n--- BƯỚC 2: THÊM SẢN PHẨM VÀO ĐƠN HÀNG (TASK B9) ---");
        while (true) {
            if (!draft.getItems().isEmpty()) {
                System.out.println("\n[GIỎ HÀNG HIỆN TẠI]");
                renderCartTable(draft.getItems());
            }

            String productId = ConsoleUtil.readString("Nhập Mã SP (hoặc '0' để sang thanh toán, 'C' để hủy đơn): ", false);
            if ("0".equalsIgnoreCase(productId)) {
                break;
            }
            if ("C".equalsIgnoreCase(productId)) {
                orderService.cancelDraft(draft);
                System.out.println("-> Đã hủy đơn hàng nháp. Tồn kho và dữ liệu không thay đổi.");
                return;
            }

            Food product = productService.findById(productId);
            if (product == null || product.isDeleted()) {
                System.out.println("Lỗi: Không tìm thấy sản phẩm hoặc sản phẩm đã bị xóa (BR8).");
                continue;
            }

            int activeStock = product.getTotalStock(LocalDate.now());
            System.out.printf("-> Sản phẩm: %s | ĐVT: %s | Đơn giá: %,.0f VND | Tồn khả dụng: %d\n",
                    product.getName(), product.getUnit(), product.getPrice(), activeStock);

            int quantity = ConsoleUtil.readInt("Nhập số lượng mua (> 0): ", 1, Integer.MAX_VALUE);

            try {
                orderService.addItemToDraft(draft, productId, quantity, LocalDate.now());
                System.out.println("-> Thêm sản phẩm thành công!");
            } catch (InsufficientStockException e) {
                System.out.println("Lỗi: " + e.getMessage()); // BR10
            } catch (IllegalArgumentException e) {
                System.out.println("Lỗi: " + e.getMessage()); // BR11, etc.
            }
        }

        // Task B10: Bill Summary and Confirmation
        if (draft.getItems().isEmpty()) {
            System.out.println("Đơn hàng chưa có sản phẩm nào. Đã hủy giao dịch.");
            return;
        }

        System.out.println("\n--- BƯỚC 3: HÓA ĐƠN & XÁC NHẬN BÁN HÀNG (TASK B10) ---");
        renderBillSummary(draft, customer);

        System.out.println("[1] Xác nhận thanh toán (Confirm Sale)");
        System.out.println("[2] Hủy đơn hàng (Cancel)");
        System.out.println("[0] Quay lại");
        int action = ConsoleUtil.readInt("Chọn hành động: ", 0, 2);

        if (action == 1) {
            System.out.println("Chọn phương thức thanh toán:");
            System.out.println("  1. Tiền mặt (CASH)");
            System.out.println("  2. Chuyển khoản (BANK_TRANSFER)");
            int pChoice = ConsoleUtil.readInt("Phương thức [1-2]: ", 1, 2);
            String paymentMethod = (pChoice == 2) ? "BANK_TRANSFER" : "CASH";

            Transaction tx = orderService.confirmSale(draft, LocalDate.now(), paymentMethod);
            System.out.println("=" + TableFormatter.repeat('=', MENU_WIDTH - 2) + "=");
            System.out.println(TableFormatter.center("XÁC NHẬN THANH TOÁN THÀNH CÔNG!", MENU_WIDTH));
            System.out.printf("  Mã Giao Dịch  : %s\n", tx.getTransactionId());
            System.out.printf("  Mã Đơn Hàng   : %s\n", draft.getOrderId());
            System.out.printf("  Số Tiền Đã Trả: %,.0f VND\n", tx.getTotalPaid());
            System.out.printf("  Phương Thức   : %s\n", tx.getPaymentMethod());
            System.out.printf("  Thời Gian     : %s\n", tx.getTransactionDate());
            System.out.println("=" + TableFormatter.repeat('=', MENU_WIDTH - 2) + "=");
        } else {
            orderService.cancelDraft(draft);
            System.out.println("-> Đã hủy đơn hàng. Tồn kho và dữ liệu không thay đổi.");
        }
    }

    /**
     * Renders 80-column Cart / Bill Summary Table conforming to Architecture Model Section 5.2.3.
     *
     * @param items List of OrderDetail items
     */
    private void renderCartTable(List<OrderDetail> items) {
        if (items == null || items.isEmpty()) {
            System.out.println("Giỏ hàng đang trống.");
            return;
        }

        System.out.println(CART_TABLE_BORDER);
        System.out.printf("| %3s | %-28s | %5s | %14s | %14s |\n",
                "STT", "Tên Sản Phẩm", "SL", "Đơn Giá", "Thành Tiền");
        System.out.println(CART_TABLE_BORDER);

        for (int i = 0; i < items.size(); i++) {
            OrderDetail d = items.get(i);
            System.out.printf("| %3d | %-28s | %5d | %14s | %14s |\n",
                    i + 1,
                    TableFormatter.fit(d.getProductName(), 28),
                    d.getQuantity(),
                    String.format("%,.0f VND", d.getUnitPrice()),
                    String.format("%,.0f VND", d.getSubtotal()));
        }
        System.out.println(CART_TABLE_BORDER);
    }

    /**
     * Renders detailed invoice summary with subtotal, discount, and final amount.
     *
     * @param order The completed or draft order
     * @param customer The attached customer (or null)
     */
    private void renderBillSummary(Order order, Customer customer) {
        renderCartTable(order.getItems());

        String discountNote = (customer instanceof VIPCustomer) ? " (VIP -10%)" : " (0%)";
        System.out.printf("  %-35s : %,16.0f VND\n", "Tạm tính (Subtotal)", order.getSubtotal());
        System.out.printf("  %-35s : %,16.0f VND\n", "Giảm giá" + discountNote, order.getDiscountAmount());
        System.out.printf("  %-35s : %,16.0f VND\n", "TỔNG THANH TOÁN (Final Amount)", order.getFinalAmount());
        System.out.println("-" + TableFormatter.repeat('-', MENU_WIDTH - 2) + "-");
    }

    /**
     * Displays all orders in 80-column tabular layout.
     */
    private void showOrderHistory() {
        List<Order> orders = orderService.getAllOrders();
        if (orders == null || orders.isEmpty()) {
            System.out.println("Chưa có đơn hàng nào được ghi nhận.");
            return;
        }

        System.out.println("\nDANH SÁCH ĐƠN HÀNG:");
        System.out.println(ORDER_TABLE_BORDER);
        System.out.printf("| %-6s | %-6s | %-10s | %13s | %12s | %14s |\n",
                "Mã ĐH", "Mã KH", "Ngày Bán", "Tạm Tính", "Giảm Giá", "Tổng Tiền");
        System.out.println(ORDER_TABLE_BORDER);

        for (Order o : orders) {
            String custId = (o.getCustomerId() != null) ? o.getCustomerId() : "VãngLai";
            String dateStr = (o.getOrderDate() != null) ? o.getOrderDate().toString() : "";
            System.out.printf("| %-6s | %-6s | %-10s | %13s | %12s | %14s |\n",
                    TableFormatter.fit(o.getOrderId(), 6),
                    TableFormatter.fit(custId, 6),
                    TableFormatter.fit(dateStr, 10),
                    String.format("%,.0f VND", o.getSubtotal()),
                    String.format("%,.0f VND", o.getDiscountAmount()),
                    String.format("%,.0f VND", o.getFinalAmount()));
        }
        System.out.println(ORDER_TABLE_BORDER);
    }

    /**
     * Displays detailed bill view for a specified order ID.
     */
    private void showOrderDetail() {
        String orderId = ConsoleUtil.readString("Nhập mã đơn hàng cần xem [0 để quay lại]: ", false);
        if ("0".equals(orderId)) {
            return;
        }

        Order order = orderService.findById(orderId);
        if (order == null) {
            System.out.println("Không tìm thấy đơn hàng với mã: " + orderId);
            return;
        }

        Customer customer = null;
        if (order.getCustomerId() != null && !order.getCustomerId().trim().isEmpty()) {
            customer = customerService.findById(order.getCustomerId());
        }

        System.out.println("\nCHI TIẾT HÓA ĐƠN: " + order.getOrderId());
        System.out.printf("Khách hàng: %s | Ngày đặt: %s | Trạng thái: %s\n",
                (customer != null ? customer.getFullName() + " (" + order.getCustomerId() + ")" : "Khách vãng lai"),
                order.getOrderDate(),
                order.isCompleted() ? "Đã hoàn thành" : "Nháp");
        renderBillSummary(order, customer);
    }
}
