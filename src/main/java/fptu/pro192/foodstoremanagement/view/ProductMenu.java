package fptu.pro192.foodstoremanagement.view;

import fptu.pro192.foodstoremanagement.controller.ProductController;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import fptu.pro192.foodstoremanagement.util.TableFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interactive 80-column console menu for managing the retail product catalog:
 * adding products with standardized Set categories and units, updating catalog
 * metadata, soft-deleting products, viewing catalog listings, and searching
 * by name, category, or expiration date (BR1, BR3, BR4, BR5, BR6, BR7, BR27).
 */
public class ProductMenu extends Menu {

    private static final String[] MENU_OPTIONS = {
            "View All Products",
            "Search Products",
            "Add New Product",
            "Update Product Information",
            "Remove Product"
    };

    private static final String PRODUCT_TABLE_BORDER = "+--------+------------------------+--------------+--------+------------+-------+";
    private static final String PRODUCT_TABLE_HEADER = "| ID     | Product Name           | Category     | Unit   | Price      | Stock |";

    private final ProductController productController;

    /**
     * Constructs a ProductMenu bound to the shared ProductController.
     *
     * @param productController the non-null ProductController collaborator
     * @throws IllegalArgumentException if productController is null
     */
    public ProductMenu(ProductController productController) {
        super("FOOD STORE MANAGEMENT SYSTEM > [1] PRODUCTS", MENU_OPTIONS);
        if (productController == null) {
            throw new IllegalArgumentException("ProductController cannot be null.");
        }
        this.productController = productController;
    }

    @Override
    protected void handleChoice(int choice) {
        switch (choice) {
            case 1:
                handleViewAllProducts();
                break;
            case 2:
                handleSearchProducts();
                break;
            case 3:
                handleAddProduct();
                break;
            case 4:
                handleUpdateProduct();
                break;
            case 5:
                handleDeleteProduct();
                break;
            default:
                System.out.println("  [ERROR] Unsupported menu option.");
                break;
        }
    }

    private void handleViewAllProducts() {
        List<Product> products = this.productController.getAllProducts();
        renderProductTable(products);
    }

    private void handleSearchProducts() {
        while (true) {
            String keyword = ConsoleUtil
                    .readOptionalString("Enter search keyword (0 to cancel): ");
            if ("0".equals(keyword.trim())) {
                break;
            }
            List<Product> matches = this.productController.search(keyword);
            renderProductTable(matches);
        }
    }

    private void handleAddProduct() {
        String suggestedId = this.productController.generateNextProductId();
        String productId = promptProductId(suggestedId);
        if (productId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        String name = promptValidatedText("Enter product name: ", "Product name");
        String category = promptCategorySelection();
        String unit = promptUnitSelection();
        double price = ConsoleUtil.readDouble("Enter unit selling price (> 0): ", 0.0, true);

        Product product = new Product(productId, name, category, unit, price);

        try {
            this.productController.addProduct(product);
            System.out.printf("  [OK] Product %s ('%s') added successfully.%n",
                    product.getId(), product.getName());
            renderProductTable(Collections.singletonList(product));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleUpdateProduct() {
        String rawId = ConsoleUtil.readNonEmptyString("Enter Product ID to update (e.g., P00001, or 0 to cancel): ");
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
        System.out.printf("  Current Category: %s | Unit: %s | Price: %s | Available Stock: %d%n",
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
            Product updated = this.productController.findById(existing.getId());
            renderProductTable(Collections.singletonList(updated != null ? updated : existing));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleDeleteProduct() {
        String rawId = ConsoleUtil.readNonEmptyString("Enter Product ID to remove (e.g., P00001, or 0 to cancel): ");
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
        if (!ConsoleUtil.confirm("Confirm removing product " + existing.getId() + "?")) {
            System.out.println("  [INFO] Removal cancelled.");
            return;
        }

        if (this.productController.deleteProduct(existing.getId())) {
            System.out.printf("  [OK] Product %s has been removed successfully.%n", existing.getId());
        } else {
            System.out.println("  [ERROR] Could not remove product.");
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
                    String.valueOf(product.getAvailableStock()), 5);
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
}
