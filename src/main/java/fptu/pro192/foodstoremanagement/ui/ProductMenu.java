package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.ChilledFood;
import fptu.pro192.foodstoremanagement.model.DryFood;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.FrozenFood;
import fptu.pro192.foodstoremanagement.model.ProductFactory;
import fptu.pro192.foodstoremanagement.service.ProductService;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.ScannerManager;
import fptu.pro192.foodstoremanagement.util.TableFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Terminal presentation menu for product catalog and warehouse inventory management.
 * Strictly conforms to 80-column ASCII layout, Vietnamese UI language, and Template Method Pattern.
 *
 * Tasks: B1 (Add), B2 (Update), B3 (List), B4 (Search), B11 (Low stock), B12 (Expired),
 *        B13 (Near-expiry), B14 (Replenish/intake).
 */
public class ProductMenu extends AbstractMenu {

    private final ProductService productService;
    private final Scanner scanner;

    // Architecture Model Section 5.2.1: Exactly 80 columns table border
    // +--------+------------------------+-----------+--------+-------------+---------+
    private static final String PRODUCT_TABLE_BORDER =
            "+--------+------------------------+-----------+--------+-------------+---------+";

    // 80-column batch table border:
    // +--------+--------+----------------------+-------+------------+---------------+
    private static final String BATCH_TABLE_BORDER =
            "+--------+--------+----------------------+-------+------------+---------------+";

    public ProductMenu() {
        this(new ProductService(), ScannerManager.getInstance().getScanner());
    }

    public ProductMenu(ProductService productService) {
        this(productService, ScannerManager.getInstance().getScanner());
    }

    public ProductMenu(ProductService productService, Scanner scanner) {
        super("QUẢN LÝ SẢN PHẨM & KHO HÀNG");
        if (productService == null) {
            throw new IllegalArgumentException("ProductService cannot be null");
        }
        this.productService = productService;
        this.scanner = scanner != null ? scanner : ScannerManager.getInstance().getScanner();
    }

    @Override
    protected void renderBody() {
        System.out.println("  1. Thêm sản phẩm mới (Task B1)");
        System.out.println("  2. Cập nhật thông tin sản phẩm (Task B2)");
        System.out.println("  3. Xem danh sách tất cả sản phẩm (Task B3)");
        System.out.println("  4. Tìm kiếm sản phẩm theo Tên / Phân loại (Task B4)");
        System.out.println("  5. Xem sản phẩm tồn kho thấp (<= 5) (Task B11)");
        System.out.println("  6. Xem các lô hàng đã hết hạn (Task B12)");
        System.out.println("  7. Xem các lô hàng cận hạn sử dụng (Task B13)");
        System.out.println("  8. Cập nhật tồn kho / Nhập thêm lô hàng (Task B14)");
    }

    @Override
    protected int getMaxOption() {
        return 8;
    }

    @Override
    protected boolean handleOption(int choice) {
        switch (choice) {
            case 1:
                addNewProduct();
                break;
            case 2:
                updateProduct();
                break;
            case 3:
                showAllProducts();
                break;
            case 4:
                searchProducts();
                break;
            case 5:
                showLowStockProducts();
                break;
            case 6:
                showExpiredBatches();
                break;
            case 7:
                showNearExpiryBatches();
                break;
            case 8:
                replenishStock();
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Renders products conforming strictly to 80-column ASCII table layout (Architecture Model 5.2.1).
     * Columns: Mã SP (6) | Tên Sản Phẩm (22) | Phân Loại (9) | ĐVT (6) | Đơn Giá (11) | Tồn Kho (7)
     * Total width: 19 characters border/padding + 61 characters content = 80 characters.
     *
     * @param productList The list of products to render
     */
    private void renderProductTable(List<Food> productList) {
        if (productList == null || productList.isEmpty()) {
            System.out.println("Chưa có dữ liệu sản phẩm nào.");
            return;
        }

        System.out.println(PRODUCT_TABLE_BORDER);
        System.out.printf("| %-6s | %-22s | %-9s | %-6s | %11s | %7s |\n",
                "Mã SP", "Tên Sản Phẩm", "Phân Loại", "ĐVT", "Đơn Giá", "Tồn Kho");
        System.out.println(PRODUCT_TABLE_BORDER);

        for (Food p : productList) {
            String formattedPrice = String.format("%,d VND", (long) p.getPrice());
            String formattedStock = String.format("%,d", p.getTotalStock());

            System.out.printf("| %-6s | %-22s | %-9s | %-6s | %11s | %7s |\n",
                    TableFormatter.fit(p.getId(), 6),
                    TableFormatter.fit(p.getName(), 22),
                    TableFormatter.fit(p.getCategory(), 9),
                    TableFormatter.fit(p.getUnit(), 6),
                    TableFormatter.fit(formattedPrice, 11),
                    TableFormatter.fit(formattedStock, 7));
        }

        System.out.println(PRODUCT_TABLE_BORDER);
    }

    /**
     * Renders batch collection in an exact 80-column ASCII table layout.
     * Columns: Mã Lô (6) | Mã SP (6) | Tên Sản Phẩm (20) | SL (5) | Hạn Dùng (10) | Trạng Thái (13)
     * Total width: 19 border/padding + 60 content + 1 = 80 characters.
     *
     * @param batchList The list of batches to render
     * @param statusLabel Column header for the status column
     */
    private void renderBatchTable(List<Batch> batchList, String statusLabel) {
        if (batchList == null || batchList.isEmpty()) {
            System.out.println("Không có lô hàng nào phù hợp điều kiện.");
            return;
        }

        System.out.println(BATCH_TABLE_BORDER);
        System.out.printf("| %-6s | %-6s | %-20s | %5s | %-10s | %-13s |\n",
                "Mã Lô", "Mã SP", "Tên Sản Phẩm", "SL", "Hạn Dùng", statusLabel);
        System.out.println(BATCH_TABLE_BORDER);

        LocalDate today = LocalDate.now();
        for (Batch b : batchList) {
            Food product = productService.findById(b.getProductId());
            String productName = (product != null) ? product.getName() : "N/A";

            String status;
            if (b.isExpired(today)) {
                status = "ĐÃ HẾT HẠN";
            } else {
                long days = b.getDaysRemaining(today);
                status = "Còn " + days + " ngày";
            }

            System.out.printf("| %-6s | %-6s | %-20s | %5s | %-10s | %-13s |\n",
                    TableFormatter.fit(b.getBatchId(), 6),
                    TableFormatter.fit(b.getProductId(), 6),
                    TableFormatter.fit(productName, 20),
                    TableFormatter.fit(String.valueOf(b.getQuantity()), 5),
                    TableFormatter.fit(b.getExpirationDate().toString(), 10),
                    TableFormatter.fit(status, 13));
        }

        System.out.println(BATCH_TABLE_BORDER);
    }

    /**
     * Task B1: Prompts user and registers a new product along with its initial inventory batch.
     */
    private void addNewProduct() {
        ConsoleUtil.clearScreen();
        System.out.println("--- THÊM SẢN PHẨM MỚI (TASK B1) ---");

        try {
            // 1. Product catalog metadata
            System.out.print("Nhập Phân loại (FROZEN / CHILLED / DRY): ");
            String category = scanner.nextLine().trim().toUpperCase();
            if (!category.equals("FROZEN") && !category.equals("CHILLED") && !category.equals("DRY")) {
                System.out.println("Lỗi: Phân loại phải là một trong các giá trị: FROZEN, CHILLED, DRY.");
                return;
            }

            System.out.print("Nhập Mã sản phẩm (Định dạng Pxxxxx, e.g. P00001): ");
            String id = scanner.nextLine().trim();

            System.out.print("Nhập Tên sản phẩm: ");
            String name = scanner.nextLine().trim();

            System.out.print("Nhập Đơn vị tính (Gram, Hop, Goi, Chai, Lon, Khay...): ");
            String unit = scanner.nextLine().trim();

            System.out.print("Nhập Đơn giá (VND, > 0): ");
            String priceStr = scanner.nextLine().trim();
            double price = Double.parseDouble(priceStr);

            // 2. Initial batch information
            System.out.println("\n--- Thông tin lô hàng ban đầu ---");
            System.out.print("Nhập Mã lô hàng (e.g. B00001): ");
            String batchId = scanner.nextLine().trim();

            System.out.print("Nhập Số lượng tồn kho ban đầu (số nguyên >= 0): ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());

            LocalDate today = LocalDate.now();
            System.out.print("Nhập Ngày sản xuất (yyyy-MM-dd, để trống lấy hôm nay): ");
            String prodDateStr = scanner.nextLine().trim();
            LocalDate prodDate = prodDateStr.isEmpty() ? today : LocalDate.parse(prodDateStr);

            System.out.print("Nhập Ngày hết hạn (yyyy-MM-dd): ");
            String expDateStr = scanner.nextLine().trim();
            LocalDate expDate = LocalDate.parse(expDateStr);

            // BR20.1 & BR20.2: Shelf-life warning check on import
            Batch batch = new Batch(batchId, id, today, prodDate, expDate, quantity);
            if (category.equals("CHILLED") && batch.isCloseToExpiry(today, ChilledFood.EXPIRY_WARNING_DAYS)) {
                System.out.print("CẢNH BÁO: Hạn sử dụng của thực phẩm tươi sống <= 1 ngày (BR20.1). Tiếp tục nhập? (Y/N): ");
                String confirm = scanner.nextLine().trim();
                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println("Đã hủy thêm sản phẩm.");
                    return;
                }
            } else if (category.equals("DRY") && batch.isCloseToExpiry(today, DryFood.EXPIRY_WARNING_DAYS)) {
                System.out.print("CẢNH BÁO: Hạn sử dụng của thực phẩm khô <= 7 ngày (BR20.2). Tiếp tục nhập? (Y/N): ");
                String confirm = scanner.nextLine().trim();
                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println("Đã hủy thêm sản phẩm.");
                    return;
                }
            }

            // Create concrete food instance via Factory
            Food product;
            switch (category) {
                case "FROZEN":
                    product = ProductFactory.create(category, id, name, category, unit, price,
                            FrozenFood.DEFAULT_MIN_TEMP, FrozenFood.DEFAULT_MAX_TEMP,
                            FrozenFood.DEFAULT_MIN_HUMIDITY, FrozenFood.DEFAULT_MAX_HUMIDITY, false);
                    break;
                case "CHILLED":
                    product = ProductFactory.create(category, id, name, category, unit, price,
                            ChilledFood.DEFAULT_MIN_TEMP, ChilledFood.DEFAULT_MAX_TEMP,
                            ChilledFood.DEFAULT_MIN_HUMIDITY, ChilledFood.DEFAULT_MAX_HUMIDITY, false);
                    break;
                case "DRY":
                default:
                    product = ProductFactory.create(category, id, name, category, unit, price,
                            DryFood.DEFAULT_MIN_TEMP, DryFood.DEFAULT_MAX_TEMP,
                            DryFood.DEFAULT_MIN_HUMIDITY, DryFood.DEFAULT_MAX_HUMIDITY, false);
                    break;
            }

            productService.addProduct(product, batch, today);
            System.out.println("Thêm sản phẩm và lô hàng thành công!");

        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Số lượng hoặc đơn giá không hợp lệ. Vui lòng nhập số.");
        } catch (DateTimeParseException e) {
            System.out.println("Lỗi: Định dạng ngày không đúng (phải là yyyy-MM-dd, ví dụ: 2026-10-30).");
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi nghiệp vụ: " + e.getMessage());
        }
    }

    /**
     * Task B2: Selectively updates product price and/or stock quantity.
     */
    private void updateProduct() {
        ConsoleUtil.clearScreen();
        System.out.println("--- CẬP NHẬT THÔNG TIN SẢN PHẨM (TASK B2) ---");

        System.out.print("Nhập Mã sản phẩm cần cập nhật (Pxxxxx): ");
        String id = scanner.nextLine().trim();

        Food product = productService.findById(id);
        if (product == null) {
            System.out.println("Lỗi: Không tìm thấy sản phẩm có mã: " + id);
            return;
        }

        System.out.println("\nThông tin hiện tại:");
        System.out.println("  Tên SP   : " + product.getName());
        System.out.println("  Phân loại: " + product.getCategory());
        System.out.println("  Đơn vị   : " + product.getUnit());
        System.out.printf("  Đơn giá  : %,d VND\n", (long) product.getPrice());
        System.out.printf("  Tồn kho  : %,d\n", product.getTotalStock());

        System.out.println("\n(Để trống nếu không muốn thay đổi giá trị)");
        try {
            System.out.print("Nhập Đơn giá mới (VND): ");
            String priceInput = scanner.nextLine().trim();
            Double newPrice = priceInput.isEmpty() ? null : Double.parseDouble(priceInput);

            System.out.print("Nhập Tồn kho cập nhật mới: ");
            String qtyInput = scanner.nextLine().trim();
            Integer newQuantity = qtyInput.isEmpty() ? null : Integer.parseInt(qtyInput);

            if (newPrice == null && newQuantity == null) {
                System.out.println("Không có thông tin nào được cập nhật.");
                return;
            }

            productService.updateProduct(id, newPrice, newQuantity);
            System.out.println("Cập nhật thông tin sản phẩm thành công!");
        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Giá hoặc số lượng nhập vào không hợp lệ.");
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi nghiệp vụ: " + e.getMessage());
        }
    }

    /**
     * Task B3: Lists all active products in an 80-column ASCII table layout.
     */
    private void showAllProducts() {
        ConsoleUtil.clearScreen();
        System.out.println("--- DANH SÁCH TẤT CẢ SẢN PHẨM (TASK B3) ---");
        List<Food> products = productService.getAllProducts();
        renderProductTable(products);
    }

    /**
     * Task B4: Searches products by keyword matching ID, name, or category.
     */
    private void searchProducts() {
        ConsoleUtil.clearScreen();
        System.out.println("--- TÌM KIẾM SẢN PHẨM (TASK B4) ---");
        System.out.print("Nhập từ khóa tìm kiếm (Mã SP / Tên / Phân loại): ");
        String keyword = scanner.nextLine().trim();

        List<Food> results = productService.search(keyword);
        if (results.isEmpty()) {
            System.out.println("Không tìm thấy sản phẩm nào khớp với từ khóa: '" + keyword + "'");
        } else {
            System.out.println("Tìm thấy " + results.size() + " sản phẩm:");
            renderProductTable(results);
        }
    }

    /**
     * Task B11: Displays products with low stock (total active stock <= 5 units, BR19).
     */
    private void showLowStockProducts() {
        ConsoleUtil.clearScreen();
        System.out.println("--- DANH SÁCH SẢN PHẨM TỒN KHO THẤP (<= 5 ĐƠN VỊ - TASK B11) ---");
        List<Food> lowStock = productService.getLowStockProducts();
        if (lowStock.isEmpty()) {
            System.out.println("Không có sản phẩm nào ở mức tồn kho thấp.");
        } else {
            renderProductTable(lowStock);
        }
    }

    /**
     * Task B12: Displays all batches that have expired relative to system date (BR11.1).
     */
    private void showExpiredBatches() {
        ConsoleUtil.clearScreen();
        System.out.println("--- DANH SÁCH LÔ HÀNG ĐÃ HẾT HẠN (TASK B12) ---");
        LocalDate today = LocalDate.now();
        List<Batch> expired = productService.getExpiredBatches(today);
        if (expired.isEmpty()) {
            System.out.println("Không có lô hàng nào đã hết hạn.");
        } else {
            renderBatchTable(expired, "Trạng Thái");
        }
    }

    /**
     * Task B13: Displays active batches expiring within 7 days of system date (BR20).
     */
    private void showNearExpiryBatches() {
        ConsoleUtil.clearScreen();
        System.out.println("--- DANH SÁCH LÔ HÀNG CẬN HẠN SỬ DỤNG (<= 7 NGÀY - TASK B13) ---");
        LocalDate today = LocalDate.now();
        List<Batch> nearExpiry = productService.getNearExpiryBatches(today);
        if (nearExpiry.isEmpty()) {
            System.out.println("Không có lô hàng nào cận hạn sử dụng trong vòng 7 ngày tới.");
        } else {
            renderBatchTable(nearExpiry, "Cận Hạn");
        }
    }

    /**
     * Task B14: Replenishes stock quantity for an existing batch or attaches a new batch to a product.
     */
    private void replenishStock() {
        ConsoleUtil.clearScreen();
        System.out.println("--- CẬP NHẬT TỒN KHO / NHẬP THÊM HÀNG (TASK B14) ---");

        System.out.print("Nhập Mã sản phẩm cần nhập thêm hàng (Pxxxxx): ");
        String productId = scanner.nextLine().trim();

        Food product = productService.findById(productId);
        if (product == null) {
            System.out.println("Lỗi: Không tìm thấy sản phẩm có mã: " + productId);
            return;
        }

        System.out.println("Sản phẩm: " + product.getName() + " (Tồn kho hiện tại: " + product.getTotalStock() + ")");
        System.out.println("  1. Cộng thêm số lượng vào lô hàng đã có");
        System.out.println("  2. Nhập thêm một lô hàng mới hoàn toàn");
        System.out.print("Chọn thao tác (1/2): ");
        String subChoice = scanner.nextLine().trim();

        try {
            if (subChoice.equals("1")) {
                System.out.println("\nCác lô hàng hiện có của sản phẩm:");
                for (Batch b : product.getBatches()) {
                    System.out.printf("  - Lô: %s | SL: %d | HSD: %s\n",
                            b.getBatchId(), b.getQuantity(), b.getExpirationDate());
                }
                System.out.print("Nhập Mã lô hàng cần cộng thêm: ");
                String batchId = scanner.nextLine().trim();
                System.out.print("Nhập Số lượng cộng thêm (> 0): ");
                int addQty = Integer.parseInt(scanner.nextLine().trim());

                productService.replenishStock(productId, batchId, addQty);
                System.out.println("Cộng thêm số lượng vào lô hàng thành công!");

            } else if (subChoice.equals("2")) {
                System.out.print("Nhập Mã lô hàng mới (e.g. B00002): ");
                String batchId = scanner.nextLine().trim();
                System.out.print("Nhập Số lượng nhập (> 0): ");
                int quantity = Integer.parseInt(scanner.nextLine().trim());

                LocalDate today = LocalDate.now();
                System.out.print("Nhập Ngày sản xuất (yyyy-MM-dd, để trống lấy hôm nay): ");
                String prodDateStr = scanner.nextLine().trim();
                LocalDate prodDate = prodDateStr.isEmpty() ? today : LocalDate.parse(prodDateStr);

                System.out.print("Nhập Ngày hết hạn (yyyy-MM-dd): ");
                LocalDate expDate = LocalDate.parse(scanner.nextLine().trim());

                Batch newBatch = new Batch(batchId, productId, today, prodDate, expDate, quantity);
                productService.addBatchToProduct(productId, newBatch);
                System.out.println("Nhập thêm lô hàng mới cho sản phẩm thành công!");

            } else {
                System.out.println("Lựa chọn không hợp lệ.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Số lượng nhập vào không hợp lệ.");
        } catch (DateTimeParseException e) {
            System.out.println("Lỗi: Định dạng ngày không đúng (phải là yyyy-MM-dd).");
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi nghiệp vụ: " + e.getMessage());
        }
    }
}
