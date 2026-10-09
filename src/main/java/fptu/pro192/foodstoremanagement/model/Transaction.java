package fptu.pro192.foodstoremanagement.model;

import java.time.LocalDate;

/**
 * Domain model recording completed financial payments linked directly to an Order.
 *
 * BR23: Total revenue is calculated exclusively from completed sales transactions.
 */
public class Transaction {

    private String transactionId;
    private String orderId;
    private String customerId;
    private LocalDate transactionDate;
    private double totalPaid;
    private String paymentMethod;

    /**
     * Default constructor initializing transaction with current date and CASH payment method.
     */
    public Transaction() {
        this.transactionDate = LocalDate.now();
        this.paymentMethod = "CASH";
    }

    /**
     * Full constructor initializing all transaction fields.
     *
     * BR23: Transaction records completed financial payment.
     *
     * @param transactionId Unique transaction identifier
     * @param orderId Associated order identifier
     * @param customerId Customer identifier who made payment
     * @param transactionDate Date transaction took place
     * @param totalPaid Total monetary amount paid
     * @param paymentMethod Method used for payment (e.g., CASH, BANK_TRANSFER)
     */
    public Transaction(String transactionId, String orderId, String customerId,
                       LocalDate transactionDate, double totalPaid, String paymentMethod) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.transactionDate = (transactionDate != null) ? transactionDate : LocalDate.now();
        this.totalPaid = totalPaid;
        this.paymentMethod = (paymentMethod != null && !paymentMethod.trim().isEmpty())
                ? paymentMethod.trim() : "CASH";
    }

    /**
     * Convenience constructor linking transaction directly to a completed order.
     *
     * BR23: Total revenue is calculated exclusively from completed sales transactions.
     *
     * @param transactionId Unique transaction identifier
     * @param order Completed order entity
     * @param paymentMethod Payment method used (e.g., CASH, BANK_TRANSFER)
     * @throws IllegalArgumentException If order is null
     */
    public Transaction(String transactionId, Order order, String paymentMethod) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null for transaction");
        }
        this.transactionId = transactionId;
        this.orderId = order.getOrderId();
        this.customerId = order.getCustomerId();
        this.transactionDate = (order.getOrderDate() != null) ? order.getOrderDate() : LocalDate.now();
        this.totalPaid = order.getFinalAmount();
        this.paymentMethod = (paymentMethod != null && !paymentMethod.trim().isEmpty())
                ? paymentMethod.trim() : "CASH";
    }

    /**
     * Retrieves unique transaction identifier.
     *
     * @return The transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Sets unique transaction identifier.
     *
     * @param transactionId The transaction ID to set
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Retrieves associated order identifier.
     *
     * @return The order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets associated order identifier.
     *
     * @param orderId The order ID to set
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * Retrieves customer identifier who placed the order.
     *
     * @return The customer ID
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets customer identifier who placed the order.
     *
     * @param customerId The customer ID to set
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * Retrieves transaction execution date.
     *
     * @return The transaction date
     */
    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    /**
     * Sets transaction execution date.
     *
     * @param transactionDate The transaction date to set
     */
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * Retrieves total monetary payment made.
     *
     * BR23: Completed paid amount contributes to store revenue.
     *
     * @return Total paid amount
     */
    public double getTotalPaid() {
        return totalPaid;
    }

    /**
     * Sets total monetary payment made.
     *
     * @param totalPaid Total paid amount to set
     */
    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    /**
     * Retrieves payment method.
     *
     * @return The payment method
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Sets payment method.
     *
     * @param paymentMethod The payment method to set
     */
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", transactionDate=" + transactionDate +
                ", totalPaid=" + totalPaid +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}
