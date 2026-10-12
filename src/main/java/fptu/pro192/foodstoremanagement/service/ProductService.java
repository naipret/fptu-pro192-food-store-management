package fptu.pro192.foodstoremanagement.service;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Core business service orchestrating inventory management and catalog operations.
 */
public class ProductService {

    private final ProductRepository productRepo;

    /**
     * Constructs ProductService with default repository instance.
     */
    public ProductService() {
        this(new ProductRepository());
    }

    /**
     * Constructs ProductService with injected ProductRepository (Dependency Injection).
     *
     * @param productRepo The repository managing product persistence
     */
    public ProductService(ProductRepository productRepo) {
        if (productRepo == null) {
            throw new IllegalArgumentException("ProductRepository cannot be null");
        }
        this.productRepo = productRepo;
    }

    /**
     * Adds a new product to the catalog with its initial inventory batch.
     *
     * @param product The food product to register
     * @param initialBatch The initial inventory batch (can be null if registered without stock)
     * @param today System date for validation and shelf-life tracking
     */
    public void addProduct(Food product, Batch initialBatch, LocalDate today) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (productRepo.findById(product.getId()) != null) {
            throw new IllegalArgumentException("Product with ID " + product.getId() + " already exists.");
        }

        if (initialBatch != null) {
            product.addBatch(initialBatch);
        }

        productRepo.save(product);
    }

    /**
     * Deducts stock from inventory batches using the First Expired, First Out (FEFO) algorithm.
     * <p>
     * Batches expiring earlier are deducted first. Depleted batches are reduced to 0, and the remaining requested
     * quantity cascades to subsequent earliest expiring batches.
     *
     * @param productId The 6-character product ID (P00001 - P99999)
     * @param quantity The integer quantity to deduct (must be > 0)
     * @param today Reference system date for evaluating batch expiration
     * @throws InsufficientStockException If requested quantity exceeds active stock
     * @throws IllegalArgumentException If product does not exist or is deleted, or quantity <= 0
     */
    public synchronized void deductStockFEFO(String productId, int quantity, LocalDate today) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Deduction quantity must be greater than zero. Got: " + quantity);
        }

        LocalDate refDate = (today != null) ? today : LocalDate.now();
        Food product = productRepo.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found or marked deleted: " + productId);
        }

        int availableStock = product.getTotalStock(refDate);
        if (availableStock < quantity) {
            throw new InsufficientStockException(
                    "Insufficient active stock for product " + product.getName() + " (" + productId + "). "
                            + "Requested: " + quantity + ", available active stock: " + availableStock + ".");
        }

        List<Batch> activeBatches = product.getActiveBatches(refDate);
        int remainingNeeded = quantity;

        for (Batch batch : activeBatches) {
            if (remainingNeeded <= 0) {
                break;
            }

            int batchQty = batch.getQuantity();
            if (batchQty >= remainingNeeded) {
                batch.deduct(remainingNeeded);
                remainingNeeded = 0;
            } else {
                remainingNeeded -= batchQty;
                batch.deduct(batchQty);
            }
        }

        // Stock quantity is reduced immediately and persisted atomically
        productRepo.saveToCsv();
    }

    /**
     * Convenience overload for {@link #deductStockFEFO(String, int, LocalDate)} using current system clock date.
     *
     * @param productId The product ID
     * @param quantity The integer quantity to deduct
     */
    public synchronized void deductStockFEFO(String productId, int quantity) {
        deductStockFEFO(productId, quantity, LocalDate.now());
    }

    /**
     * Returns all active products whose total active inventory stock is <= 5 units (low stock threshold).
     *
     * @return List of products with low stock
     */
    public List<Food> getLowStockProducts() {
        LocalDate today = LocalDate.now();
        List<Food> lowStockProducts = new ArrayList<>();
        for (Food product : productRepo.findAll()) {
            if (product.getTotalStock(today) <= 5) {
                lowStockProducts.add(product);
            }
        }
        return lowStockProducts;
    }

    /**
     * Returns all batches across active products that have expired relative to reference date.
     *
     * @param today Reference system date
     * @return List of expired batches
     */
    public List<Batch> getExpiredBatches(LocalDate today) {
        LocalDate refDate = (today != null) ? today : LocalDate.now();
        List<Batch> expiredBatches = new ArrayList<>();
        for (Food product : productRepo.findAll()) {
            for (Batch batch : product.getBatches()) {
                if (batch.isExpired(refDate)) {
                    expiredBatches.add(batch);
                }
            }
        }
        return expiredBatches;
    }

    /**
     * Returns all active (non-expired) batches with remaining stock that expire within 7 days of reference date.
     *
     * @param today Reference system date
     * @return List of near-expiry batches
     */
    public List<Batch> getNearExpiryBatches(LocalDate today) {
        LocalDate refDate = (today != null) ? today : LocalDate.now();
        List<Batch> nearExpiryBatches = new ArrayList<>();
        for (Food product : productRepo.findAll()) {
            for (Batch batch : product.getBatches()) {
                if (!batch.isExpired(refDate) && batch.getQuantity() > 0 && batch.isCloseToExpiry(refDate, 7)) {
                    nearExpiryBatches.add(batch);
                }
            }
        }
        return nearExpiryBatches;
    }

    /**
     * Task B2: Updates product price and/or stock quantity selectively.
     *
     * @param id Product ID to update
     * @param newPrice Optional new price (null or <= 0 to skip)
     * @param newQuantity Optional quantity adjustment or stock override (null to skip)
     */
    public synchronized void updateProduct(String id, Double newPrice, Integer newQuantity) {
        Food product = productRepo.findById(id);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + id);
        }

        if (newPrice != null && newPrice > 0) {
            product.setPrice(newPrice);
        }

        if (newQuantity != null && newQuantity >= 0) {
            List<Batch> batches = product.getBatches();
            if (!batches.isEmpty()) {
                // Adjust quantity on latest active batch
                batches.get(batches.size() - 1).setQuantity(newQuantity);
            } else {
                // Create default batch if no batch exists
                String batchId = "B" + product.getId().substring(1);
                LocalDate today = LocalDate.now();
                Batch newBatch = new Batch(batchId, product.getId(), today, today, today.plusMonths(6), newQuantity);
                product.addBatch(newBatch);
            }
        }

        productRepo.saveToCsv();
    }

    /**
     * Task B14: Replenishes stock quantity for an existing batch or adds stock to a product.
     *
     * @param productId The product ID
     * @param batchId The batch ID to replenish
     * @param addQty Quantity to add (must be > 0)
     */
    public synchronized void replenishStock(String productId, String batchId, int addQty) {
        if (addQty <= 0) {
            throw new IllegalArgumentException("Replenish quantity must be greater than zero: " + addQty);
        }
        Food product = productRepo.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        boolean found = false;
        for (Batch batch : product.getBatches()) {
            if (batch.getBatchId().equalsIgnoreCase(batchId)) {
                batch.setQuantity(batch.getQuantity() + addQty);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Batch not found with ID: " + batchId + " for product: " + productId);
        }

        productRepo.saveToCsv();
    }

    /**
     * Attaches a newly received batch to an existing product and persists state.
     *
     * @param productId The product ID
     * @param newBatch The new batch to attach
     */
    public synchronized void addBatchToProduct(String productId, Batch newBatch) {
        if (newBatch == null) {
            throw new IllegalArgumentException("Batch cannot be null.");
        }
        Food product = productRepo.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        product.addBatch(newBatch);
        productRepo.saveToCsv();
    }

    /**
     * Task B4: Searches active products by keyword matching ID, name, or category (case-insensitive).
     *
     * @param keyword Search term
     * @return List of matching products
     */
    public List<Food> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepo.findAll();
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        List<Food> results = new ArrayList<>();
        for (Food product : productRepo.findAll()) {
            if (product.getId().toLowerCase().contains(lowerKeyword)
                    || product.getName().toLowerCase().contains(lowerKeyword)
                    || product.getCategory().toLowerCase().contains(lowerKeyword)) {
                results.add(product);
            }
        }
        return results;
    }

    /**
     * Returns an unmodifiable list of all active (non-deleted) products in catalog.
     *
     * @return List of active Food items
     */
    public List<Food> getAllProducts() {
        return productRepo.findAll();
    }

    /**
     * Retrieves active product by unique ID.
     *
     * @param id The product ID
     * @return Product if found; null otherwise
     */
    public Food findById(String id) {
        return productRepo.findById(id);
    }

    /**
     * Soft-deletes a product by ID, preserving physical data integrity.
     *
     * @param id The product ID
     * @return true if deleted successfully; false otherwise
     */
    public synchronized boolean deleteProduct(String id) {
        return productRepo.delete(id);
    }

    /**
     * Returns underlying ProductRepository instance.
     *
     * @return ProductRepository
     */
    public ProductRepository getProductRepo() {
        return productRepo;
    }
}
