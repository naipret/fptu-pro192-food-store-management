package fptu.pro192.foodstoremanagement.model;

import fptu.pro192.foodstoremanagement.util.InputValidator;
import java.time.LocalDate;

/**
 * Represents a physical inventory batch of a retail product with import,
 * production, and expiration dates and an integer stock quantity (BR6, BR7,
 * BR11, BR20, BR24, BR26).
 */
public class Batch {

    private String batchId;
    private String productId;
    private LocalDate importDate;
    private LocalDate productionDate;
    private LocalDate expirationDate;
    private int quantity;

    /**
     * Constructs a validated Batch instance (BR6, BR7, BR18, BR26).
     *
     * @param batchId the unique 6-character batch identifier (B00001-B99999)
     * @param productId the 6-character identifier of the parent product (P00001-P99999)
     * @param importDate the date the batch was received into inventory
     * @param productionDate the manufacturing or packing date of the batch
     * @param expirationDate the expiration date of the batch
     * @param quantity the non-negative integer stock quantity
     * @throws IllegalArgumentException if any identifier, date, or quantity is invalid
     */
    public Batch(String batchId, String productId, LocalDate importDate,
            LocalDate productionDate, LocalDate expirationDate, int quantity) {
        setBatchId(batchId);
        setProductId(productId);
        setImportDate(importDate);
        setDates(productionDate, expirationDate);
        setQuantity(quantity);
    }

    /**
     * Checks whether this batch has expired relative to the current system date (BR11).
     *
     * @return true if today is strictly after expirationDate, false otherwise
     */
    public boolean isExpired() {
        return isExpired(LocalDate.now());
    }

    /**
     * Checks whether this batch has expired relative to a given reference date (BR11).
     * A batch expiring on the reference date remains valid through the end of that day.
     *
     * @param referenceDate the date to evaluate against
     * @return true if referenceDate is strictly after expirationDate
     * @throws IllegalArgumentException if referenceDate is null
     */
    public boolean isExpired(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("Reference date cannot be null.");
        }
        return referenceDate.isAfter(this.expirationDate);
    }

    /**
     * Checks whether this batch has positive stock, is not yet expired, and
     * expires within the specified number of days from today (BR20).
     *
     * @param daysThreshold the non-negative warning window in days
     * @return true if the batch expires within daysThreshold days from today
     */
    public boolean isNearExpiry(int daysThreshold) {
        return isNearExpiry(LocalDate.now(), daysThreshold);
    }

    /**
     * Checks whether this batch has positive stock, is not yet expired relative
     * to referenceDate, and expires within daysThreshold days (BR20).
     *
     * @param referenceDate the reference date to evaluate from
     * @param daysThreshold the non-negative warning window in days
     * @return true if the batch is active and expires on or before referenceDate plus daysThreshold
     * @throws IllegalArgumentException if referenceDate is null or daysThreshold is negative
     */
    public boolean isNearExpiry(LocalDate referenceDate, int daysThreshold) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("Reference date cannot be null.");
        }
        if (daysThreshold < 0) {
            throw new IllegalArgumentException("Days threshold cannot be negative.");
        }
        if (this.quantity <= 0 || isExpired(referenceDate)) {
            return false;
        }
        LocalDate warningLimit = referenceDate.plusDays(daysThreshold);
        return !this.expirationDate.isAfter(warningLimit);
    }

    /**
     * Deducts a positive integer amount from this batch's remaining stock (BR6, BR14, BR24, BR26).
     *
     * @param amount the positive integer quantity to deduct
     * @throws IllegalArgumentException if amount is not positive or exceeds remaining quantity
     */
    public void deduct(int amount) {
        InputValidator.validatePositiveQuantity(amount, "Deduction amount");
        if (amount > this.quantity) {
            throw new IllegalArgumentException(
                    "Cannot deduct " + amount + " units from batch " + this.batchId
                    + " (only " + this.quantity + " available).");
        }
        this.quantity -= amount;
    }

    /**
     * Returns the unique batch identifier.
     *
     * @return the 6-character batch ID
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets and validates the batch identifier (BR6).
     *
     * @param batchId the 6-character batch ID (B00001-B99999)
     * @throws IllegalArgumentException if batchId is invalid
     */
    public final void setBatchId(String batchId) {
        this.batchId = InputValidator.validateBatchId(batchId);
    }

    /**
     * Returns the parent product identifier.
     *
     * @return the 6-character product ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets and validates the parent product identifier (BR1, BR18).
     *
     * @param productId the 6-character product ID (P00001-P99999)
     * @throws IllegalArgumentException if productId is invalid
     */
    public final void setProductId(String productId) {
        this.productId = InputValidator.validateProductId(productId);
    }

    /**
     * Returns the date the batch was imported into inventory.
     *
     * @return the import LocalDate
     */
    public LocalDate getImportDate() {
        return importDate;
    }

    /**
     * Sets the batch import date.
     *
     * @param importDate the non-null import LocalDate
     * @throws IllegalArgumentException if importDate is null
     */
    public final void setImportDate(LocalDate importDate) {
        if (importDate == null) {
            throw new IllegalArgumentException("Import date cannot be null.");
        }
        this.importDate = importDate;
    }

    /**
     * Returns the batch production date.
     *
     * @return the production LocalDate
     */
    public LocalDate getProductionDate() {
        return productionDate;
    }

    /**
     * Sets the production date after verifying it is not after the current expiration date (BR7).
     *
     * @param productionDate the non-null production LocalDate
     * @throws IllegalArgumentException if productionDate is null or after expirationDate
     */
    public void setProductionDate(LocalDate productionDate) {
        if (productionDate == null) {
            throw new IllegalArgumentException("Production date cannot be null.");
        }
        if (this.expirationDate != null) {
            InputValidator.validateDateOrder(productionDate, this.expirationDate);
        }
        this.productionDate = productionDate;
    }

    /**
     * Returns the batch expiration date.
     *
     * @return the expiration LocalDate
     */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the expiration date after verifying it is not before the current production date (BR7).
     *
     * @param expirationDate the non-null expiration LocalDate
     * @throws IllegalArgumentException if expirationDate is null or before productionDate
     */
    public void setExpirationDate(LocalDate expirationDate) {
        if (expirationDate == null) {
            throw new IllegalArgumentException("Expiration date cannot be null.");
        }
        if (this.productionDate != null) {
            InputValidator.validateDateOrder(this.productionDate, expirationDate);
        }
        this.expirationDate = expirationDate;
    }

    /**
     * Atomically validates and updates both productionDate and expirationDate (BR7).
     *
     * @param productionDate the batch production date
     * @param expirationDate the batch expiration date
     * @throws IllegalArgumentException if either date is null or productionDate is after expirationDate
     */
    public final void setDates(LocalDate productionDate, LocalDate expirationDate) {
        InputValidator.validateDateOrder(productionDate, expirationDate);
        this.productionDate = productionDate;
        this.expirationDate = expirationDate;
    }

    /**
     * Returns the remaining integer stock quantity in this batch.
     *
     * @return the non-negative stock quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the remaining integer stock quantity for this batch (BR6, BR26).
     *
     * @param quantity the non-negative integer quantity
     * @throws IllegalArgumentException if quantity is negative
     */
    public final void setQuantity(int quantity) {
        this.quantity = InputValidator.validateNonNegativeQuantity(quantity, "Batch quantity");
    }
}
