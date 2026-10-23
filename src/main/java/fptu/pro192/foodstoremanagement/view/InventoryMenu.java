package fptu.pro192.foodstoremanagement.view;

import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import fptu.pro192.foodstoremanagement.util.TableFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interactive 80-column console menu dedicated to warehouse inventory and
 * batch lifecycle management: importing new batches, replenishing or adjusting
 * batch quantities, inspecting product batches, and monitoring low-stock,
 * near-expiry, and expired inventory (BR6, BR7, BR11, BR19, BR20, BR24, BR26,
 * BR30, BR33).
 */
public class InventoryMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "Import New Batch for Product",
            "Replenish Existing Batch Quantity (+Stock)",
            "Adjust Existing Batch Quantity (Set Exact Stock)",
            "View All Batches of a Product",
            "View Low-Stock Products Alert",
            "View Near-Expiry Batches Alert",
            "View Expired Batches Report"
    };

    private static final String BATCH_TABLE_BORDER = "+--------+--------+------------------+------------+------------+-------+--------+";
    private static final String BATCH_TABLE_HEADER = "| Batch  | ProdID | Product Name     | Prod Date  | Exp Date   |   Qty | Status |";

    private static final String LOW_STOCK_BORDER = "+--------+-------------------------+--------------+--------+--------+----------+";
    private static final String LOW_STOCK_HEADER = "| ID     | Product Name            | Category     | Unit   |  Avail |    Total |";

    private final ProductController productController;

    /**
     * Constructs an InventoryMenu bound to the shared ProductController.
     *
     * @param productController the non-null ProductController collaborator
     * @throws IllegalArgumentException if productController is null
     */
    public InventoryMenu(ProductController productController) {
        super("FOOD STORE MANAGEMENT SYSTEM > [4] INVENTORY", MENU_OPTIONS);
        if (productController == null) {
            throw new IllegalArgumentException("ProductController cannot be null.");
        }
        this.productController = productController;
    }

    @Override
    protected void handleChoice(int choice) {
        switch (choice) {
            case 1:
                handleImportNewBatch();
                break;
            case 2:
                handleReplenishBatch();
                break;
            case 3:
                handleAdjustBatchQuantity();
                break;
            case 4:
                handleViewProductBatches();
                break;
            case 5:
                handleViewLowStockProducts();
                break;
            case 6:
                handleViewNearExpiryBatches();
                break;
            case 7:
                handleViewExpiredBatches();
                break;
            default:
                System.out.println("  [ERROR] Unsupported menu option.");
                break;
        }
    }

    @Override
    protected boolean shouldPauseAfterAction(int choice) {
        return choice != 4;
    }

    private void handleImportNewBatch() {
        String rawProdId = ConsoleUtil.readCancellableString("Enter Product ID (e.g., P00001, or 0 to cancel): ");
        if (rawProdId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }
        Product product = this.productController.findById(rawProdId);
        if (product == null) {
            System.out.println("  [ERROR] Active product not found with ID: " + rawProdId);
            return;
        }

        System.out.printf("  Product: %s - %s (Current Available Stock: %d %s)%n",
                product.getId(), product.getName(),
                product.getAvailableStock(), product.getUnit());

        Integer quantity = ConsoleUtil.readPositiveInt("Enter batch quantity (> 0, or 0 to cancel): ");
        if (quantity == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        LocalDate productionDate = ConsoleUtil.readDate("Enter production date (dd/MM/yyyy, or 0 to cancel): ");
        if (productionDate == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        LocalDate expirationDate = ConsoleUtil.readDate("Enter expiration date (dd/MM/yyyy, or 0 to cancel): ");
        if (expirationDate == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        if (productionDate.isAfter(expirationDate)) {
            System.out.println("  [ERROR] Production date cannot be after expiration date (BR7).");
            return;
        }

        if (!ConsoleUtil.confirm("Are you sure you want to import this batch?")) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        try {
            Batch newBatch = this.productController.importBatch(
                    product.getId(), productionDate, expirationDate, quantity);
            System.out.printf("  [OK] Batch %s (%d %s) imported for product %s.%n",
                    newBatch.getBatchId(), quantity, product.getUnit(), product.getId());
            renderBatchTable(product.getBatches());
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleReplenishBatch() {
        String rawProdId = ConsoleUtil.readCancellableString("Enter Product ID (e.g., P00001, or 0 to cancel): ");
        if (rawProdId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }
        Product product = this.productController.findById(rawProdId);
        if (product == null) {
            System.out.println("  [ERROR] Active product not found with ID: " + rawProdId);
            return;
        }
        if (product.getBatches().isEmpty()) {
            System.out.println("  [WARN] Product has no existing batches. Please import a new batch first.");
            return;
        }
        renderBatchTable(product.getBatches());

        String rawBatchId = ConsoleUtil
                .readCancellableString("Enter Batch ID to replenish (e.g., B00001, or 0 to cancel): ");
        if (rawBatchId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Integer addedQty = ConsoleUtil.readPositiveInt("Enter quantity to add (> 0, or 0 to cancel): ");
        if (addedQty == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        if (!ConsoleUtil
                .confirm("\nAre you sure you want to replenish batch " + rawBatchId.trim() + " by " + addedQty + "?")) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        try {
            this.productController.replenishBatch(product.getId(), rawBatchId, addedQty);
            System.out.printf("  [OK] Added %d %s to batch %s. Total available stock: %d.%n",
                    addedQty, product.getUnit(),
                    InputValidator.normalizeBatchId(rawBatchId),
                    product.getAvailableStock());
            renderBatchTable(product.getBatches());
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleAdjustBatchQuantity() {
        String rawProdId = ConsoleUtil.readCancellableString("Enter Product ID (e.g., P00001, or 0 to cancel): ");
        if (rawProdId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }
        Product product = this.productController.findById(rawProdId);
        if (product == null) {
            System.out.println("  [ERROR] Active product not found with ID: " + rawProdId);
            return;
        }
        if (product.getBatches().isEmpty()) {
            System.out.println("  [WARN] Product has no batches to adjust.");
            return;
        }
        renderBatchTable(product.getBatches());

        String rawBatchId = ConsoleUtil
                .readCancellableString("Enter Batch ID to adjust (e.g., B00001, or 0 to cancel): ");
        if (rawBatchId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        int newQty = ConsoleUtil.readInt("Enter new exact batch quantity (>= 0): ", 0, Integer.MAX_VALUE);

        if (!ConsoleUtil
                .confirm(
                        "\nAre you sure you want to set batch " + rawBatchId.trim() + " quantity to " + newQty + "?")) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        try {
            this.productController.adjustBatchQuantity(product.getId(), rawBatchId, newQty);
            System.out.printf("  [OK] Batch %s quantity set to %d. Available product stock: %d.%n",
                    InputValidator.normalizeBatchId(rawBatchId),
                    newQty, product.getAvailableStock());
            renderBatchTable(product.getBatches());
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleViewProductBatches() {
        while (true) {
            String rawProdId = ConsoleUtil.readCancellableString("Enter Product ID (e.g., P00001, or 0 to cancel): ");
            if (rawProdId == null) {
                break;
            }
            Product product = this.productController.findById(rawProdId);
            if (product == null) {
                System.out.println("  [ERROR] Active product not found with ID: " + rawProdId);
                continue;
            }
            System.out.printf("  Product: %s - %s | Available: %d %s | Physical Total: %d %s%n",
                    product.getId(), product.getName(),
                    product.getAvailableStock(), product.getUnit(),
                    product.getTotalStock(), product.getUnit());
            renderBatchTable(product.getBatches());
        }
    }

    private void handleViewLowStockProducts() {
        Integer threshold = ConsoleUtil.readIntWithDefault(
                "Enter stock threshold (N, Enter for default 5, or 0 to cancel): ",
                1, Integer.MAX_VALUE, Product.DEFAULT_LOW_STOCK_THRESHOLD);
        if (threshold == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        List<Product> lowStock = this.productController.getLowStockProducts(threshold);
        if (lowStock.isEmpty()) {
            System.out.println("  [OK] No active products are at or below " + threshold + " units.");
            return;
        }
        System.out.println(LOW_STOCK_BORDER);
        System.out.println(LOW_STOCK_HEADER);
        System.out.println(LOW_STOCK_BORDER);
        for (Product p : lowStock) {
            System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                    TableFormatter.fit(p.getId(), 6),
                    TableFormatter.fit(p.getName(), 23),
                    TableFormatter.fit(p.getCategory(), 12),
                    TableFormatter.fit(p.getUnit(), 6),
                    TableFormatter.fitRight(String.valueOf(p.getAvailableStock()), 6),
                    TableFormatter.fitRight(String.valueOf(p.getTotalStock()), 8));
        }
        System.out.println(LOW_STOCK_BORDER);
        System.out.printf("  Total low-stock products (<= %d units): %d%n", threshold, lowStock.size());
    }

    private void handleViewNearExpiryBatches() {
        Integer days = ConsoleUtil.readIntWithDefault(
                "Enter warning window in days (N, Enter for default 7, or 0 to cancel): ",
                1, Integer.MAX_VALUE, Product.DEFAULT_NEAR_EXPIRY_DAYS);
        if (days == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        List<Batch> nearBatches = this.productController.getNearExpiryBatches(days);
        renderBatchTable(nearBatches);
    }

    private void handleViewExpiredBatches() {
        Integer windowDays = ConsoleUtil.readIntWithDefault(
                "Enter look-back window in days (N, Enter for all-expired, or 0 to cancel): ",
                1, Integer.MAX_VALUE, 0);
        if (windowDays == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        List<Batch> expiredBatches = this.productController.getExpiredBatches();
        if (windowDays > 0) {
            LocalDate cutoff = LocalDate.now().minusDays(windowDays);
            List<Batch> filtered = new ArrayList<>();
            for (Batch b : expiredBatches) {
                if (!b.getExpirationDate().isBefore(cutoff)) {
                    filtered.add(b);
                }
            }
            expiredBatches = filtered;
        }
        renderBatchTable(expiredBatches);
    }

    /**
     * Renders an 80-column ASCII table of inventory batches with their
     * shelf-life status (ACTIVE, NEAR, EXPRD, or EMPTY).
     *
     * @param batches the list of batches to display
     */
    public void renderBatchTable(List<Batch> batches) {
        if (batches == null || batches.isEmpty()) {
            System.out.println("  [INFO] No matching batches found.");
            return;
        }
        System.out.println(BATCH_TABLE_BORDER);
        System.out.println(BATCH_TABLE_HEADER);
        System.out.println(BATCH_TABLE_BORDER);
        for (Batch batch : batches) {
            Product parent = this.productController.findByIdIncludeDeleted(batch.getProductId());
            String prodName = (parent != null) ? parent.getName() : "Unknown";
            String status;
            if (batch.getQuantity() == 0) {
                status = "EMPTY";
            } else if (batch.isExpired()) {
                status = "EXPRD";
            } else if (batch.isNearExpiry(Product.DEFAULT_NEAR_EXPIRY_DAYS)) {
                status = "NEAR";
            } else {
                status = "ACTIVE";
            }
            System.out.printf("| %s | %s | %s | %s | %s | %s | %s |%n",
                    TableFormatter.fit(batch.getBatchId(), 6),
                    TableFormatter.fit(batch.getProductId(), 6),
                    TableFormatter.fit(prodName, 16),
                    TableFormatter.fit(InputValidator.formatDisplayDate(batch.getProductionDate()), 10),
                    TableFormatter.fit(InputValidator.formatDisplayDate(batch.getExpirationDate()), 10),
                    TableFormatter.fitRight(String.valueOf(batch.getQuantity()), 5),
                    TableFormatter.fit(status, 6));
        }
        System.out.println(BATCH_TABLE_BORDER);
        System.out.printf("  Total batches listed: %d%n", batches.size());
    }
}
