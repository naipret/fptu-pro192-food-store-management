package fptu.pro192.foodstoremanagement.controller;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Product;
import fptu.pro192.foodstoremanagement.util.CsvUtil;
import fptu.pro192.foodstoremanagement.util.InputValidator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Controller managing the retail product catalog, standardized category and
 * measurement unit registries, batch inventory, FEFO stock deduction, and
 * atomic CSV persistence (BR1, BR3, BR4, BR5, BR6, BR7, BR10, BR11, BR14,
 * BR18, BR19, BR20, BR24, BR26, BR27).
 */
public class ProductController {

    /**
     * Default file path for the products CSV storage.
     */
    public static final String DEFAULT_PRODUCTS_CSV = "data/products.csv";

    /**
     * Default file path for the batches CSV storage.
     */
    public static final String DEFAULT_BATCHES_CSV = "data/batches.csv";

    private static final String[] PRODUCT_CSV_HEADERS = {
            "id", "name", "category", "unit", "price", "isDeleted"
    };

    private static final String[] BATCH_CSV_HEADERS = {
            "batchId", "productId", "importDate", "productionDate", "expirationDate", "quantity"
    };

    private final Path productCsvPath;
    private final Path batchCsvPath;
    private final List<Product> products;
    private final Set<String> categories;
    private final Set<String> units;

    /**
     * Constructs a ProductController bound to the default CSV paths in data/.
     */
    public ProductController() {
        this(Paths.get(DEFAULT_PRODUCTS_CSV), Paths.get(DEFAULT_BATCHES_CSV));
    }

    /**
     * Constructs a ProductController bound to the specified Path objects and
     * immediately loads existing product and batch records from disk.
     *
     * @param productCsvPath the Path for products.csv
     * @param batchCsvPath   the Path for batches.csv
     * @throws IllegalArgumentException if either Path is null
     */
    public ProductController(Path productCsvPath, Path batchCsvPath) {
        if (productCsvPath == null) {
            throw new IllegalArgumentException("Product CSV path cannot be null.");
        }
        if (batchCsvPath == null) {
            throw new IllegalArgumentException("Batch CSV path cannot be null.");
        }
        this.productCsvPath = productCsvPath;
        this.batchCsvPath = batchCsvPath;
        this.products = new ArrayList<>();
        this.categories = new LinkedHashSet<>(InputValidator.DEFAULT_CATEGORIES);
        this.units = new LinkedHashSet<>(InputValidator.DEFAULT_UNITS);
        loadFromCsv();
    }

    /**
     * Loads all products and batches from their respective CSV files, skipping
     * header rows at line 0 and linking batches to their parent products in O(N)
     * time via a Map (BR1, BR5, BR6, BR7, BR18, BR27).
     */
    public final void loadFromCsv() {
        this.products.clear();
        this.categories.clear();
        this.categories.addAll(InputValidator.DEFAULT_CATEGORIES);
        this.units.clear();
        this.units.addAll(InputValidator.DEFAULT_UNITS);

        List<String[]> productRows = CsvUtil.readCsv(this.productCsvPath);
        List<String[]> batchRows = CsvUtil.readCsv(this.batchCsvPath);
        if (productRows.isEmpty()) {
            CsvUtil.writeAtomic(this.productCsvPath, Collections.singletonList(PRODUCT_CSV_HEADERS));
        }
        if (batchRows.isEmpty()) {
            CsvUtil.writeAtomic(this.batchCsvPath, Collections.singletonList(BATCH_CSV_HEADERS));
        }

        Map<String, Product> productMap = new LinkedHashMap<>();

        for (int i = 0; i < productRows.size(); i++) {
            String[] row = productRows.get(i);
            if (row.length == 0 || (row.length == 1 && row[0].trim().isEmpty())) {
                continue;
            }
            if (i == 0 && "id".equalsIgnoreCase(row[0].trim())) {
                continue;
            }
            if (row.length < 6) {
                throw new IllegalStateException(
                        "Corrupted product CSV row at index " + i + ": expected 6 columns.");
            }
            try {
                String id = InputValidator.validateProductId(row[0]);
                String name = InputValidator.normalizeText(row[1], "Product name");
                String category = registerCategory(row[2]);
                String unit = registerUnit(row[3]);
                double price = Double.parseDouble(row[4].trim());
                boolean isDeleted = Boolean.parseBoolean(row[5].trim());

                Product product = new Product(id, name, category, unit, price, isDeleted);
                this.products.add(product);
                productMap.put(id.toUpperCase(Locale.ROOT), product);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Corrupted record in product CSV file at row " + i + ": " + e.getMessage(), e);
            }
        }

        batchRows = CsvUtil.readCsv(this.batchCsvPath);
        for (int i = 0; i < batchRows.size(); i++) {
            String[] row = batchRows.get(i);
            if (row.length == 0 || (row.length == 1 && row[0].trim().isEmpty())) {
                continue;
            }
            if (i == 0 && "batchId".equalsIgnoreCase(row[0].trim())) {
                continue;
            }
            if (row.length < 6) {
                throw new IllegalStateException(
                        "Corrupted batch CSV row at index " + i + ": expected 6 columns.");
            }
            try {
                String batchId = InputValidator.validateBatchId(row[0]);
                String productId = InputValidator.validateProductId(row[1]);
                LocalDate importDate = LocalDate.parse(row[2].trim());
                LocalDate productionDate = LocalDate.parse(row[3].trim());
                LocalDate expirationDate = LocalDate.parse(row[4].trim());
                int quantity = Integer.parseInt(row[5].trim());

                Batch batch = new Batch(batchId, productId, importDate,
                        productionDate, expirationDate, quantity);
                Product parent = productMap.get(productId.toUpperCase(Locale.ROOT));
                if (parent != null) {
                    parent.addBatch(batch);
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Corrupted record in batch CSV file at row " + i + ": " + e.getMessage(), e);
            }
        }
    }

    /**
     * Atomically persists all products (including soft-deleted records) and
     * all attached batches to products.csv and batches.csv with uniform header
     * rows at line 0 (BR14, BR27).
     */
    public void saveToCsv() {
        List<String[]> productRows = new ArrayList<>();
        List<String[]> batchRows = new ArrayList<>();
        productRows.add(PRODUCT_CSV_HEADERS);
        batchRows.add(BATCH_CSV_HEADERS);

        for (Product product : this.products) {
            productRows.add(new String[] {
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getUnit(),
                    String.valueOf(product.getPrice()),
                    String.valueOf(product.isDeleted())
            });
            for (Batch batch : product.getBatches()) {
                batchRows.add(new String[] {
                        batch.getBatchId(),
                        batch.getProductId(),
                        batch.getImportDate().toString(),
                        batch.getProductionDate().toString(),
                        batch.getExpirationDate().toString(),
                        String.valueOf(batch.getQuantity())
                });
            }
        }

        CsvUtil.writeAtomic(this.productCsvPath, productRows);
        CsvUtil.writeAtomic(this.batchCsvPath, batchRows);
    }

    /**
     * Returns an unmodifiable view of the registered product categories (BR5).
     *
     * @return an unmodifiable Set of category names in insertion order
     */
    public Set<String> getCategories() {
        return Collections.unmodifiableSet(this.categories);
    }

    /**
     * Returns an unmodifiable view of the registered measurement units (BR5).
     *
     * @return an unmodifiable Set of measurement unit names in insertion order
     */
    public Set<String> getUnits() {
        return Collections.unmodifiableSet(this.units);
    }

    /**
     * Validates and registers a retail category in the controller's Set registry,
     * preserving existing canonical casing when a case-insensitive match exists
     * (BR5).
     *
     * @param rawCategory the raw category name
     * @return the canonical registered category name
     * @throws IllegalArgumentException if rawCategory is blank or not a single-word
     *                                  token
     */
    public final String registerCategory(String rawCategory) {
        String normalized = InputValidator.normalizeSingleWordToken(rawCategory, "Category");
        for (String existing : this.categories) {
            if (existing.equalsIgnoreCase(normalized)) {
                return existing;
            }
        }
        this.categories.add(normalized);
        return normalized;
    }

    /**
     * Validates and registers a measurement unit in the controller's Set registry,
     * preserving existing canonical casing when a case-insensitive match exists
     * (BR5).
     *
     * @param rawUnit the raw measurement unit name
     * @return the canonical registered unit name
     * @throws IllegalArgumentException if rawUnit is blank or not a single-word
     *                                  token
     */
    public final String registerUnit(String rawUnit) {
        String normalized = InputValidator.normalizeSingleWordToken(rawUnit, "Unit");
        for (String existing : this.units) {
            if (existing.equalsIgnoreCase(normalized)) {
                return existing;
            }
        }
        this.units.add(normalized);
        return normalized;
    }

    /**
     * Generates the next available 6-character Product ID (P00001-P99999),
     * checking both active and soft-deleted records so deleted IDs are never
     * reused (BR1, BR27).
     *
     * @return the next canonical Product ID
     */
    public String generateNextProductId() {
        int maxNumber = 0;
        for (Product product : this.products) {
            String id = product.getId();
            if (id != null && id.length() == 6 && (id.charAt(0) == 'P' || id.charAt(0) == 'p')) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (maxNumber >= 99999) {
            throw new IllegalStateException("Maximum Product ID limit (P99999) reached.");
        }
        return String.format(Locale.US, "P%05d", maxNumber + 1);
    }

    /**
     * Generates the next available 6-character Batch ID (B00001-B99999) across
     * all products in the catalog (BR6).
     *
     * @return the next canonical Batch ID
     */
    public String generateNextBatchId() {
        int maxNumber = 0;
        for (Product product : this.products) {
            for (Batch batch : product.getBatches()) {
                String id = batch.getBatchId();
                if (id != null && id.length() == 6 && (id.charAt(0) == 'B' || id.charAt(0) == 'b')) {
                    try {
                        int num = Integer.parseInt(id.substring(1));
                        if (num > maxNumber) {
                            maxNumber = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        if (maxNumber >= 99999) {
            throw new IllegalStateException("Maximum Batch ID limit (B99999) reached.");
        }
        return String.format(Locale.US, "B%05d", maxNumber + 1);
    }

    /**
     * Checks whether a Product ID already exists in the catalog, including
     * soft-deleted products (BR1, BR27).
     *
     * @param id the product ID to check
     * @return true if the ID is already used by any active or soft-deleted product
     */
    public boolean existsById(String id) {
        return findByIdIncludeDeleted(id) != null;
    }

    /**
     * Checks whether a Batch ID already exists across any product in the catalog
     * (BR6).
     *
     * @param batchId the batch ID to check
     * @return true if the batch ID is already in use
     */
    public boolean existsBatchId(String batchId) {
        if (batchId == null || batchId.trim().isEmpty()) {
            return false;
        }
        String target;
        try {
            target = InputValidator.normalizeBatchId(batchId);
        } catch (IllegalArgumentException e) {
            target = batchId.trim();
        }
        for (Product product : this.products) {
            for (Batch batch : product.getBatches()) {
                if (batch.getBatchId().equalsIgnoreCase(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Finds an active (non-deleted) Product by its identifier (BR1, BR27).
     *
     * @param id the product ID to look up (supports shorthand like "P1" or
     *           "P00001")
     * @return the matching active Product, or null if not found or soft-deleted
     */
    public Product findById(String id) {
        Product product = findByIdIncludeDeleted(id);
        if (product != null && !product.isDeleted()) {
            return product;
        }
        return null;
    }

    /**
     * Finds a Product by its identifier across both active and soft-deleted
     * records so historical orders and reports can resolve product names (BR18,
     * BR27).
     *
     * @param id the product ID to look up
     * @return the matching Product, or null if not found
     */
    public Product findByIdIncludeDeleted(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        String lookupId;
        try {
            lookupId = InputValidator.normalizeProductId(id);
        } catch (IllegalArgumentException e) {
            lookupId = id.trim();
        }
        for (Product product : this.products) {
            if (product.getId().equalsIgnoreCase(lookupId)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Returns an unmodifiable list of all active (non-deleted) products (BR27).
     *
     * @return an unmodifiable list of active Product objects
     */
    public List<Product> getAllProducts() {
        List<Product> active = new ArrayList<>();
        for (Product product : this.products) {
            if (!product.isDeleted()) {
                active.add(product);
            }
        }
        return Collections.unmodifiableList(active);
    }

    /**
     * Adds a new Product without an initial batch and persists to CSV (BR1, BR3,
     * BR4, BR5, BR27).
     *
     * @param product the new Product to register
     * @throws IllegalArgumentException if product is null, its ID already exists,
     *                                  or any attached batch ID already exists
     */
    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (existsById(product.getId())) {
            throw new IllegalArgumentException(
                    "Product ID '" + product.getId() + "' already exists in the system.");
        }
        for (Batch attached : product.getBatches()) {
            if (existsBatchId(attached.getBatchId())) {
                throw new IllegalArgumentException(
                        "Batch ID '" + attached.getBatchId() + "' already exists in the system.");
            }
        }
        product.setCategory(registerCategory(product.getCategory()));
        product.setUnit(registerUnit(product.getUnit()));
        this.products.add(product);
        saveToCsv();
    }

    /**
     * Updates the catalog metadata of an existing active Product without
     * corrupting batch quantities, then persists to CSV (BR3, BR4, BR5).
     * Passing null or blank for any string parameter (or null for newPrice)
     * preserves the existing attribute value.
     *
     * @param id          the product ID to update
     * @param newName     the new product name, or null/blank to keep existing
     * @param newCategory the new category, or null/blank to keep existing
     * @param newUnit     the new unit, or null/blank to keep existing
     * @param newPrice    the new positive selling price, or null to keep existing
     * @throws IllegalArgumentException if the product does not exist or any new
     *                                  value is invalid
     */
    public void updateProduct(String id, String newName, String newCategory,
            String newUnit, Double newPrice) {
        Product product = findById(id);
        if (product == null) {
            throw new IllegalArgumentException("Active product not found with ID: " + id);
        }
        String validatedName = (newName != null && !newName.trim().isEmpty())
                ? InputValidator.normalizeText(newName, "Product name")
                : product.getName();
        String validatedCategory = (newCategory != null && !newCategory.trim().isEmpty())
                ? registerCategory(newCategory)
                : product.getCategory();
        String validatedUnit = (newUnit != null && !newUnit.trim().isEmpty())
                ? registerUnit(newUnit)
                : product.getUnit();
        double validatedPrice = (newPrice != null)
                ? InputValidator.validatePositivePrice(newPrice)
                : product.getPrice();

        product.setName(validatedName);
        product.setCategory(validatedCategory);
        product.setUnit(validatedUnit);
        product.setPrice(validatedPrice);
        saveToCsv();
    }

    /**
     * Soft-deletes an active Product by setting its isDeleted flag to true and
     * persisting the updated catalog to CSV (BR27).
     *
     * @param id the product ID to soft-delete
     * @return true if the product was found and soft-deleted, false otherwise
     */
    public boolean deleteProduct(String id) {
        Product product = findById(id);
        if (product == null) {
            return false;
        }
        product.setDeleted(true);
        saveToCsv();
        return true;
    }

    /**
     * Attaches a new inventory Batch to an existing active Product after
     * verifying positive quantity and global batchId uniqueness (BR6, BR7, BR18,
     * BR26).
     *
     * @param productId the target product ID
     * @param batch     the new Batch to attach
     * @throws IllegalArgumentException if product is not found, batch is null,
     *                                  quantity is not positive, or batchId is
     *                                  duplicate
     */
    public void addBatchToProduct(String productId, Batch batch) {
        Product product = findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Active product not found with ID: " + productId);
        }
        if (batch == null) {
            throw new IllegalArgumentException("Batch cannot be null.");
        }
        InputValidator.validatePositiveQuantity(batch.getQuantity(), "New batch quantity");
        if (existsBatchId(batch.getBatchId())) {
            throw new IllegalArgumentException(
                    "Batch ID '" + batch.getBatchId() + "' already exists in the system.");
        }
        product.addBatch(batch);
        saveToCsv();
    }

    /**
     * Replenishes an existing Batch by adding a positive integer quantity to its
     * current stock and persisting to CSV (BR6, BR26).
     *
     * @param productId     the parent product ID
     * @param batchId       the target batch ID
     * @param addedQuantity the positive integer quantity to add
     * @throws IllegalArgumentException if product or batch is not found or
     *                                  addedQuantity is not positive
     */
    public void replenishBatch(String productId, String batchId, int addedQuantity) {
        InputValidator.validatePositiveQuantity(addedQuantity, "Replenishment quantity");
        Product product = findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Active product not found with ID: " + productId);
        }
        String normalizedBatchId = InputValidator.normalizeBatchId(batchId);
        for (Batch batch : product.getBatches()) {
            if (batch.getBatchId().equalsIgnoreCase(normalizedBatchId)) {
                batch.setQuantity(batch.getQuantity() + addedQuantity);
                saveToCsv();
                return;
            }
        }
        throw new IllegalArgumentException(
                "Batch '" + normalizedBatchId + "' not found on product " + product.getId() + ".");
    }

    /**
     * Adjusts an existing Batch's stock to an explicit non-negative integer
     * quantity and persists to CSV (BR6, BR26).
     *
     * @param productId   the parent product ID
     * @param batchId     the target batch ID
     * @param newQuantity the non-negative integer quantity to set
     * @throws IllegalArgumentException if product or batch is not found or
     *                                  newQuantity is negative
     */
    public void adjustBatchQuantity(String productId, String batchId, int newQuantity) {
        InputValidator.validateNonNegativeQuantity(newQuantity, "Adjusted batch quantity");
        Product product = findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Active product not found with ID: " + productId);
        }
        String normalizedBatchId = InputValidator.normalizeBatchId(batchId);
        for (Batch batch : product.getBatches()) {
            if (batch.getBatchId().equalsIgnoreCase(normalizedBatchId)) {
                batch.setQuantity(newQuantity);
                saveToCsv();
                return;
            }
        }
        throw new IllegalArgumentException(
                "Batch '" + normalizedBatchId + "' not found on product " + product.getId() + ".");
    }

    /**
     * Deducts the requested quantity from a product's non-expired batches
     * relative to today using the First-Expired-First-Out algorithm and
     * persists the updated batch quantities atomically (BR10, BR11, BR14, BR24,
     * BR26).
     *
     * @param productId         the product ID to deduct from
     * @param requestedQuantity the positive integer quantity to deduct
     * @throws IllegalArgumentException   if product is not found or
     *                                    requestedQuantity is not positive
     * @throws InsufficientStockException if available non-expired stock is less
     *                                    than requestedQuantity
     */
    public void deductStockFEFO(String productId, int requestedQuantity)
            throws InsufficientStockException {
        InputValidator.validatePositiveQuantity(requestedQuantity, "Requested deduction quantity");
        Product product = findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Active product not found with ID: " + productId);
        }

        int availableStock = product.getAvailableStock();
        if (availableStock < requestedQuantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product '" + product.getName() + "' ("
                            + product.getId() + "). Requested: " + requestedQuantity
                            + ", Available non-expired stock: " + availableStock + ".");
        }

        List<Batch> activeBatches = product.getActiveBatches();
        int remaining = requestedQuantity;
        for (Batch batch : activeBatches) {
            if (remaining == 0) {
                break;
            }
            int deductFromBatch = Math.min(batch.getQuantity(), remaining);
            batch.deduct(deductFromBatch);
            remaining -= deductFromBatch;
        }
        saveToCsv();
    }

    /**
     * Searches active products by matching the keyword against product ID,
     * product name, category, or measurement unit (case-insensitive substring
     * match).
     * Returns all active products if keyword is null or blank.
     *
     * @param keyword the search keyword
     * @return an unmodifiable list of matching active products
     */
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        String lower = keyword.trim().toLowerCase(Locale.ROOT);
        List<Product> matches = new ArrayList<>();
        for (Product product : this.products) {
            if (product.isDeleted()) {
                continue;
            }
            boolean matchId = product.getId().toLowerCase(Locale.ROOT).contains(lower);
            boolean matchName = product.getName().toLowerCase(Locale.ROOT).contains(lower);
            boolean matchCat = product.getCategory().toLowerCase(Locale.ROOT).contains(lower);
            boolean matchUnit = product.getUnit().toLowerCase(Locale.ROOT).contains(lower);
            if (matchId || matchName || matchCat || matchUnit) {
                matches.add(product);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    /**
     * Returns all active products whose available non-expired stock is at or
     * below the default threshold of 5 units relative to today (BR19).
     *
     * @return an unmodifiable list of low-stock active products
     */
    public List<Product> getLowStockProducts() {
        return getLowStockProducts(Product.DEFAULT_LOW_STOCK_THRESHOLD);
    }

    /**
     * Returns all active products whose available non-expired stock is at or
     * below the specified threshold relative to today (BR19).
     *
     * @param threshold the non-negative stock threshold
     * @return an unmodifiable list of low-stock active products
     * @throws IllegalArgumentException if threshold is negative
     */
    public List<Product> getLowStockProducts(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Low-stock threshold cannot be negative.");
        }
        List<Product> result = new ArrayList<>();
        for (Product product : this.products) {
            if (!product.isDeleted() && product.isLowStock(threshold)) {
                result.add(product);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns all batches with positive stock across active products that have
     * expired relative to today, sorted by expiration date ascending (BR11).
     *
     * @return an unmodifiable list of expired batches
     */
    public List<Batch> getExpiredBatches() {
        List<Batch> expired = new ArrayList<>();
        for (Product product : this.products) {
            if (product.isDeleted()) {
                continue;
            }
            for (Batch batch : product.getBatches()) {
                if (batch.getQuantity() > 0 && batch.isExpired()) {
                    expired.add(batch);
                }
            }
        }
        sortBatchesByExpiry(expired);
        return Collections.unmodifiableList(expired);
    }

    /**
     * Returns all non-expired batches with positive stock across active products
     * that expire within the default 7-day warning window from today (BR20).
     *
     * @return an unmodifiable list of near-expiry batches
     */
    public List<Batch> getNearExpiryBatches() {
        return getNearExpiryBatches(Product.DEFAULT_NEAR_EXPIRY_DAYS);
    }

    /**
     * Returns all non-expired batches with positive stock across active products
     * that expire within daysThreshold days from today (BR20).
     *
     * @param daysThreshold the non-negative warning window in days
     * @return an unmodifiable list of near-expiry batches
     * @throws IllegalArgumentException if daysThreshold is negative
     */
    public List<Batch> getNearExpiryBatches(int daysThreshold) {
        if (daysThreshold < 0) {
            throw new IllegalArgumentException("Days threshold cannot be negative.");
        }
        List<Batch> nearExpiry = new ArrayList<>();
        for (Product product : this.products) {
            if (product.isDeleted()) {
                continue;
            }
            for (Batch batch : product.getBatches()) {
                if (batch.isNearExpiry(daysThreshold)) {
                    nearExpiry.add(batch);
                }
            }
        }
        sortBatchesByExpiry(nearExpiry);
        return Collections.unmodifiableList(nearExpiry);
    }

    private void sortBatchesByExpiry(List<Batch> batchList) {
        Collections.sort(batchList, new Comparator<Batch>() {
            @Override
            public int compare(Batch b1, Batch b2) {
                int exp = b1.getExpirationDate().compareTo(b2.getExpirationDate());
                if (exp != 0) {
                    return exp;
                }
                return b1.getBatchId().compareToIgnoreCase(b2.getBatchId());
            }
        });
    }
}
