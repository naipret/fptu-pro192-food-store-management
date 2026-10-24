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
import java.util.Locale;

/**
 * Interactive 80-column console menu for warehouse inventory and batch
 * lifecycle
 * management: importing batches, adjusting stock, inspecting batch listings,
 * and monitoring low-stock, near-expiry, and expired inventory (BR6, BR7, BR11,
 * BR19, BR20, BR24, BR26, BR30, BR33).
 */
public class InventoryMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "Import New Batch for Product",
            "Adjust Existing Batch Stock",
            "View All Batches (Overview & Details)",
            "Alert: Low-Stock Products",
            "Alert: Near-Expiry Batches",
            "Alert: Expired Batches"
    };

    private static final String BATCH_TABLE_BORDER = "+--------+--------+-----------------+------------+------------+-------+--------+";
    private static final String BATCH_TABLE_HEADER = "| Batch  | ProdID | Product Name    | Prod Date  | Exp Date   |   Qty | Status |";

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
                handleAdjustBatchStock();
                break;
            case 3:
                handleViewBatches();
                break;
            case 4:
                handleViewLowStockAlert();
                break;
            case 5:
                handleViewNearExpiryAlert();
                break;
            case 6:
                handleViewExpiredAlert();
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

    private void handleImportNewBatch() {
        while (true) {
            boolean imported = promptAndImportBatch();
            if (!imported) {
                break;
            }
            if (!ConsoleUtil.confirmWithDefault("\nImport another batch?", true)) {
                break;
            }
            System.out.println();
        }
    }

    private boolean promptAndImportBatch() {
        String rawProdId = ConsoleUtil.readCancellableString(
                "\nEnter Product ID to import batch for (or 0 to cancel): ");
        if (rawProdId == null) {
            return false;
        }
        Product product = this.productController.findById(rawProdId);
        if (product == null) {
            System.out.println("  [ERROR] Active product not found with ID: " + rawProdId);
            return false;
        }
        System.out.printf("  Selected: %s - %s (Current Available: %d %s)%n",
                product.getId(), product.getName(), product.getAvailableStock(), product.getUnit());

        LocalDate prodDate = ConsoleUtil.readDate("Enter production date (dd/MM/yyyy, or 0 to cancel): ");
        if (prodDate == null) {
            return false;
        }
        LocalDate expDate = ConsoleUtil.readDate("Enter expiration date (dd/MM/yyyy, or 0 to cancel): ");
        if (expDate == null) {
            return false;
        }
        if (prodDate.isAfter(expDate)) {
            System.out.println("  [ERROR] Production date cannot be after expiration date (BR7).");
            return false;
        }
        Integer qty = ConsoleUtil.readPositiveInt("Enter batch quantity (> 0, or 0 to cancel): ");
        if (qty == null) {
            return false;
        }
        return confirmAndPersistBatch(product, prodDate, expDate, qty);
    }

    private boolean confirmAndPersistBatch(Product product, LocalDate prodDate, LocalDate expDate, int qty) {
        if (!ConsoleUtil.confirm("Are you sure you want to import this batch?")) {
            System.out.println("  [INFO] Batch import cancelled.");
            return false;
        }
        try {
            Batch newBatch = this.productController.importBatch(
                    product.getId(), prodDate, expDate, qty);
            System.out.printf("  [OK] Batch %s (%d %s) imported for product %s.%n",
                    newBatch.getBatchId(), qty, product.getUnit(), product.getId());
            renderBatchTable(Collections.singletonList(newBatch));
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
            return false;
        }
    }

    private void handleAdjustBatchStock() {
        while (true) {
            String rawBatchId = ConsoleUtil.readCancellableString(
                    "\nEnter Batch ID to adjust stock (e.g., B00001, or 0 to cancel): ");
            if (rawBatchId == null) {
                break;
            }
            performBatchAdjustment(rawBatchId);
        }
    }

    private void performBatchAdjustment(String rawBatchId) {
        Batch target = findBatchAcrossProducts(rawBatchId);
        if (target == null) {
            System.out.println("  [ERROR] Batch not found with ID: " + rawBatchId);
            return;
        }
        Product parent = this.productController.findByIdIncludeDeleted(target.getProductId());
        String parentName = (parent != null) ? parent.getName() : "Unknown";
        System.out.printf("  Batch %s | Product: %s - %s | Current Stock: %d%n",
                target.getBatchId(), target.getProductId(), parentName, target.getQuantity());

        Integer newQty = ConsoleUtil.readPositiveInt(
                "Enter new exact batch quantity (>= 0, or 0 to set to zero): ", false);
        if (newQty == null) {
            return;
        }
        if (!ConsoleUtil.confirm("Confirm setting batch " + target.getBatchId() + " stock to " + newQty + "?")) {
            System.out.println("  [INFO] Adjustment cancelled.");
            return;
        }
        try {
            this.productController.adjustBatchQuantity(target.getProductId(), target.getBatchId(), newQty);
            System.out.printf("  [OK] Batch %s quantity updated to %d.%n", target.getBatchId(), newQty);
            renderBatchTable(Collections.singletonList(target));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private Batch findBatchAcrossProducts(String rawBatchId) {
        String targetId = InputValidator.tryNormalizeBatchId(rawBatchId);
        if (targetId == null) {
            targetId = rawBatchId.trim();
        }
        for (Product product : this.productController.getAllProducts()) {
            for (Batch batch : product.getBatches()) {
                if (batch.getBatchId().equalsIgnoreCase(targetId)) {
                    return batch;
                }
            }
        }
        return null;
    }

    private void handleViewBatches() {
        renderBatchTable(getAllActiveBatches());
        while (true) {
            String input = ConsoleUtil.readOptionalString(
                    "\nEnter search keyword or Product ID (or [Enter] to refresh, 0 to back): ");
            if ("0".equals(input)) {
                break;
            }
            if (input.isEmpty()) {
                renderBatchTable(getAllActiveBatches());
                continue;
            }
            inspectBatchesOrSearch(input);
        }
    }

    private void inspectBatchesOrSearch(String input) {
        String possibleProdId = InputValidator.tryNormalizeProductId(input);
        Product product = (possibleProdId != null)
                ? this.productController.findById(possibleProdId)
                : this.productController.findById(input);
        if (product != null) {
            System.out.printf("  Batches for product %s - %s (Total physical stock: %d):%n",
                    product.getId(), product.getName(), product.getTotalStock());
            renderBatchTable(product.getBatches());
            return;
        }
        List<Batch> matching = filterBatchesByKeyword(input);
        renderBatchTable(matching);
    }

    private List<Batch> filterBatchesByKeyword(String keyword) {
        String lower = keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedBatchId = InputValidator.tryNormalizeBatchId(keyword);
        List<Batch> result = new ArrayList<>();
        for (Batch batch : getAllActiveBatches()) {
            boolean matchBatchId = batch.getBatchId().toLowerCase(Locale.ROOT).contains(lower)
                    || (normalizedBatchId != null && batch.getBatchId().equalsIgnoreCase(normalizedBatchId));
            boolean matchProdId = batch.getProductId().toLowerCase(Locale.ROOT).contains(lower);
            Product p = this.productController.findByIdIncludeDeleted(batch.getProductId());
            boolean matchName = p != null && p.getName().toLowerCase(Locale.ROOT).contains(lower);
            if (matchBatchId || matchProdId || matchName) {
                result.add(batch);
            }
        }
        return result;
    }

    private List<Batch> getAllActiveBatches() {
        List<Batch> all = new ArrayList<>();
        for (Product product : this.productController.getAllProducts()) {
            all.addAll(product.getBatches());
        }
        return all;
    }

    private void handleViewLowStockAlert() {
        while (true) {
            Integer threshold = ConsoleUtil.readIntWithDefault(
                    "\nEnter stock threshold [Enter for default " + Product.DEFAULT_LOW_STOCK_THRESHOLD
                            + ", 0 to back]: ",
                    1, Integer.MAX_VALUE, Product.DEFAULT_LOW_STOCK_THRESHOLD);
            if (threshold == null) {
                break;
            }
            List<Product> lowStock = this.productController.getLowStockProducts(threshold);
            renderLowStockTable(lowStock, threshold);
        }
    }

    private void renderLowStockTable(List<Product> lowStock, int threshold) {
        if (lowStock == null || lowStock.isEmpty()) {
            System.out.printf("  [OK] No active products are at or below %d units.%n", threshold);
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

    private void handleViewNearExpiryAlert() {
        while (true) {
            Integer days = ConsoleUtil.readIntWithDefault(
                    "\nEnter warning window in days [Enter for default " + Product.DEFAULT_NEAR_EXPIRY_DAYS
                            + ", 0 to back]: ",
                    1, Integer.MAX_VALUE, Product.DEFAULT_NEAR_EXPIRY_DAYS);
            if (days == null) {
                break;
            }
            List<Batch> nearBatches = this.productController.getNearExpiryBatches(days);
            renderBatchTable(nearBatches);
        }
    }

    private void handleViewExpiredAlert() {
        while (true) {
            Integer windowDays = ConsoleUtil.readIntWithDefault(
                    "\nEnter look-back window in days [Enter for all expired, 0 to back]: ",
                    1, Integer.MAX_VALUE, 0);
            if (windowDays == null) {
                break;
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
            String status = determineBatchStatus(batch);
            System.out.printf("| %s | %s | %s | %s | %s | %s | %s |%n",
                    TableFormatter.fit(batch.getBatchId(), 6),
                    TableFormatter.fit(batch.getProductId(), 6),
                    TableFormatter.fit(prodName, 15),
                    TableFormatter.fit(InputValidator.formatDisplayDate(batch.getProductionDate()), 10),
                    TableFormatter.fit(InputValidator.formatDisplayDate(batch.getExpirationDate()), 10),
                    TableFormatter.fitRight(String.valueOf(batch.getQuantity()), 5),
                    TableFormatter.fit(status, 6));
        }
        System.out.println(BATCH_TABLE_BORDER);
        System.out.printf("  Total batches listed: %d%n", batches.size());
    }

    private String determineBatchStatus(Batch batch) {
        if (batch.getQuantity() == 0) {
            return "EMPTY";
        }
        if (batch.isExpired()) {
            return "EXPRD";
        }
        if (batch.isNearExpiry(Product.DEFAULT_NEAR_EXPIRY_DAYS)) {
            return "NEAR";
        }
        return "ACTIVE";
    }
}
