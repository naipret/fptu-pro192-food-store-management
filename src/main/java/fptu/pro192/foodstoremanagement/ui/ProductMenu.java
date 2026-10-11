package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.ProductFactory;
import fptu.pro192.foodstoremanagement.service.ProductService;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.TableFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Concrete console menu providing interactive catalog and inventory management.
 * <p>
 * Implements rigid 80-column ASCII formatting and enforces business rules:
 * BR1, BR3-BR7, BR10, BR11, BR14, BR19, BR20, BR20.1, BR20.2, BR24, BR26, BR27.
 */
public class ProductMenu extends AbstractMenu {

    private final ProductService productService;

    /**
     * Constructs a ProductMenu with the specified title and a default ProductService.
     *
     * @param title The menu screen title
     */
    public ProductMenu(String title) {
        this(title, new ProductService());
    }

    /**
     * Constructs a ProductMenu with injected ProductService (Dependency Injection).
     *
     * @param title The menu screen title
     * @param productService The product business service
     */
    public ProductMenu(String title, ProductService productService) {
        super(title);
        this.productService = (productService != null) ? productService : new ProductService();
    }

    @Override
    protected void renderBody() {
        System.out.println("1. Thêm sản phẩm mới (Add new product - Task B1)");
        System.out.println("2. Cập nhật thông tin sản phẩm (Update product - Task B2)");
        System.out.println("3. Xem danh sách tất cả sản phẩm (View all products - Task B3)");
        System.out.println("4. Tìm kiếm sản phẩm theo tên / loại (Search products - Task B4)");
        System.out.println("5. Xem sản phẩm tồn kho thấp (Low stock <= 5 - Task B11)");
        System.out.println("6. Xem các lô hàng đã hết hạn (Expired batches - Task B12)");
        System.out.println("7. Xem các lô hàng cận hạn sử dụng (Near-expiry <= 7d - Task B13)");
        System.out.println("8. Cập nhật tồn kho / Nhập thêm hàng (Restock product - Task B14)");
        System.out.println("9. Xóa sản phẩm (Delete product - Soft delete BR27)");
        System.out.println("0. Quay lại Menu chính");
    }

    @Override
    protected boolean handleOption(int choice) {
        switch (choice) {
            case 1:
                handleAddProduct();
                break;
            case 2:
                handleUpdateProduct();
                break;
            case 3:
                handleViewAllProducts();
                break;
            case 4:
                handleSearchProducts();
                break;
            case 5:
                handleViewLowStockProducts();
                break;
            case 6:
                handleViewExpiredProducts();
                break;
            case 7:
                handleViewNearExpiryProducts();
                break;
            case 8:
                handleRestockProduct();
                break;
            case 9:
                handleDeleteProduct();
                break;
            case 0:
                return false;
            default:
                System.out.println("Lựa chọn không hợp lệ. Vui lòng chọn lại!");
                break;
        }
        return true;
    }

    // =========================================================================
    // OPTION HANDLERS
    // =========================================================================

    /**
     * Task B1: Handles adding a new product and registering its initial inventory batch.
     * Enforces BR1, BR3-BR7, BR20.1, BR20.2, BR26.
     */
    private void handleAddProduct() {
        System.out.println("\n--- [1] THÊM SẢN PHẨM MỚI (TASK B1) ---");

        // 1. Product ID (BR1)
        String id;
        while (true) {
            id = ConsoleUtil.readString("Nhập mã sản phẩm (Định dạng P00001 - P99999, '0' để hủy): ", false).toUpperCase();
            if ("0".equals(id)) {
                System.out.println("Đã hủy thao tác thêm sản phẩm.");
                return;
            }
            if (!id.matches("^P\\d{5}$")) {
                System.out.println("Mã sản phẩm không đúng định dạng P00001 - P99999 (BR1)! Vui lòng nhập lại.");
                continue;
            }
            if (productService.findById(id) != null) {
                System.out.println("Mã sản phẩm " + id + " đã tồn tại trong hệ thống! Vui lòng chọn mã khác.");
                continue;
            }
            break;
        }

        // 2. Product Name (BR3, BR5.1)
        String name;
        while (true) {
            name = ConsoleUtil.readString("Nhập tên sản phẩm: ", false);
            if (!name.matches(".*[a-zA-ZÀ-ỹ].*")) {
                System.out.println("Tên sản phẩm phải chứa ký tự chữ cái, không được chỉ gồm số (BR5.1)!");
                continue;
            }
            break;
        }

        // 3. Category (BR3, Subclasses: FrozenFood, ChilledFood, DryFood)
        System.out.println("Chọn phân loại thực phẩm:");
        System.out.println("  1. FROZEN (Đông lạnh - Nhiệt độ -25°C đến -18°C)");
        System.out.println("  2. CHILLED (Tươi sống / Mát - Nhiệt độ 0°C đến 4°C)");
        System.out.println("  3. DRY (Đồ khô - Nhiệt độ 15°C đến 25°C)");
        int catChoice = ConsoleUtil.readInt("Lựa chọn [1 - 3]: ", 1, 3);
        String category;
        double minTemp, maxTemp, minHum, maxHum;
        switch (catChoice) {
            case 1:
                category = "FROZEN";
                minTemp = -25.0; maxTemp = -18.0; minHum = 85.0; maxHum = 95.0;
                break;
            case 2:
                category = "CHILLED";
                minTemp = 0.0; maxTemp = 4.0; minHum = 75.0; maxHum = 85.0;
                break;
            default:
                category = "DRY";
                minTemp = 15.0; maxTemp = 25.0; minHum = 30.0; maxHum = 60.0;
                break;
        }

        // 4. Unit (BR4)
        String unit = ConsoleUtil.readString("Nhập đơn vị tính (Goi, Hop, Kg, Gram, Lon, Chai...): ", false);

        // 5. Unit Price (BR5)
        double price = readPositiveDouble("Nhập đơn giá bán (VND, > 0): ");

        // 6. Initial Batch (BR7, BR20.1, BR20.2, BR26)
        System.out.println("\n--- THÔNG TIN LÔ HÀNG BAN ĐẦU ---");
        String defaultBatchId = "B" + id.substring(1);
        String batchPrompt = "Nhập mã lô hàng (Nhấn ENTER để dùng mặc định " + defaultBatchId + "): ";
        String batchId = ConsoleUtil.readString(batchPrompt, true);
        if (batchId.isEmpty()) {
            batchId = defaultBatchId;
        }

        int quantity = ConsoleUtil.readInt("Nhập số lượng nhập ban đầu (> 0): ", 1, 1_000_000);
        LocalDate today = LocalDate.now();

        LocalDate prodDate;
        while (true) {
            prodDate = readDate("Nhập ngày sản xuất (YYYY-MM-DD): ", false);
            if (prodDate.isAfter(today)) {
                System.out.println("Ngày sản xuất không được ở tương lai so với ngày hiện tại (" + today + ") (BR7)!");
                continue;
            }
            break;
        }

        LocalDate expDate;
        while (true) {
            expDate = readDate("Nhập ngày hết hạn (YYYY-MM-DD): ", false);
            if (expDate.isBefore(prodDate)) {
                System.out.println("Ngày hết hạn không được trước ngày sản xuất (" + prodDate + ") (BR7)!");
                continue;
            }
            break;
        }

        // BR20.1 & BR20.2: HACCP Warning Confirmation
        long daysRemaining = ChronoUnit.DAYS.between(today, expDate);
        if ("CHILLED".equalsIgnoreCase(category) && daysRemaining <= 1) {
            System.out.println("\n>>> CẢNH BÁO: Sản phẩm tươi sống (CHILLED) chỉ còn " + daysRemaining
                    + " ngày hạn sử dụng (<= 1 ngày) (BR20.1)!");
            String confirm = ConsoleUtil.readString("Bạn có chắc chắn muốn nhập lô hàng này không? (Y/N): ", false);
            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("Đã hủy thao tác nhập sản phẩm!");
                return;
            }
        } else if ("DRY".equalsIgnoreCase(category) && daysRemaining <= 7) {
            System.out.println("\n>>> CẢNH BÁO: Sản phẩm đồ khô (DRY) chỉ còn " + daysRemaining
                    + " ngày hạn sử dụng (<= 7 ngày) (BR20.2)!");
            String confirm = ConsoleUtil.readString("Bạn có chắc chắn muốn nhập lô hàng này không? (Y/N): ", false);
            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("Đã hủy thao tác nhập sản phẩm!");
                return;
            }
        }

        try {
            Food newProduct = ProductFactory.create(category, id, name, category, unit, price,
                    minTemp, maxTemp, minHum, maxHum, false);
            Batch initialBatch = new Batch(batchId, id, today, prodDate, expDate, quantity);
            productService.addProduct(newProduct, initialBatch, today);
            System.out.println("\n[✓] Thêm sản phẩm '" + name + "' (" + id + ") và lô hàng " + batchId + " thành công!");
        } catch (Exception e) {
            System.out.println("[✗] Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }

    /**
     * Task B2: Handles updating product information selectively.
     */
    private void handleUpdateProduct() {
        System.out.println("\n--- [2] CẬP NHẬT THÔNG TIN SẢN PHẨM (TASK B2) ---");
        String id = ConsoleUtil.readString("Nhập mã sản phẩm cần cập nhật (Pxxxxx, '0' để hủy): ", false).toUpperCase();
        if ("0".equals(id)) {
            return;
        }

        Food product = productService.findById(id);
        if (product == null) {
            System.out.println("[!] Không tìm thấy sản phẩm có mã '" + id + "' hoặc sản phẩm đã bị xóa.");
            return;
        }

        System.out.println("Thông tin hiện tại:");
        System.out.println("  - Tên: " + product.getName());
        System.out.println("  - Phân loại: " + product.getCategory());
        System.out.println("  - Đơn vị tính: " + product.getUnit());
        System.out.printf("  - Đơn giá: %,.0f VND\n", product.getPrice());
        System.out.println("  - Tổng tồn kho: " + product.getTotalStock());
        System.out.println("\n(Nhấn ENTER nếu muốn giữ nguyên thông tin hiện tại)");

        // New name
        String newName = ConsoleUtil.readString("Nhập tên mới: ", true);
        if (!newName.isEmpty()) {
            if (newName.matches(".*[a-zA-ZÀ-ỹ].*")) {
                product.setName(newName);
            } else {
                System.out.println("[!] Tên không hợp lệ, giữ nguyên tên cũ.");
            }
        }

        // New unit
        String newUnit = ConsoleUtil.readString("Nhập đơn vị tính mới: ", true);
        if (!newUnit.isEmpty()) {
            product.setUnit(newUnit);
        }

        // New price
        Double newPrice = readOptionalPositiveDouble("Nhập đơn giá mới (> 0): ");
        if (newPrice != null) {
            product.setPrice(newPrice);
        }

        // New quantity override
        Integer newQuantity = readOptionalPositiveInt("Nhập số lượng tồn kho mới (hoặc ENTER để bỏ qua): ");

        try {
            productService.updateProduct(id, newPrice, newQuantity);
            System.out.println("\n[✓] Cập nhật thông tin sản phẩm " + id + " thành công!");
        } catch (Exception e) {
            System.out.println("[✗] Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    /**
     * Task B3: Displays all active products in rigid 80-column ASCII table.
     */
    private void handleViewAllProducts() {
        System.out.println("\n--- [3] DANH SÁCH TẤT CẢ SẢN PHẨM (TASK B3) ---");
        List<Food> products = productService.getAllProducts();
        renderProductTable(products);
        System.out.println("Tổng số sản phẩm đang quản lý: " + products.size());
    }

    /**
     * Task B4: Searches products by keyword matching ID, name, or category.
     */
    private void handleSearchProducts() {
        System.out.println("\n--- [4] TÌM KIẾM SẢN PHẨM (TASK B4) ---");
        String keyword = ConsoleUtil.readString("Nhập từ khóa tìm kiếm (Mã SP, Tên, hoặc Phân loại): ", false);
        List<Food> results = productService.search(keyword);
        System.out.println("\nKết quả tìm kiếm cho: \"" + keyword + "\"");
        renderProductTable(results);
        System.out.println("Tìm thấy " + results.size() + " sản phẩm phù hợp.");
    }

    /**
     * Task B11 / BR19: Displays products with stock <= 5 units.
     */
    private void handleViewLowStockProducts() {
        System.out.println("\n--- [5] DANH SÁCH SẢN PHẨM TỒN KHO THẤP (<= 5 ĐƠN VỊ - TASK B11 / BR19) ---");
        List<Food> lowStock = productService.getLowStockProducts();
        renderProductTable(lowStock);
        if (lowStock.isEmpty()) {
            System.out.println("[✓] Tồn kho an toàn. Không có sản phẩm nào chạm ngưỡng tồn kho thấp (<= 5)!");
        } else {
            System.out.println("[!] Cảnh báo: Có " + lowStock.size() + " sản phẩm cần nhập thêm hàng gấp!");
        }
    }

    /**
     * Task B12 / BR11, BR11.1: Displays all expired inventory batches.
     */
    private void handleViewExpiredProducts() {
        LocalDate today = LocalDate.now();
        System.out.println("\n--- [6] DANH SÁCH LÔ HÀNG ĐÃ HẾT HẠN (TASK B12 / BR11) ---");
        System.out.println("Ngày kiểm tra hệ thống: " + today);
        List<Batch> expired = productService.getExpiredBatches(today);
        renderBatchTable(expired, today);
        if (expired.isEmpty()) {
            System.out.println("[✓] Không có lô hàng nào hết hạn sử dụng. Toàn bộ hàng hóa đạt chuẩn!");
        } else {
            System.out.println("[!] CẢNH BÁO: Phát hiện " + expired.size()
                    + " lô hàng đã hết hạn. Các lô hàng này BỊ KHÓA BÁN theo quy định BR11!");
        }
    }

    /**
     * Task B13 / BR20: Displays all active batches expiring within 7 days.
     */
    private void handleViewNearExpiryProducts() {
        LocalDate today = LocalDate.now();
        System.out.println("\n--- [7] DANH SÁCH LÔ HÀNG CẬN HẠN SỬ DỤNG (<= 7 NGÀY - TASK B13 / BR20) ---");
        System.out.println("Ngày kiểm tra hệ thống: " + today);
        List<Batch> nearExpiry = productService.getNearExpiryBatches(today);
        renderBatchTable(nearExpiry, today);
        if (nearExpiry.isEmpty()) {
            System.out.println("[✓] Không có lô hàng nào sắp hết hạn trong vòng 7 ngày tới.");
        } else {
            System.out.println("[!] Lưu ý: Có " + nearExpiry.size()
                    + " lô hàng cận hạn. Hệ thống sẽ tự động ưu tiên xuất bán theo thuật toán FEFO (BR24)!");
        }
    }

    /**
     * Task B14: Restocks an existing batch or imports a new batch for an existing product.
     */
    private void handleRestockProduct() {
        System.out.println("\n--- [8] CẬP NHẬT TỒN KHO / NHẬP THÊM HÀNG (TASK B14) ---");
        String productId = ConsoleUtil.readString("Nhập mã sản phẩm cần nhập hàng (Pxxxxx, '0' để hủy): ", false).toUpperCase();
        if ("0".equals(productId)) {
            return;
        }

        Food product = productService.findById(productId);
        if (product == null) {
            System.out.println("[!] Không tìm thấy sản phẩm có mã '" + productId + "'.");
            return;
        }

        System.out.println("Sản phẩm: " + product.getName() + " | Phân loại: " + product.getCategory()
                + " | Tồn kho hiện tại: " + product.getTotalStock());
        System.out.println("\nDanh sách lô hàng hiện tại của sản phẩm:");
        renderBatchTable(product.getBatches(), LocalDate.now());

        System.out.println("\nChọn hình thức nhập hàng:");
        System.out.println("  1. Tăng số lượng cho lô hàng đã có");
        System.out.println("  2. Nhập thêm lô hàng mới hoàn toàn");
        System.out.println("  0. Hủy thao tác");
        int restockChoice = ConsoleUtil.readInt("Lựa chọn [0 - 2]: ", 0, 2);

        if (restockChoice == 0) {
            System.out.println("Đã hủy thao tác.");
            return;
        }

        if (restockChoice == 1) {
            String batchId = ConsoleUtil.readString("Nhập mã lô hàng cần thêm số lượng: ", false).toUpperCase();
            int addQty = ConsoleUtil.readInt("Nhập số lượng bổ sung (> 0): ", 1, 1_000_000);
            try {
                productService.replenishStock(productId, batchId, addQty);
                System.out.println("\n[✓] Đã bổ sung +" + addQty + " sản phẩm vào lô " + batchId + " thành công!");
            } catch (Exception e) {
                System.out.println("[✗] Lỗi: " + e.getMessage());
            }
        } else {
            String defaultBatchId = "B" + productId.substring(1) + "_" + (product.getBatches().size() + 1);
            String batchId = ConsoleUtil.readString("Nhập mã lô hàng mới (Nhấn ENTER để dùng " + defaultBatchId + "): ", true);
            if (batchId.isEmpty()) {
                batchId = defaultBatchId;
            }

            int qty = ConsoleUtil.readInt("Nhập số lượng lô hàng mới (> 0): ", 1, 1_000_000);
            LocalDate today = LocalDate.now();

            LocalDate prodDate;
            while (true) {
                prodDate = readDate("Nhập ngày sản xuất (YYYY-MM-DD): ", false);
                if (prodDate.isAfter(today)) {
                    System.out.println("Ngày sản xuất không được ở tương lai so với ngày hiện tại (BR7)!");
                    continue;
                }
                break;
            }

            LocalDate expDate;
            while (true) {
                expDate = readDate("Nhập ngày hết hạn (YYYY-MM-DD): ", false);
                if (expDate.isBefore(prodDate)) {
                    System.out.println("Ngày hết hạn không được trước ngày sản xuất (BR7)!");
                    continue;
                }
                break;
            }

            // Shelf-life warning check
            long daysRemaining = ChronoUnit.DAYS.between(today, expDate);
            if ("CHILLED".equalsIgnoreCase(product.getCategory()) && daysRemaining <= 1) {
                System.out.println("\n>>> CẢNH BÁO: Lô hàng tươi sống chỉ còn " + daysRemaining + " ngày hạn sử dụng (BR20.1)!");
                String confirm = ConsoleUtil.readString("Bạn có chắc chắn muốn nhập không? (Y/N): ", false);
                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println("Đã hủy nhập hàng!");
                    return;
                }
            } else if ("DRY".equalsIgnoreCase(product.getCategory()) && daysRemaining <= 7) {
                System.out.println("\n>>> CẢNH BÁO: Lô hàng đồ khô chỉ còn " + daysRemaining + " ngày hạn sử dụng (BR20.2)!");
                String confirm = ConsoleUtil.readString("Bạn có chắc chắn muốn nhập không? (Y/N): ", false);
                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println("Đã hủy nhập hàng!");
                    return;
                }
            }

            try {
                Batch newBatch = new Batch(batchId, productId, today, prodDate, expDate, qty);
                productService.addBatchToProduct(productId, newBatch);
                System.out.println("\n[✓] Nhập lô hàng mới " + batchId + " cho sản phẩm " + productId + " thành công!");
            } catch (Exception e) {
                System.out.println("[✗] Lỗi: " + e.getMessage());
            }
        }
    }

    /**
     * BR27: Handles soft-deleting a product, preserving audit logs.
     */
    private void handleDeleteProduct() {
        System.out.println("\n--- [9] XÓA SẢN PHẨM (TASK BR27 - SOFT DELETE) ---");
        String productId = ConsoleUtil.readString("Nhập mã sản phẩm cần xóa (Pxxxxx, '0' để hủy): ", false).toUpperCase();
        if ("0".equals(productId)) {
            return;
        }

        Food product = productService.findById(productId);
        if (product == null) {
            System.out.println("[!] Không tìm thấy sản phẩm có mã '" + productId + "'.");
            return;
        }

        System.out.println("Sản phẩm tìm thấy: " + product.getName() + " (" + productId + ") - Tồn kho: " + product.getTotalStock());
        System.out.println("CHÚ Ý: Xóa sản phẩm sẽ ẩn sản phẩm khỏi danh sách bán nhưng vẫn lưu trữ dữ liệu lịch sử (BR27).");
        String confirm = ConsoleUtil.readString("Bạn có chắc chắn muốn xóa sản phẩm này không? (Y/N): ", false);

        if (confirm.equalsIgnoreCase("Y")) {
            boolean success = productService.deleteProduct(productId);
            if (success) {
                System.out.println("\n[✓] Đã xóa sản phẩm '" + product.getName() + "' (" + productId + ") thành công (BR27)!");
            } else {
                System.out.println("[✗] Không thể xóa sản phẩm.");
            }
        } else {
            System.out.println("Đã hủy thao tác xóa.");
        }
    }

    // =========================================================================
    // 80-COLUMN ASCII TABLE RENDERERS (Architecture Model Section 5.2.1)
    // =========================================================================

    /**
     * Renders a list of products in the strict 80-column ASCII table layout.
     * <p>
     * Layout specification (Architecture Model Section 5.2.1):
     * Columns: ID (6) + Name (22) + Category (9) + Unit (6) + Price (11) + Stock (7) = 61 chars.
     * Borders: 7 '|' borders + 12 spaces padding = 19 chars.
     * Total line width = 80 characters.
     *
     * @param products The list of products to display
     */
    public static void renderProductTable(List<Food> products) {
        String border = "+--------+------------------------+-----------+--------+-------------+---------+";
        System.out.println(border);
        System.out.println("| Mã SP  | Tên Sản Phẩm           | Phân Loại | ĐVT    | Đơn Giá     | Tồn Kho |");
        System.out.println(border);

        if (products == null || products.isEmpty()) {
            System.out.println("|" + TableFormatter.center("Không có sản phẩm nào", 78) + "|");
        } else {
            for (Food p : products) {
                String priceStr = String.format("%,.0f VND", p.getPrice());
                System.out.printf("| %-6s | %-22s | %-9s | %-6s | %11s | %7d |\n",
                        TableFormatter.fit(p.getId(), 6),
                        TableFormatter.fit(p.getName(), 22),
                        TableFormatter.fit(p.getCategory(), 9),
                        TableFormatter.fit(p.getUnit(), 6),
                        priceStr,
                        p.getTotalStock());
            }
        }
        System.out.println(border);
    }

    /**
     * Renders a list of inventory batches in a strict 80-column ASCII table layout.
     * <p>
     * Columns: Mã Lô (6) + Mã SP (6) + Ngày Nhập (10) + Ngày SX (10) + Hạn Dùng (10) + Còn Lại (8) + SL Tồn (8) = 58 chars.
     * Borders: 8 '|' borders + 14 spaces padding = 22 chars.
     * Total line width = 80 characters.
     *
     * @param batches The list of batches to display
     * @param today Reference date for shelf-life calculations
     */
    public static void renderBatchTable(List<Batch> batches, LocalDate today) {
        String border = "+--------+--------+------------+------------+------------+----------+----------+";
        System.out.println(border);
        System.out.println("| Mã Lô  | Mã SP  | Ngày Nhập  | Ngày SX    | Hạn Dùng   | Còn Lại  | SL Tồn   |");
        System.out.println(border);

        if (batches == null || batches.isEmpty()) {
            System.out.println("|" + TableFormatter.center("Không có lô hàng nào", 78) + "|");
        } else {
            for (Batch b : batches) {
                long days = b.getDaysRemaining(today);
                String daysStr = (days < 0) ? (Math.abs(days) + "d quá") : (days + " ngày");
                System.out.printf("| %-6s | %-6s | %-10s | %-10s | %-10s | %8s | %8d |\n",
                        TableFormatter.fit(b.getBatchId(), 6),
                        TableFormatter.fit(b.getProductId(), 6),
                        TableFormatter.fit(b.getImportDate().toString(), 10),
                        TableFormatter.fit(b.getProductionDate().toString(), 10),
                        TableFormatter.fit(b.getExpirationDate().toString(), 10),
                        TableFormatter.fit(daysStr, 8),
                        b.getQuantity());
            }
        }
        System.out.println(border);
    }

    // =========================================================================
    // HELPER INPUT METHODS
    // =========================================================================

    private double readPositiveDouble(String prompt) {
        while (true) {
            String s = ConsoleUtil.readString(prompt, false);
            try {
                double val = Double.parseDouble(s);
                if (val > 0) {
                    return val;
                }
                System.out.println("Đơn giá phải lớn hơn 0 (BR5)! Vui lòng nhập lại.");
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số hợp lệ!");
            }
        }
    }

    private Double readOptionalPositiveDouble(String prompt) {
        while (true) {
            String s = ConsoleUtil.readString(prompt, true);
            if (s.isEmpty()) {
                return null;
            }
            try {
                double val = Double.parseDouble(s);
                if (val > 0) {
                    return val;
                }
                System.out.println("Đơn giá phải lớn hơn 0 (BR5)! Vui lòng nhập lại.");
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số hợp lệ hoặc nhấn ENTER để bỏ qua!");
            }
        }
    }

    private Integer readOptionalPositiveInt(String prompt) {
        while (true) {
            String s = ConsoleUtil.readString(prompt, true);
            if (s.isEmpty()) {
                return null;
            }
            try {
                int val = Integer.parseInt(s);
                if (val >= 0) {
                    return val;
                }
                System.out.println("Số lượng không được âm (BR6, BR26)! Vui lòng nhập lại.");
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số nguyên hợp lệ hoặc nhấn ENTER để bỏ qua!");
            }
        }
    }

    private LocalDate readDate(String prompt, boolean allowDefaultToday) {
        while (true) {
            String s = ConsoleUtil.readString(prompt, allowDefaultToday);
            if (s.isEmpty() && allowDefaultToday) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("Định dạng ngày không hợp lệ! Vui lòng nhập định dạng YYYY-MM-DD (Ví dụ: 2026-10-15).");
            }
        }
    }
}
