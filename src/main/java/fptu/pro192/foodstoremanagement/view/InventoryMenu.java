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
 * lifecycle management: importing batches, adjusting stock, inspecting batch
 * listings, and monitoring low-stock, near-expiry, and expired inventory (BR6,
 * BR7, BR11, BR19, BR20, BR24, BR26, BR30, BR33).
 */
public class InventoryMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "Import New Batch for Product",
            "View All Batches of a Product",
            "Adjust Existing Batch Quantity",
            "Alert Low-Stock Products",
            "Alert Near-Expiry Batches",
            "Alert Expired Batches"
    };

    private static final String BATCH_TABLE_BORDER = "+--------+--------+-----------------+------------+------------+-------+--------+";
    private static final String BATCH_TABLE_HEADER = "| Batch  | ProdID | Product Name    | Prod Date  | Exp Date   |   Qty | Status |";
    private static final String LOW_STOCK_BORDER = "+--------+-------------------------+--------------+--------+--------+----------+";
    private static final String LOW_STOCK_HEADER = "| ID     | Product Name            | Category     | Unit   |  Avail |    Total |";
    private static final String PRODUCT_BATCHES_BORDER = "+--------+------------+------------+------------+----------+-------------------+";
    private static final String PRODUCT_BATCHES_HEADER = "| Batch  | ImportDate | Prod Date  | Exp Date   | Status   |               Qty |";
    private static final String PRODUCT_BATCHES_TOTAL_BORDER = "+----------------------------------------------------------+-------------------+";

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
                handleViewBatches();
                break;
            case 3:
                handleAdjustBatchStock();
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

    private void handleImportNewBatch() {
        while (true) {
            String rawProdId = ConsoleUtil.readString(
                    "Enter Product ID to import batch for (or 0 to cancel): ");
            if (rawProdId == null) {
                break;
            }
            Product product = this.productController.findById(rawProdId);
            if (product == null) {
                System.out.println("  [ERROR] Active product not found with ID: " + rawProdId);
                printDivider();
                continue;
            }
            ProductMenu.renderProductTable(Collections.singletonList(product));
            System.out.println();

            LocalDate prodDate = ConsoleUtil.readDate(
                    "Enter production date [dd/MM/yyyy] (Enter for today, 0 to cancel): ",
                    LocalDate.now());
            if (prodDate == null) {
                System.out.println("  [INFO] Batch import cancelled.");
                printDivider();
                continue;
            }

            LocalDate expDate = ConsoleUtil.readDate(
                    "Enter expiration date [dd/MM/yyyy] (or 0 to cancel): ", null);
            if (expDate == null) {
                System.out.println("  [INFO] Batch import cancelled.");
                printDivider();
                continue;
            }
            if (prodDate.isAfter(expDate)) {
                System.out.println("  [ERROR] Production date cannot be after expiration date.");
                printDivider();
                continue;
            }

            Integer qty = ConsoleUtil.readPositiveInt("Enter batch quantity [>0] (or 0 to cancel): ");
            if (qty == null) {
                System.out.println("  [INFO] Batch import cancelled.");
                printDivider();
                continue;
            }

            System.out.println();
            if (!ConsoleUtil.confirm("Are you sure you want to import this batch?")) {
                System.out.println("  [INFO] Batch import cancelled.");
                printDivider();
                continue;
            }

            try {
                Batch newBatch = this.productController.importBatch(
                        product.getId(), prodDate, expDate, qty);
                System.out.printf("  [OK] Batch %s (%d %s) imported for product %s.%n",
                        newBatch.getBatchId(), qty, product.getUnit(), product.getId());
                renderBatchTable(Collections.singletonList(newBatch));
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
            printDivider();
        }
    }

    private void handleAdjustBatchStock() {
        while (true) {
            String rawBatchId = ConsoleUtil.readString(
                    "Enter Batch ID to adjust stock (or 0 to cancel): ");
            if (rawBatchId == null) {
                break;
            }
            Batch target = findBatchAcrossProducts(rawBatchId);
            if (target == null) {
                System.out.println("  [ERROR] Batch not found with ID: " + rawBatchId);
                printDivider();
                continue;
            }
            renderBatchTable(Collections.singletonList(target));
            System.out.println();

            Integer newQty = ConsoleUtil.readInt("Enter new exact batch quantity [>=0]: ", 0,
                    Integer.MAX_VALUE);
            if (!ConsoleUtil.confirm("Confirm setting batch " + target.getBatchId() + " stock to " + newQty + "?")) {
                System.out.println("  [INFO] Adjustment cancelled.");
                printDivider();
                continue;
            }
            try {
                this.productController.adjustBatchQuantity(target.getProductId(), target.getBatchId(), newQty);
                System.out.printf("  [OK] Batch %s quantity updated to %d.%n", target.getBatchId(), newQty);
                renderBatchTable(Collections.singletonList(target));
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
            printDivider();
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
            printDivider();
            String input = ConsoleUtil.readOptionalString(
                    "Enter search keyword or Product ID (or 0 to cancel): ");
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
        Product directProduct = resolveProductForBatchInspection(input);
        if (directProduct != null) {
            renderProductBatchesTable(directProduct);
            return;
        }
        List<Batch> matching = filterBatchesByKeyword(input);
        renderBatchTable(matching);
    }

    private Product resolveProductForBatchInspection(String input) {
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

        String possibleProdId = InputValidator.tryNormalizeProductId(clean);
        Product product = (possibleProdId != null)
                ? this.productController.findById(possibleProdId)
                : this.productController.findById(clean);
        if (product != null) {
            return product;
        }

        List<Product> matches = this.productController.search(clean);
        if (matches.size() == 1) {
            return matches.get(0);
        }
        return null;
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
            Integer threshold = ConsoleUtil.readInt(
                    "Enter stock threshold [5] (Enter for default, 0 to cancel): ",
                    1, Integer.MAX_VALUE, Product.DEFAULT_LOW_STOCK_THRESHOLD);
            if (threshold == null) {
                break;
            }
            List<Product> lowStock = this.productController.getLowStockProducts(threshold);
            renderLowStockTable(lowStock, threshold);
            printDivider();
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
        System.out.printf("  Total low-stock products listed: %d%n", lowStock.size());
    }

    private void handleViewNearExpiryAlert() {
        while (true) {
            Integer days = ConsoleUtil.readInt(
                    "Enter warning window in days [7] (Enter for default, 0 to cancel): ",
                    1, Integer.MAX_VALUE, Product.DEFAULT_NEAR_EXPIRY_DAYS);
            if (days == null) {
                break;
            }
            List<Batch> nearBatches = this.productController.getNearExpiryBatches(days);
            renderBatchTable(nearBatches);
            printDivider();
        }
    }

    private void handleViewExpiredAlert() {
        while (true) {
            Integer windowDays = ConsoleUtil.readInt(
                    "Enter look-back window in days [N] (Enter for all, 0 to cancel): ",
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
            printDivider();
        }
    }

    /**
     * Renders a dedicated 80-column ASCII table showing all imported batches for a
     * product.
     *
     * @param product the non-null product whose batches are to be displayed
     */
    public void renderProductBatchesTable(Product product) {
        if (product == null) {
            System.out.println("  [INFO] No product selected.");
            return;
        }
        System.out.println(PRODUCT_BATCHES_BORDER);
        System.out.printf("| %-76s |%n",
                TableFormatter.fit("Product : " + product.getId() + " - " + product.getName(), 76));
        System.out.printf("| %-76s |%n",
                TableFormatter.fit("Category: " + product.getCategory(), 76));
        System.out.printf("| %-76s |%n",
                TableFormatter.fit("Unit    : " + product.getUnit(), 76));
        System.out.printf("| %-76s |%n",
                TableFormatter.fit("Price   : " + InputValidator.formatCurrency(product.getPrice()), 76));
        System.out.printf("| %-76s |%n",
                TableFormatter.fit("Stock   : " + product.getAvailableStock() + "/" + product.getTotalStock(), 76));
        System.out.println(PRODUCT_BATCHES_BORDER);
        System.out.println(PRODUCT_BATCHES_HEADER);
        System.out.println(PRODUCT_BATCHES_BORDER);

        List<Batch> batches = product.getBatches();
        if (batches.isEmpty()) {
            System.out.printf("| %-76s |%n",
                    TableFormatter.center("(No inventory batches recorded for this product)", 76));
        } else {
            for (Batch batch : batches) {
                String batchIdCell = TableFormatter.fit(batch.getBatchId(), 6);
                String importDateCell = TableFormatter.fit(InputValidator.formatDisplayDate(batch.getImportDate()), 10);
                String prodDateCell = TableFormatter.fit(InputValidator.formatDisplayDate(batch.getProductionDate()),
                        10);
                String expDateCell = TableFormatter.fit(InputValidator.formatDisplayDate(batch.getExpirationDate()),
                        10);
                String statusCell = TableFormatter.fit(determineBatchStatus(batch), 8);
                String qtyCell = TableFormatter.fitRight(String.valueOf(batch.getQuantity()), 17);

                System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                        batchIdCell, importDateCell, prodDateCell, expDateCell, statusCell, qtyCell);
            }
        }
        System.out.println(PRODUCT_BATCHES_BORDER);
        System.out.printf("| %s | %s |%n",
                TableFormatter.fit("Total Inventory Stock", 56),
                TableFormatter.fitRight(String.valueOf(product.getTotalStock()), 17));
        System.out.println(PRODUCT_BATCHES_TOTAL_BORDER);
        System.out.printf("  Total batches displayed: %d%n", batches.size());
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
