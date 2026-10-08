package fptu.pro192.foodstoremanagement.repository;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.model.ProductFactory;
import fptu.pro192.foodstoremanagement.util.MiniCsv;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lightweight repository managing in-memory product collections and coordinating
 * atomic two-phase CSV persistence for products and inventory batches.
 *
 * BR1: Unique Product ID lookup.
 * BR27: Soft-delete semantics (isDeleted = true) preserving physical audit trails.
 */
public class ProductRepository {

    private final String productFilePath;
    private final String batchFilePath;
    private final List<Food> products = new ArrayList<>();

    /**
     * Constructs a repository with custom CSV file paths (supports isolated unit testing).
     *
     * @param productFilePath Path to the products CSV file
     * @param batchFilePath Path to the batches CSV file
     */
    public ProductRepository(String productFilePath, String batchFilePath) {
        if (productFilePath == null || productFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Product file path cannot be null or empty");
        }
        if (batchFilePath == null || batchFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Batch file path cannot be null or empty");
        }
        this.productFilePath = productFilePath;
        this.batchFilePath = batchFilePath;
        loadFromCsv();
    }

    /**
     * Default constructor pointing to production data paths.
     */
    public ProductRepository() {
        this("data/products.csv", "data/batches.csv");
    }

    /**
     * Loads catalog products and inventory batches from CSV storage into memory.
     * Synchronized to prevent state corruption during concurrent reloads.
     */
    public synchronized void loadFromCsv() {
        products.clear();

        // 1. Read catalog products from products.csv
        List<String[]> productsData = MiniCsv.read(productFilePath);
        for (String[] row : productsData) {
            if (row == null || row.length < 10) {
                continue;
            }
            try {
                String id = row[0];
                String name = row[1];
                String category = row[2];
                String unit = row[3];
                double price = Double.parseDouble(row[4]);
                double minTemperature = Double.parseDouble(row[5]);
                double maxTemperature = Double.parseDouble(row[6]);
                double minHumidity = Double.parseDouble(row[7]);
                double maxHumidity = Double.parseDouble(row[8]);
                boolean isDeleted = Boolean.parseBoolean(row[9]);

                Food food = ProductFactory.create(category, id, name, category, unit, price,
                        minTemperature, maxTemperature, minHumidity, maxHumidity, isDeleted);
                products.add(food);
            } catch (Exception e) {
                throw new RuntimeException("Corrupted record in product CSV file: " + String.join(",", row), e);
            }
        }

        // 2. Read inventory batches from batches.csv and associate with parent products
        List<String[]> batchesData = MiniCsv.read(batchFilePath);
        for (String[] row : batchesData) {
            if (row == null || row.length < 6) {
                continue;
            }
            try {
                String batchId = row[0];
                String productId = row[1];
                LocalDate importDate = LocalDate.parse(row[2]);
                int quantity = Integer.parseInt(row[3]);
                LocalDate productionDate = LocalDate.parse(row[4]);
                LocalDate expirationDate = LocalDate.parse(row[5]);

                Batch batch = new Batch(batchId, productId, importDate, productionDate, expirationDate, quantity);

                // Attach batch to corresponding food item
                for (Food product : products) {
                    if (product.getId().equalsIgnoreCase(productId)) {
                        product.addBatch(batch);
                        break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Corrupted record in batch CSV file: " + String.join(",", row), e);
            }
        }
    }

    /**
     * Atomically serializes in-memory products and batches to flat CSV files.
     */
    public synchronized void saveToCsv() {
        List<String[]> productsData = new ArrayList<>();
        List<String[]> batchesData = new ArrayList<>();

        for (Food product : products) {
            String[] productRow = {
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getUnit(),
                    String.valueOf(product.getPrice()),
                    String.valueOf(product.getMinTemperature()),
                    String.valueOf(product.getMaxTemperature()),
                    String.valueOf(product.getMinHumidity()),
                    String.valueOf(product.getMaxHumidity()),
                    String.valueOf(product.isDeleted())
            };
            productsData.add(productRow);

            for (Batch batch : product.getBatches()) {
                String[] batchRow = {
                        batch.getBatchId(),
                        product.getId(),
                        batch.getImportDate().toString(),
                        String.valueOf(batch.getQuantity()),
                        batch.getProductionDate().toString(),
                        batch.getExpirationDate().toString()
                };
                batchesData.add(batchRow);
            }
        }

        MiniCsv.writeAtomic(productFilePath, productsData);
        MiniCsv.writeAtomic(batchFilePath, batchesData);
    }

    /**
     * BR1: Retrieves an active (non-deleted) product by unique ID.
     *
     * @param id The 6-character product ID (case-insensitive)
     * @return Food instance if found and not deleted; null otherwise
     */
    public synchronized Food findById(String id) {
        if (id == null) {
            return null;
        }
        for (Food product : products) {
            if (product.getId().equalsIgnoreCase(id.trim()) && !product.isDeleted()) {
                return product;
            }
        }
        return null;
    }

    /**
     * Returns an unmodifiable list of all active (non-deleted) products.
     *
     * @return List of active Food items
     */
    public synchronized List<Food> findAll() {
        List<Food> active = new ArrayList<>();
        for (Food food : products) {
            if (!food.isDeleted()) {
                active.add(food);
            }
        }
        return Collections.unmodifiableList(active);
    }

    /**
     * Saves or updates a food item in repository and persists state atomically to disk.
     *
     * @param food The food entity to persist
     */
    public synchronized void save(Food food) {
        if (food == null) {
            throw new IllegalArgumentException("Food entity cannot be null");
        }
        boolean exists = false;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equalsIgnoreCase(food.getId())) {
                products.set(i, food);
                exists = true;
                break;
            }
        }
        if (!exists) {
            products.add(food);
        }
        saveToCsv();
    }

    /**
     * BR27: Soft-deletes a product by marking isDeleted = true and persisting state.
     *
     * @param id The product ID to delete
     * @return true if product was found and marked deleted; false otherwise
     */
    public synchronized boolean delete(String id) {
        if (id == null) {
            return false;
        }
        for (Food product : products) {
            if (product.getId().equalsIgnoreCase(id.trim())) {
                product.setDeleted(true);
                saveToCsv();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns total count of all products (including soft-deleted).
     *
     * @return Total in-memory product count
     */
    public synchronized int count() {
        return products.size();
    }
}
