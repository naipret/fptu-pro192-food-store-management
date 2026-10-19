package fptu.pro192.foodstoremanagement.view;

import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import fptu.pro192.foodstoremanagement.util.TableFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive 80-column console menu for managing the retail product catalog:
 * adding products with standardized Set categories and units, updating catalog
 * metadata, soft-deleting products, viewing catalog listings, and searching
 * by name, category, or expiration date (BR1, BR3, BR4, BR5, BR6, BR7, BR27).
 */
public class ProductMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "Add New Product",
            "Update Product Catalog Information",
            "Delete Product (Soft Delete)",
            "View All Active Products",
            "View Available In-Stock Products",
            "Search Products (by Name, Category, or Expiry Date)"
    };

    private static final String PRODUCT_TABLE_BORDER = "+--------+------------------------+--------------+--------+------------+------+";
    private static final String PRODUCT_TABLE_HEADER = "| ID     | Product Name           | Category     | Unit   | Price(VND) |Stock |";

    private final ProductController productController;

    /**
     * Constructs a ProductMenu bound to the shared ProductController.
     *
     * @param productController the non-null ProductController collaborator
     * @throws IllegalArgumentException if productController is null
     */
    public ProductMenu(ProductController productController) {
        super("PRODUCT CATALOG MANAGEMENT", MENU_OPTIONS);
        if (productController == null) {
            throw new IllegalArgumentException("ProductController cannot be null.");
        }
        this.productController = productController;
    }

    @Override
    protected void handleChoice(int choice) {
        switch (choice) {
            case 1:
                handleAddProduct();
                break;
            case 2:
                handleUpdateProduct();
                break;
            case 3:
                handleDeleteProduct();
                break;
            case 4:
                handleViewAllProducts();
                break;
            case 5:
                handleViewAvailableProducts();
                break;
            case 6:
                handleSearchProducts();
                break;
            default:
                System.out.println("  [ERROR] Unsupported menu option.");
                break;
        }
    }

    private void handleAddProduct() {
        printSectionHeader("ADD NEW PRODUCT (Enter 0 as ID to Cancel)");
        String suggestedId = this.productController.generateNextProductId();
        String productId = promptProductId(suggestedId);
        if (productId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        String name = promptValidatedText("Enter product name: ", "Product name");
        String category = promptCategorySelection();
        String unit = promptUnitSelection();
        double price = ConsoleUtil.readDouble("Enter unit selling price (VND, > 0): ", 0.0, true);

        Product product = new Product(productId, name, category, unit, price);
        Batch initialBatch = null;
        if (ConsoleUtil.confirm("Do you want to import an initial inventory batch now?")) {
            initialBatch = promptNewBatchForProduct(productId);
        }

        try {
            this.productController.addProduct(product, initialBatch);
            System.out.printf("  [OK] Product %s ('%s') added successfully.%n",
                    product.getId(), product.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleUpdateProduct() {
        printSectionHeader("UPDATE PRODUCT CATALOG INFORMATION (Enter 0 to Cancel)");
        String rawId = ConsoleUtil.readNonEmptyString("Enter Product ID to update (e.g., P00001): ");
        if ("0".equals(rawId)) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Product existing;
        try {
            existing = this.productController.findById(rawId);
        } catch (IllegalArgumentException e) {
            existing = null;
        }
        if (existing == null) {
            System.out.println("  [ERROR] Active product not found with ID: " + rawId);
            return;
        }

        System.out.printf("  Current Product : %s - %s%n", existing.getId(), existing.getName());
        System.out.printf("  Current Category: %s | Unit: %s | Price: %s VND | Available Stock: %d%n",
                existing.getCategory(), existing.getUnit(),
                InputValidator.formatCurrency(existing.getPrice()),
                existing.getAvailableStock());
        System.out.println("  (Leave input blank and press Enter to keep current value)");

        String newName = promptOptionalText("Enter new product name [" + existing.getName() + "]: ",
                "Product name");
        String newCategory = null;
        if (ConsoleUtil.confirm("Do you want to change the category (" + existing.getCategory() + ")?")) {
            newCategory = promptCategorySelection();
        }
        String newUnit = null;
        if (ConsoleUtil.confirm("Do you want to change the unit (" + existing.getUnit() + ")?")) {
            newUnit = promptUnitSelection();
        }
        Double newPrice = promptOptionalPositivePrice(
                "Enter new selling price [" + InputValidator.formatCurrency(existing.getPrice()) + "]: ");

        try {
            this.productController.updateProduct(
                    existing.getId(), newName, newCategory, newUnit, newPrice);
            System.out.printf("  [OK] Product %s updated successfully.%n", existing.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleDeleteProduct() {
        printSectionHeader("DELETE PRODUCT (Enter 0 to Cancel)");
        String rawId = ConsoleUtil.readNonEmptyString("Enter Product ID to delete (e.g., P00001): ");
        if ("0".equals(rawId)) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Product existing = this.productController.findById(rawId);
        if (existing == null) {
            System.out.println("  [ERROR] Active product not found with ID: " + rawId);
            return;
        }

        System.out.printf("  Selected Product: %s - %s (Available Stock: %d %s)%n",
                existing.getId(), existing.getName(),
                existing.getAvailableStock(), existing.getUnit());
        if (!ConsoleUtil.confirm("Confirm soft-deleting product " + existing.getId() + "?")) {
            System.out.println("  [INFO] Deletion cancelled.");
            return;
        }

        if (this.productController.deleteProduct(existing.getId())) {
            System.out.printf("  [OK] Product %s has been soft-deleted.%n", existing.getId());
        } else {
            System.out.println("  [ERROR] Could not delete product.");
        }
    }

    private void handleViewAllProducts() {
        printSectionHeader("ALL ACTIVE PRODUCTS IN CATALOG");
        List<Product> products = this.productController.getAllProducts();
        renderProductTable(products);
    }

    private void handleViewAvailableProducts() {
        printSectionHeader("AVAILABLE IN-STOCK PRODUCTS (NON-EXPIRED STOCK > 0)");
        List<Product> available = this.productController.getAvailableProducts();
        renderProductTable(available);
    }

    private void handleSearchProducts() {
        printSectionHeader("SEARCH PRODUCTS");
        System.out.println("  [1] Search by Product Name or Keyword");
        System.out.println("  [2] Search by Category");
        System.out.println("  [3] Search by Batch Expiration Date (Expiring On or Before Date)");
        System.out.println("  [0] Cancel");
        int mode = ConsoleUtil.readInt("Select search mode [0-3]: ", 0, 3);
        if (mode == 0) {
            return;
        }

        if (mode == 1) {
            String keyword = ConsoleUtil.readOptionalString("Enter product name or keyword: ");
            List<Product> matches = this.productController.search(keyword);
            renderProductTable(matches);
        } else if (mode == 2) {
            String category = promptCategoryFilter();
            List<Product> matches = this.productController.searchByCategory(category);
            renderProductTable(matches);
        } else if (mode == 3) {
            LocalDate targetDate = ConsoleUtil.readDate(
                    "Enter target expiration date (dd/MM/yyyy): ");
            List<Product> matches = this.productController.searchByExpirationDate(targetDate);
            renderProductTable(matches);
        }
    }

    /**
     * Renders a formatted 80-column ASCII table of products showing available
     * non-expired sellable stock.
     *
     * @param products the list of products to display
     */
    public void renderProductTable(List<Product> products) {
        if (products == null || products.isEmpty()) {
            System.out.println("  [INFO] No matching products found.");
            return;
        }
        System.out.println(PRODUCT_TABLE_BORDER);
        System.out.println(PRODUCT_TABLE_HEADER);
        System.out.println(PRODUCT_TABLE_BORDER);
        for (Product product : products) {
            String idCell = TableFormatter.fit(product.getId(), 6);
            String nameCell = TableFormatter.fit(product.getName(), 22);
            String catCell = TableFormatter.fit(product.getCategory(), 12);
            String unitCell = TableFormatter.fit(product.getUnit(), 6);
            String priceCell = TableFormatter.fitRight(
                    InputValidator.formatCurrency(product.getPrice()), 10);
            String stockCell = TableFormatter.fitRight(
                    String.valueOf(product.getAvailableStock()), 4);
            System.out.printf("| %s | %s | %s | %s | %s | %s |%n",
                    idCell, nameCell, catCell, unitCell, priceCell, stockCell);
        }
        System.out.println(PRODUCT_TABLE_BORDER);
        System.out.printf("  Total products listed: %d%n", products.size());
    }

    private String promptProductId(String suggestedId) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(
                    "Enter Product ID [Press Enter for " + suggestedId + ", or 0 to cancel]: ");
            if ("0".equals(raw)) {
                return null;
            }
            if (raw.isEmpty()) {
                return suggestedId;
            }
            try {
                String normalized = InputValidator.normalizeProductId(raw);
                if (this.productController.existsById(normalized)) {
                    System.out.println("  [ERROR] Product ID '" + normalized
                            + "' already exists (including soft-deleted records).");
                    continue;
                }
                return normalized;
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private String promptValidatedText(String prompt, String fieldName) {
        while (true) {
            String raw = ConsoleUtil.readNonEmptyString(prompt);
            try {
                return InputValidator.normalizeText(raw, fieldName);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private String promptOptionalText(String prompt, String fieldName) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(prompt);
            if (raw.isEmpty()) {
                return null;
            }
            try {
                return InputValidator.normalizeText(raw, fieldName);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private Double promptOptionalPositivePrice(String prompt) {
        while (true) {
            String raw = ConsoleUtil.readOptionalString(prompt);
            if (raw.isEmpty()) {
                return null;
            }
            try {
                double val = Double.parseDouble(raw);
                return InputValidator.validatePositivePrice(val);
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Invalid numeric price. Please try again.");
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private String promptCategorySelection() {
        List<String> list = new ArrayList<>(this.productController.getCategories());
        System.out.println("  Available Categories:");
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("    [%2d] %-14s", i + 1, list.get(i));
            if ((i + 1) % 3 == 0 || i == list.size() - 1) {
                System.out.println();
            }
        }
        int customOption = list.size() + 1;
        System.out.printf("    [%2d] Add New Custom Category%n", customOption);
        int selected = ConsoleUtil.readInt(
                "Select category [1-" + customOption + "]: ", 1, customOption);
        if (selected <= list.size()) {
            return list.get(selected - 1);
        }
        while (true) {
            String custom = ConsoleUtil.readNonEmptyString(
                    "Enter new single-word category name (letters only, no spaces/slashes): ");
            try {
                return this.productController.registerCategory(custom);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private String promptCategoryFilter() {
        List<String> list = new ArrayList<>(this.productController.getCategories());
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("    [%2d] %-14s", i + 1, list.get(i));
            if ((i + 1) % 3 == 0 || i == list.size() - 1) {
                System.out.println();
            }
        }
        int selected = ConsoleUtil.readInt("Select category [1-" + list.size() + "]: ", 1, list.size());
        return list.get(selected - 1);
    }

    private String promptUnitSelection() {
        List<String> list = new ArrayList<>(this.productController.getUnits());
        System.out.println("  Available Measurement Units:");
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("    [%2d] %-14s", i + 1, list.get(i));
            if ((i + 1) % 3 == 0 || i == list.size() - 1) {
                System.out.println();
            }
        }
        int customOption = list.size() + 1;
        System.out.printf("    [%2d] Add New Custom Unit%n", customOption);
        int selected = ConsoleUtil.readInt(
                "Select measurement unit [1-" + customOption + "]: ", 1, customOption);
        if (selected <= list.size()) {
            return list.get(selected - 1);
        }
        while (true) {
            String custom = ConsoleUtil.readNonEmptyString(
                    "Enter new single-word unit name (letters only, no spaces/slashes): ");
            try {
                return this.productController.registerUnit(custom);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private Batch promptNewBatchForProduct(String productId) {
        String suggestedBatchId = this.productController.generateNextBatchId();
        String batchId;
        while (true) {
            String raw = ConsoleUtil.readOptionalString(
                    "Enter Batch ID [Press Enter for " + suggestedBatchId + "]: ");
            if (raw.isEmpty()) {
                batchId = suggestedBatchId;
                break;
            }
            try {
                String normalized = InputValidator.normalizeBatchId(raw);
                if (this.productController.existsBatchId(normalized)) {
                    System.out.println("  [ERROR] Batch ID '" + normalized + "' already exists.");
                    continue;
                }
                batchId = normalized;
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }

        int quantity = ConsoleUtil.readPositiveInt("Enter initial batch quantity (> 0): ", false);
        while (true) {
            LocalDate productionDate = ConsoleUtil.readDate("Enter production date (dd/MM/yyyy): ");
            LocalDate expirationDate = ConsoleUtil.readDate("Enter expiration date (dd/MM/yyyy): ");
            try {
                InputValidator.validateDateOrder(productionDate, expirationDate);
                return new Batch(batchId, productId, LocalDate.now(),
                        productionDate, expirationDate, quantity);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    /**
     * Returns the shared ProductController collaborator.
     *
     * @return the ProductController instance
     */
    public ProductController getProductController() {
        return productController;
    }
}
