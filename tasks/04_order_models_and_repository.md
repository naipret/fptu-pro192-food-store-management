# [TASK #4] feat(sales): implement order domain models and order repository

### 👤 Ownership & Roles

- **Issue Number:** #4
- **Assignee:** Nguyễn Công Tâm
- **Reviewer:** Nguyễn Văn Phú
- **Target Module:** `sales-reporting`
- **Deadline:** 08/10/2026 (Day 7)
- **Priority:** High (P1)

---

### 🎯 Objective & Summary

Implement the domain model layer for sales transactions (`OrderDetail`, `Order`, `Transaction`) and the `OrderRepository` coordinating in-memory transaction history and atomic CSV storage (`data/orders.csv` and `data/order_details.csv`).

This task models:

1. `OrderDetail`: Captures individual line items in a transaction, preserving an immutable price snapshot at the moment of sale.
2. `Order`: Aggregates line items, executes subtotal and discount calculations (**BR13**, **BR17**), and tracks order state.
3. `Transaction`: Records completed financial payments linked directly to an `Order`.
4. `OrderRepository`: Manages in-memory order collections and atomic CSV persistence.

---

### 📖 Required Reading Before Coding

1. [`docs/BUSINESS_RULES.md`](../docs/BUSINESS_RULES.md):
   - **BR8**: A product must exist and not be marked as deleted before being added to an order.
   - **BR9**: Sold quantity in any transaction must be greater than zero.
   - **BR12**: An order must contain at least one product item.
   - **BR13**: $\text{Total Amount} = \sum (\text{Product Unit Price} \times \text{Quantity})$.
   - **BR17**: $\text{Final Amount} = \text{Total Amount} - \text{Discount Amount}$.
   - **BR23**: Total revenue is calculated exclusively from completed sales transactions.
   - **BR26**: All quantities must be positive integers (`int`).
2. [`docs/ARCHITECTURE_MODEL.md`](../docs/ARCHITECTURE_MODEL.md):
   - **Section 2**: Class Diagram showing `Order 1 --- 1..* OrderDetail` and `Transaction 1 ---> 1 Order`.
   - **Section 5.2.3**: 80-Column Cart and Invoice Summary Table Layout.

---

### 💡 Conceptual Deep Dive (For Newbies)

1. **Why `OrderDetail` Must Snapshot Unit Price:**
   - If Instant Noodles is sold today for 8,000 VND and the store raises the price tomorrow to 10,000 VND, historical invoices must still reflect 8,000 VND. Therefore, `OrderDetail` stores a detached copy of `unitPrice` and `productName` rather than referencing the live mutable `Food` entity directly.
2. **Order Aggregation & Subtotal Logic (BR13 & BR17):**
   - An `Order` owns a collection of `OrderDetail` items. Subtotal is computed by summing `item.getSubtotal()`. When a `Customer` is attached, discount is calculated via `customer.calculateDiscount(subtotal)` and deducted to determine `finalAmount`.
3. **Decoupled Flat CSV Storage:**
   - `orders.csv`: Stores transaction header info (`orderId`, `customerId`, `orderDate`, `subtotal`, `discountAmount`, `finalAmount`).
   - `order_details.csv`: Stores child line items linked via `orderId` (`orderId`, `productId`, `productName`, `unit`, `unitPrice`, `quantity`).

---

### 🛠 Technical Specifications & Target Files

#### Target Package: `fptu.pro192.foodstoremanagement.model`

##### 1. `OrderDetail.java`

- **Fields:**
  - `private String orderId;`
  - `private String productId;`
  - `private String productName;`
  - `private String unit;`
  - `private double unitPrice;`
  - `private int quantity;` (BR9, BR26)
- **Methods:**
  - Standard getters and setters enforcing **BR9** (`quantity > 0`).
  - `// BR13: Calculate line item amount`
    `public double getSubtotal() { return unitPrice * quantity; }`

##### 2. `Order.java`

- **Fields:**
  - `private String orderId;`
  - `private String customerId;`
  - `private LocalDate orderDate;`
  - `private List<OrderDetail> items = new ArrayList<>();`
  - `private double subtotal;`
  - `private double discountAmount;`
  - `private double finalAmount;`
  - `private boolean isCompleted;`
- **Methods:**
  - `public void addItem(Food product, int quantity)`:
    - Creates and appends an `OrderDetail` capturing current price snapshot.
  - `// BR13, BR15, BR16, BR17: Recomputes subtotal, discount, and final amount`
    `public void calculateTotals(Customer customer)`:
    - Computes `subtotal = sum(item.getSubtotal())`.
    - Computes `discountAmount = (customer != null) ? customer.calculateDiscount(subtotal) : 0.0`.
    - Computes `finalAmount = subtotal - discountAmount`.
  - `public List<OrderDetail> getItems()`: Returns `Collections.unmodifiableList(items)`.

##### 3. `Transaction.java`

- **Fields:**
  - `private String transactionId;`
  - `private String orderId;`
  - `private String customerId;`
  - `private LocalDate transactionDate;`
  - `private double totalPaid;`
  - `private String paymentMethod;` (e.g., `CASH`, `BANK_TRANSFER`)
- **Methods:** Standard getters and constructor.

#### Target Package: `fptu.pro192.foodstoremanagement.repository`

##### 4. `OrderRepository.java`

- **Fields:**
  - `private final String ordersFilePath = "data/orders.csv";`
  - `private final String detailsFilePath = "data/order_details.csv";`
  - `private final List<Order> orders = new ArrayList<>();`
  - `private final List<Transaction> transactions = new ArrayList<>();`
- **Methods:**
  - `public void loadFromCsv()`: Parses `orders.csv` and `order_details.csv`, stitching line items to their respective orders by `orderId`.
  - `public synchronized void saveToCsv()`: Serializes both collections and writes atomically via `MiniCsv.writeAtomic()`.
  - `public void saveOrder(Order order)`: Saves order and items, triggering `saveToCsv()`.
  - `public Order findById(String orderId)`
  - `public List<Order> findAll()`: Returns unmodifiable list of orders.
  - `public List<Transaction> findAllTransactions()`

---

### 🔗 Dependencies & System Impact

- **Depends On:** Task #1 (`MiniCsv.java`), Task #2 (`Food.java`), Task #3 (`Customer.java`).
- **Unblocks:**
  - Task #7 (Tâm): Powers the checkout workflow and invoice presentation in `OrderService` and `SalesMenu`.
  - Task #8 (Tâm): Supplies order data for monthly sales reports and best-seller rankings in `ReportService`.

---

### ✅ Acceptance Criteria & Verification

- [ ] All classes compile cleanly under Java 8 with zero warnings.
- [ ] Subtotal and discount math verified: Adding 3 items with prices 10k, 20k, 30k produces subtotal 60k. Attaching a VIP customer produces discount 6,000 VND and final total 54,000 VND (**BR13**, **BR16**, **BR17**).
- [ ] Persistence verified: `data/orders.csv` and `data/order_details.csv` successfully record line items and reload cleanly on application restart.
- [ ] Pull Request submitted conforming to Conventional Commits: `feat(sales): implement order domain models and order repository`.
