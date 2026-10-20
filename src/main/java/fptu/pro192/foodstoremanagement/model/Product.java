package fptu.pro192.foodstoremanagement.model;

import fptu.pro192.foodstoremanagement.util.InputValidator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a retail food product in the store catalog, composed of zero or
 * more inventory Batch records managed under First-Expired-First-Out rules
 * (BR1, BR3, BR4, BR5, BR10, BR11, BR19, BR24, BR26, BR27).
 */
public class Product {

    /**
     * Default low-stock warning threshold in units (BR19).
     */
    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    /**
     * Default near-expiry warning window in days (BR20).
     */
    public static final int DEFAULT_NEAR_EXPIRY_DAYS = 7;

    private String id;
    private String name;
    private String category;
    private String unit;
    private double price;
    private final List<Batch> batches;
    private boolean isDeleted;

    /**
     * Constructs an active Product with an empty batch list (BR1, BR3, BR4, BR5).
     *
     * @param id       the unique 6-character product identifier (P00001-P99999)
     * @param name     the non-empty product display name
     * @param category the single-word retail category name
     * @param unit     the single-word measurement unit
     * @param price    the positive unit selling price in VND
     * @throws IllegalArgumentException if any parameter violates domain invariants
     */
    public Product(String id, String name, String category, String unit, double price) {
        this(id, name, category, unit, price, false);
    }

    /**
     * Constructs a Product with an explicit soft-delete state (BR1, BR3, BR4, BR5,
     * BR27).
     *
     * @param id        the unique 6-character product identifier (P00001-P99999)
     * @param name      the non-empty product display name
     * @param category  the single-word retail category name
     * @param unit      the single-word measurement unit
     * @param price     the positive unit selling price in VND
     * @param isDeleted true if the product is soft-deleted, false if active
     * @throws IllegalArgumentException if any parameter violates domain invariants
     */
    public Product(String id, String name, String category, String unit, double price, boolean isDeleted) {
        setId(id);
        setName(name);
        setCategory(category);
        setUnit(unit);
        setPrice(price);
        this.batches = new ArrayList<>();
        this.isDeleted = isDeleted;
    }

    /**
     * Attaches an inventory batch to this product after verifying referential
     * integrity and batch ID uniqueness within the product (BR6, BR18).
     *
     * @param batch the Batch instance to attach
     * @throws IllegalArgumentException if batch is null, belongs to another
     *                                  product, or has a duplicate batchId
     */
    public void addBatch(Batch batch) {
        if (batch == null) {
            throw new IllegalArgumentException("Batch cannot be null.");
        }
        if (!this.id.equalsIgnoreCase(batch.getProductId())) {
            throw new IllegalArgumentException(
                    "Batch product ID (" + batch.getProductId()
                            + ") does not match product ID (" + this.id + ").");
        }
        for (Batch existing : this.batches) {
            if (existing.getBatchId().equalsIgnoreCase(batch.getBatchId())) {
                throw new IllegalArgumentException(
                        "Duplicate batch ID '" + batch.getBatchId() + "' on product " + this.id + ".");
            }
        }
        this.batches.add(batch);
    }

    /**
     * Returns an unmodifiable view of all batches associated with this product.
     *
     * @return an unmodifiable list of all Batch instances
     */
    public List<Batch> getBatches() {
        return Collections.unmodifiableList(this.batches);
    }

    /**
     * Returns all non-expired batches with positive stock relative to today,
     * sorted in FEFO order (earliest expirationDate first) (BR11, BR24).
     *
     * @return an unmodifiable list of active, sellable batches sorted by FEFO
     */
    public List<Batch> getActiveBatches() {
        return getActiveBatches(LocalDate.now());
    }

    /**
     * Returns all non-expired batches with positive stock relative to the given
     * reference date, sorted in FEFO order (ascending expirationDate, then
     * importDate, then batchId) (BR11, BR24).
     *
     * @param referenceDate the date used to evaluate batch expiration
     * @return an unmodifiable list of active, sellable batches sorted by FEFO
     * @throws IllegalArgumentException if referenceDate is null
     */
    public List<Batch> getActiveBatches(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("Reference date cannot be null.");
        }
        List<Batch> active = new ArrayList<>();
        for (Batch batch : this.batches) {
            if (batch.getQuantity() > 0 && !batch.isExpired(referenceDate)) {
                active.add(batch);
            }
        }
        Collections.sort(active, new Comparator<Batch>() {
            @Override
            public int compare(Batch b1, Batch b2) {
                int expCompare = b1.getExpirationDate().compareTo(b2.getExpirationDate());
                if (expCompare != 0) {
                    return expCompare;
                }
                int impCompare = b1.getImportDate().compareTo(b2.getImportDate());
                if (impCompare != 0) {
                    return impCompare;
                }
                return b1.getBatchId().compareToIgnoreCase(b2.getBatchId());
            }
        });
        return Collections.unmodifiableList(active);
    }

    /**
     * Returns the raw physical sum of quantities across all batches, including
     * expired batches (BR6, BR26).
     *
     * @return the total physical stock across all batches
     */
    public int getTotalStock() {
        int total = 0;
        for (Batch batch : this.batches) {
            total += batch.getQuantity();
        }
        return total;
    }

    /**
     * Returns the sellable stock quantity across all non-expired batches
     * relative to today (BR10, BR11, BR26).
     *
     * @return the available non-expired stock quantity
     */
    public int getAvailableStock() {
        return getAvailableStock(LocalDate.now());
    }

    /**
     * Returns the sellable stock quantity across all non-expired batches
     * relative to the specified reference date (BR10, BR11, BR26).
     *
     * @param referenceDate the date used to evaluate batch expiration
     * @return the available non-expired stock quantity
     * @throws IllegalArgumentException if referenceDate is null
     */
    public int getAvailableStock(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("Reference date cannot be null.");
        }
        int available = 0;
        for (Batch batch : this.batches) {
            if (batch.getQuantity() > 0 && !batch.isExpired(referenceDate)) {
                available += batch.getQuantity();
            }
        }
        return available;
    }

    /**
     * Checks whether this product's available non-expired stock is at or below
     * the specified threshold relative to today (BR19).
     *
     * @param threshold the non-negative low-stock threshold
     * @return true if available stock is less than or equal to threshold
     */
    public boolean isLowStock(int threshold) {
        return isLowStock(LocalDate.now(), threshold);
    }

    /**
     * Checks whether this product's available non-expired stock is at or below
     * the specified threshold relative to referenceDate (BR19).
     *
     * @param referenceDate the date used to evaluate batch expiration
     * @param threshold     the non-negative low-stock threshold
     * @return true if available stock is less than or equal to threshold
     * @throws IllegalArgumentException if referenceDate is null or threshold is
     *                                  negative
     */
    public boolean isLowStock(LocalDate referenceDate, int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Low-stock threshold cannot be negative.");
        }
        return getAvailableStock(referenceDate) <= threshold;
    }

    /**
     * Returns the unique product identifier.
     *
     * @return the 6-character product ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets and validates the product identifier (BR1).
     * Immutable outside of constructor.
     *
     * @param id the 6-character product ID (P00001-P99999)
     * @throws IllegalArgumentException if id is invalid
     */
    private void setId(String id) {
        this.id = InputValidator.validateProductId(id);
    }

    /**
     * Returns the product display name.
     *
     * @return the product name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets and validates the product display name (BR3).
     *
     * @param name the non-empty product name
     * @throws IllegalArgumentException if name is blank, purely numeric, or unsafe
     */
    public final void setName(String name) {
        this.name = InputValidator.normalizeText(name, "Product name");
    }

    /**
     * Returns the retail grocery category of this product.
     *
     * @return the product category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets and validates the single-word retail category (BR5).
     *
     * @param category the category token
     * @throws IllegalArgumentException if category is blank or contains invalid
     *                                  characters
     */
    public final void setCategory(String category) {
        this.category = InputValidator.normalizeSingleWordToken(category, "Category");
    }

    /**
     * Returns the measurement unit of this product.
     *
     * @return the measurement unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets and validates the single-word measurement unit (BR5).
     *
     * @param unit the measurement unit token
     * @throws IllegalArgumentException if unit is blank or contains invalid
     *                                  characters
     */
    public final void setUnit(String unit) {
        this.unit = InputValidator.normalizeSingleWordToken(unit, "Unit");
    }

    /**
     * Returns the unit selling price in VND.
     *
     * @return the positive selling price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets and validates the unit selling price in VND (BR4).
     *
     * @param price the positive selling price
     * @throws IllegalArgumentException if price is not greater than zero
     */
    public final void setPrice(double price) {
        this.price = InputValidator.validatePositivePrice(price);
    }

    /**
     * Checks whether this product has been soft-deleted (BR27).
     *
     * @return true if soft-deleted, false if active
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Updates the soft-delete flag for this product (BR27).
     *
     * @param isDeleted true to mark as soft-deleted, false to mark active
     */
    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
