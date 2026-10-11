package fptu.pro192.foodstoremanagement.service;

/**
 * Domain exception thrown when inventory stock is insufficient to satisfy an order or deduction request.
 * <p>
 * Enforces Business Rule BR10: The quantity sold cannot exceed the available active stock of the product.
 */
public class InsufficientStockException extends RuntimeException {

    /**
     * Constructs a new InsufficientStockException with the specified detail message.
     *
     * @param message Descriptive error message
     */
    public InsufficientStockException(String message) {
        super(message);
    }

    /**
     * Constructs a new InsufficientStockException with message and cause.
     *
     * @param message Descriptive error message
     * @param cause Root cause exception
     */
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
