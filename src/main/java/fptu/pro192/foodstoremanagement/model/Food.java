package fptu.pro192.foodstoremanagement.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Abstract domain model representing a food product item in catalog inventory.
 *
 * BR1: Product ID format P00001-P99999 (immutable).
 * BR3: Name and category cannot be empty or blank.
 * BR4: Unit cannot be empty or blank.
 * BR5: Price must be greater than zero.
 * BR5.1: Name must contain alphabetic characters.
 * BR6: Stock quantity cannot be negative.
 * BR27: Soft delete semantics (isDeleted = true).
 */
public abstract class Food {

    protected String id;
    protected String name;
    protected String category;
    protected String unit;
    protected double price;
    protected double minTemperature;
    protected double maxTemperature;
    protected double minHumidity;
    protected double maxHumidity;
    protected boolean isDeleted;
    protected List<Batch> batches = new ArrayList<>();

    /**
     * Default constructor for serialization or subclass instantiation.
     */
    public Food() {
    }

    /**
     * Full domain constructor enforcing all business rules and invariants.
     *
     * @param id Unique 6-character product ID (BR1)
     * @param name Descriptive food name (BR3, BR5.1)
     * @param category Food classification category (BR3)
     * @param unit Measurement unit (BR4)
     * @param price Unit price in VND (BR5)
     * @param minTemperature Lower HACCP temperature bound in Celsius
     * @param maxTemperature Upper HACCP temperature bound in Celsius
     * @param minHumidity Lower HACCP humidity percentage bound
     * @param maxHumidity Upper HACCP humidity percentage bound
     * @param isDeleted Soft-delete status flag (BR27)
     * @throws IllegalArgumentException If any invariant is violated
     */
    public Food(String id, String name, String category, String unit, double price,
            double minTemperature, double maxTemperature, double minHumidity,
            double maxHumidity, boolean isDeleted) {
        validateInvariants(id, name, category, unit, price, minTemperature, maxTemperature, minHumidity, maxHumidity);
        this.id = id.trim();
        this.name = name.trim();
        this.category = category.trim();
        this.unit = unit.trim();
        this.price = price;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        this.isDeleted = isDeleted;
    }

    /**
     * Convenience constructor with default environmental bounds.
     *
     * @param id Product ID
     * @param name Product name
     * @param category Product category
     * @param unit Measurement unit
     * @param price Unit price
     */
    public Food(String id, String name, String category, String unit, double price) {
        this(id, name, category, unit, price, 0.0, 0.0, 0.0, 0.0, false);
    }

    /**
     * Validates domain constraints against Business Rules BR1, BR3, BR4, BR5, BR5.1.
     */
    private static void validateInvariants(String id, String name, String category, String unit,
            double price, double minTemp, double maxTemp, double minHum, double maxHum) {
        // BR1: Unique Product ID format P00001-P99999
        if (id == null || !id.trim().matches("^P\\d{5}$")) {
            throw new IllegalArgumentException(
                    "Invalid product ID: " + id + ". Product ID must follow format P00001-P99999 (BR1).");
        }
        // BR3 & BR5.1: Name not empty and must contain letters
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty or blank (BR3).");
        }
        if (!name.matches(".*[a-zA-ZÀ-ỹ].*")) {
            throw new IllegalArgumentException(
                    "Product name must contain alphabetic characters; cannot be purely numeric (BR5.1).");
        }
        // BR3: Category not empty
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Product category cannot be empty or blank (BR3).");
        }
        // BR4: Unit not empty
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Product unit cannot be empty or blank (BR4).");
        }
        // BR5: Price > 0
        if (price <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero (BR5). Got: " + price);
        }
        // Environmental bounds invariant
        if (minTemp > maxTemp) {
            throw new IllegalArgumentException(
                    "Minimum temperature (" + minTemp + ") cannot exceed maximum temperature (" + maxTemp + ")");
        }
        if (minHum > maxHum) {
            throw new IllegalArgumentException(
                    "Minimum humidity (" + minHum + ") cannot exceed maximum humidity (" + maxHum + ")");
        }
    }

    /**
     * Returns polymorphic human-readable HACCP storage instruction guidelines.
     *
     * @return Formatted storage instruction description
     */
    public abstract String getStorageInstructions();

    /**
     * Returns polymorphic shelf-life expiration warning threshold in days.
     *
     * @return Number of warning days before batch expiration
     */
    public abstract int getDaysBeforeExpiryWarning();

    /**
     * BR6, BR11: Calculates total active inventory stock across all non-expired batches relative to reference date.
     *
     * @param today Reference date for expiry evaluation
     * @return Total active stock quantity
     */
    public int getTotalStock(LocalDate today) {
        LocalDate refDate = (today != null) ? today : LocalDate.now();
        int sum = 0;
        for (Batch batch : this.batches) {
            if (!batch.isExpired(refDate)) {
                sum += batch.getQuantity();
            }
        }
        return sum;
    }

    /**
     * Convenience overload calculating total active stock relative to today.
     *
     * @return Total active stock quantity
     */
    public int getTotalStock() {
        return getTotalStock(LocalDate.now());
    }

    /**
     * BR24: Returns all active (non-expired) batches sorted by expiration date ascending (FEFO order).
     *
     * @param today Reference date for expiry evaluation
     * @return Unmodifiable or copy list of sorted active batches
     */
    public List<Batch> getActiveBatches(LocalDate today) {
        LocalDate refDate = (today != null) ? today : LocalDate.now();
        List<Batch> activeBatches = new ArrayList<>();
        for (Batch batch : this.batches) {
            if (!batch.isExpired(refDate) && batch.getQuantity() > 0) {
                activeBatches.add(batch);
            }
        }
        activeBatches.sort(Comparator.comparing(Batch::getExpirationDate));
        return activeBatches;
    }

    /**
     * Convenience overload returning FEFO sorted active batches relative to today.
     *
     * @return List of sorted active batches
     */
    public List<Batch> getActiveBatches() {
        return getActiveBatches(LocalDate.now());
    }

    /**
     * Appends a new inventory batch to this product.
     *
     * @param batch The batch to attach
     */
    public void addBatch(Batch batch) {
        if (batch != null) {
            this.batches.add(batch);
        }
    }

    /**
     * Returns an unmodifiable view of internal batches.
     *
     * @return Unmodifiable list of batches
     */
    public List<Batch> getBatches() {
        return Collections.unmodifiableList(this.batches);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero: " + price);
        }
        this.price = price;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public double getMinHumidity() {
        return minHumidity;
    }

    public void setMinHumidity(double minHumidity) {
        this.minHumidity = minHumidity;
    }

    public double getMaxHumidity() {
        return maxHumidity;
    }

    public void setMaxHumidity(double maxHumidity) {
        this.maxHumidity = maxHumidity;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return Objects.equals(id, food.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Food{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", unit='" + unit + '\'' +
                ", price=" + price +
                ", isDeleted=" + isDeleted +
                ", totalStock=" + getTotalStock() +
                '}';
    }
}
