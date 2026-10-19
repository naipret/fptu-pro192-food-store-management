package fptu.pro192.foodstoremanagement.controller;

/**
 * Checked domain exception thrown when a requested order quantity exceeds a
 * product's available non-expired stock during cart addition or FEFO deduction
 * (BR10, BR11, BR24).
 */
public class InsufficientStockException extends Exception {

    /**
     * Constructs an InsufficientStockException with a descriptive message.
     *
     * @param message the detail message explaining the stock shortage
     */
    public InsufficientStockException(String message) {
        super(message);
    }

    /**
     * Constructs an InsufficientStockException with a message and root cause.
     *
     * @param message the detail message explaining the stock shortage
     * @param cause   the underlying cause
     */
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
