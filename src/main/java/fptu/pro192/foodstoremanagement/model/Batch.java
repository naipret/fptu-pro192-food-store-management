package fptu.pro192.foodstoremanagement.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents an inventory shipment batch with distinct production and expiration dates.
 *
 * BR7: Production date <= Expiration date, and Production date <= Current system date. BR11:
 * Expired batches cannot be sold. BR20: Close to expiration warning threshold. BR26: Quantity must
 * be a non-negative integer.
 */
public class Batch {

    private String batchId;
    private String productId;
    private LocalDate importDate;
    private LocalDate productionDate;
    private LocalDate expirationDate;
    private int quantity;

    /**
     * Default constructor for serialization frameworks or bean conventions.
     */
    public Batch() {}

    /**
     * Constructs an inventory batch with strict domain invariants validation.
     *
     * @param batchId Unique batch identifier (e.g., B00001)
     * @param productId Associated product ID foreign key (e.g., P00001)
     * @param importDate Date the batch was imported into warehouse
     * @param productionDate Date the batch was manufactured
     * @param expirationDate Date the batch expires
     * @param quantity Stock quantity available in this batch
     * @throws IllegalArgumentException If any invariant or business rule is violated
     */
    public Batch(String batchId, String productId, LocalDate importDate, LocalDate productionDate,
            LocalDate expirationDate, int quantity) {
        if (batchId == null || batchId.trim().isEmpty()) {
            throw new IllegalArgumentException("Batch ID cannot be null or empty");
        }
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (productionDate == null) {
            throw new IllegalArgumentException("Production date cannot be null");
        }
        if (expirationDate == null) {
            throw new IllegalArgumentException("Expiration date cannot be null");
        }
        // BR7: Production date <= Expiration date
        if (productionDate.isAfter(expirationDate)) {
            throw new IllegalArgumentException("Production date cannot be after expiration date");
        }
        // BR7: Production date <= Current system date
        if (productionDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Production date cannot be in the future relative to system date");
        }
        // BR26: Quantity cannot be negative
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Batch stock quantity cannot be negative: " + quantity);
        }

        this.batchId = batchId.trim();
        this.productId = productId.trim();
        this.importDate = importDate != null ? importDate : LocalDate.now();
        this.productionDate = productionDate;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
    }

    /**
     * BR11: Checks if this batch is expired relative to the specified reference date.
     *
     * @param currentDate Reference date for expiration evaluation
     * @return true if batch is expired relative to currentDate; false otherwise
     */
    public boolean isExpired(LocalDate currentDate) {
        LocalDate refDate = (currentDate != null) ? currentDate : LocalDate.now();
        return refDate.isAfter(this.expirationDate);
    }

    /**
     * Convenience overload evaluating expiration relative to system clock date.
     *
     * @return true if batch is expired today; false otherwise
     */
    public boolean isExpired() {
        return isExpired(LocalDate.now());
    }

    /**
     * BR20: Checks if this batch is within the specified shelf-life warning threshold.
     *
     * @param currentDate Reference date for expiration evaluation
     * @param warningDays Number of warning days before expiry
     * @return true if active (not expired) and remaining days <= warningDays; false otherwise
     */
    public boolean isCloseToExpiry(LocalDate currentDate, int warningDays) {
        if (warningDays < 0) {
            throw new IllegalArgumentException("Warning days cannot be negative: " + warningDays);
        }
        LocalDate refDate = (currentDate != null) ? currentDate : LocalDate.now();
        if (isExpired(refDate)) {
            return false;
        }
        LocalDate warningThreshold = refDate.plusDays(warningDays);
        return !this.expirationDate.isAfter(warningThreshold);
    }

    /**
     * Convenience overload evaluating shelf-life warning relative to system clock date.
     *
     * @param warningDays Number of warning days before expiry
     * @return true if active and remaining days <= warningDays; false otherwise
     */
    public boolean isCloseToExpiry(int warningDays) {
        return isCloseToExpiry(LocalDate.now(), warningDays);
    }

    /**
     * Calculates the number of days remaining until expiration relative to reference date.
     *
     * @param currentDate Reference date
     * @return Number of days remaining (negative if expired)
     */
    public long getDaysRemaining(LocalDate currentDate) {
        LocalDate refDate = (currentDate != null) ? currentDate : LocalDate.now();
        return ChronoUnit.DAYS.between(refDate, this.expirationDate);
    }

    /**
     * Convenience overload calculating remaining days relative to today.
     *
     * @return Number of days remaining until expiry
     */
    public long getDaysRemaining() {
        return getDaysRemaining(LocalDate.now());
    }

    /**
     * BR14, BR24: Deducts stock from this batch.
     *
     * @param amount The integer amount to deduct
     * @throws IllegalArgumentException If amount <= 0 or amount > available quantity
     */
    public void deduct(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deduction amount must be positive: " + amount);
        }
        if (amount > this.quantity) {
            throw new IllegalArgumentException("Insufficient batch stock. Available: "
                    + this.quantity + ", requested: " + amount);
        }
        this.quantity -= amount;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        if (productionDate != null && expirationDate != null
                && productionDate.isAfter(expirationDate)) {
            throw new IllegalArgumentException("Production date cannot be after expiration date");
        }
        this.productionDate = productionDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        if (productionDate != null && expirationDate != null
                && productionDate.isAfter(expirationDate)) {
            throw new IllegalArgumentException("Production date cannot be after expiration date");
        }
        this.expirationDate = expirationDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Batch batch = (Batch) o;
        return Objects.equals(batchId, batch.batchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(batchId);
    }

    @Override
    public String toString() {
        return "Batch{" + "batchId='" + batchId + '\'' + ", productId='" + productId + '\''
                + ", importDate=" + importDate + ", productionDate=" + productionDate
                + ", expirationDate=" + expirationDate + ", quantity=" + quantity + '}';
    }
}
