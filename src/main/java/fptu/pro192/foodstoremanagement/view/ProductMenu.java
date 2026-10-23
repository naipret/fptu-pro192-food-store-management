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
 * by name, category, or ID (BR1, BR3, BR4, BR5, BR6, BR7, BR27, BR31, BR33).
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

    @Override
    protected boolean shouldPauseAfterAction(int choice) {
        return choice != 2;
    }

    private void handleViewAllProducts() {
        List<Product> products = this.productController.getAllProducts();
        renderProductTable(products);
    }

    private void handleSearchProducts() {
        while (true) {
            String keyword = ConsoleUtil.readOptionalString("Enter search keyword (or 0 to cancel): ");
            if ("0".equals(keyword)) {
                break;
            }
            List<Product> matches = this.productController.search(keyword);
            renderProductTable(matches);
        }
    }

    private void handleAddProduct() {
        String name = ConsoleUtil.readValidatedText("Enter product name (or 0 to cancel): ", "Product name");
        if (name == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        String category = promptCategorySelection(null);
        if (category == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        String unit = promptUnitSelection(null);
        if (unit == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Double price = ConsoleUtil.readPositivePrice("Enter selling price (> 0, or 0 to cancel): ");
        if (price == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        if (!ConsoleUtil.confirm("Are you sure you want to add this product?")) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        try {
            Product product = this.productController.addProduct(name, category, unit, price);
            System.out.printf("  [OK] Product %s ('%s') added successfully.%n",
                    product.getId(), product.getName());
            renderProductTable(Collections.singletonList(product));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleUpdateProduct() {
        String rawId = ConsoleUtil.readCancellableString("Enter Product ID to update (or 0 to cancel): ");
        if (rawId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Product existing = this.productController.findById(rawId);
        if (existing == null) {
            System.out.println("  [ERROR] Active product not found with ID: " + rawId);
            return;
        }

        System.out.println("  Current Product Details:");
        renderProductTable(Collections.singletonList(existing));
        System.out.println("  (Press Enter to keep current value, or 0 to cancel)");

        String newName = ConsoleUtil.readOptionalValidatedText(
                "Enter new product name [" + existing.getName() + "]: ",
                "Product name", existing.getName());
        if (newName == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        System.out.println("  Current Category: " + existing.getCategory());
        String newCategory = promptCategorySelection(existing.getCategory());
        if (newCategory == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        System.out.println("  Current Unit: " + existing.getUnit());
        String newUnit = promptUnitSelection(existing.getUnit());
        if (newUnit == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Double newPrice = ConsoleUtil.readOptionalPositivePrice(
                "Enter new selling price [" + InputValidator.formatCurrency(existing.getPrice()) + "]: ",
                existing.getPrice());
        if (newPrice == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        if (!ConsoleUtil.confirm("Are you sure you want to update this product?")) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        try {
            this.productController.updateProduct(
                    existing.getId(), newName, newCategory, newUnit, newPrice);
            Product updated = this.productController.findById(existing.getId());
            System.out.printf("  [OK] Product %s updated successfully.%n", existing.getId());
            renderProductTable(Collections.singletonList(updated != null ? updated : existing));
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void handleDeleteProduct() {
        String rawId = ConsoleUtil.readCancellableString("Enter Product ID to remove (or 0 to cancel): ");
        if (rawId == null) {
            System.out.println("  [INFO] Operation cancelled.");
            return;
        }

        Product existing = this.productController.findById(rawId);
        if (existing == null) {
            System.out.println("  [ERROR] Active product not found with ID: " + rawId);
            return;
        }

        System.out.println("  Product to delete:");
        renderProductTable(Collections.singletonList(existing));

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

    private String promptCategorySelection(String currentCategory) {
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
        Integer selected;
        if (currentCategory == null) {
            selected = ConsoleUtil.readInt(
                    "Select category [1-" + customOption + "] (or 0 to cancel): ", 1, customOption);
        } else {
            selected = ConsoleUtil.readIntWithDefault(
                    "Select new category [1-" + customOption + "] (Enter to keep, or 0 to cancel): ",
                    1, customOption, -1);
        }
        if (selected == null) {
            return null;
        }
        if (selected == -1) {
            return currentCategory;
        }
        if (selected <= list.size()) {
            return list.get(selected - 1);
        }
        while (true) {
            String custom = ConsoleUtil.readCancellableString(
                    "Enter new single-word category name (letters only, or 0 to cancel): ");
            if (custom == null) {
                return null;
            }
            try {
                return this.productController.registerCategory(custom);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }

    private String promptUnitSelection(String currentUnit) {
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
        Integer selected;
        if (currentUnit == null) {
            selected = ConsoleUtil.readInt(
                    "Select measurement unit [1-" + customOption + "] (or 0 to cancel): ", 1, customOption);
        } else {
            selected = ConsoleUtil.readIntWithDefault(
                    "Select new measurement unit [1-" + customOption + "] (Enter to keep, or 0 to cancel): ",
                    1, customOption, -1);
        }
        if (selected == null) {
            return null;
        }
        if (selected == -1) {
            return currentUnit;
        }
        if (selected <= list.size()) {
            return list.get(selected - 1);
        }
        while (true) {
            String custom = ConsoleUtil.readCancellableString(
                    "Enter new single-word unit name (letters only, or 0 to cancel): ");
            if (custom == null) {
                return null;
            }
            try {
                return this.productController.registerUnit(custom);
            } catch (IllegalArgumentException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            }
        }
    }
}
