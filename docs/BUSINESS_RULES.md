# Business Rules Specification

This document defines the domain invariants, validation constraints, inventory algorithms, and transactional policies governing the Food Store Management System. Every business rule is assigned a stable identifier (`BR1` through `BR27`) that is referenced directly inside the Javadoc of the methods enforcing that rule.

---

## 1. Product Catalog and Identification Rules

| Rule ID | Rule Name | Specification |
| :--- | :--- | :--- |
| `BR1` | Unique Product Identifier | Every product must have a unique, non-empty identifier normalized to the 6-character format `P` followed by 5 digits (`P00001` through `P99999`). Typing a shorthand number such as `P1` or `1` normalizes automatically to `P00001`. |
| `BR3` | Text Sanitization and CSV Injection Guard | Product names, customer names, and addresses must not be blank and must contain only Unicode letters (`\p{L}`), digits, spaces, and standard punctuation. To prevent CSV formula injection when opening files in spreadsheet software, no text field may begin with `=`, `+`, `-`, or `@`. |
| `BR4` | Positive Selling Price | A product's unit selling price (`price`) must be strictly greater than zero (`price > 0`). |
| `BR5` | Standardized Category and Unit Registry | Every product belongs to a single-word retail grocery category (`Grains`, `Noodles`, `Meat`, `Seafood`, `Produce`, `Dairy`, `Frozen`, `Canned`, `Beverages`, `Condiments`, `Snacks`, `Bakery`) and a standard measurement unit (`Pack`, `Box`, `Bottle`, `Can`, `Bag`, `Tray`, `Kg`, `Gram`, `Jar`, `Carton`) managed in a `Set<String>` registry. Users select from the existing set or register a new single-concept entry. |

---

## 2. Inventory, Batch, and FEFO Stock Rules

| Rule ID | Rule Name | Specification |
| :--- | :--- | :--- |
| `BR6` | Non-Negative Batch Quantity | Every inventory batch must have a unique 6-character identifier (`B00001` through `B99999`). When a new batch is received, its initial quantity must be a positive integer (`quantity > 0`). As items are sold, the remaining batch quantity may decrease to zero (`quantity >= 0`) and is never allowed to become negative. |
| `BR7` | Chronological Production and Expiration Dates | Every batch records an `importDate`, a `productionDate`, and an `expirationDate`. Interactive console prompts accept and display dates in `dd/MM/yyyy` format, while CSV files store dates in ISO-8601 `yyyy-MM-dd` format. A batch is valid only when `productionDate` is on or before `expirationDate` (`!productionDate.isAfter(expirationDate)`). |
| `BR11` | Expired Stock Exclusion | A batch is considered expired when the current system date is strictly after its `expirationDate` (`LocalDate.now().isAfter(expirationDate)`). A batch whose `expirationDate` equals today remains valid until the end of the day. Expired batches remain stored in inventory for auditing and reporting but are excluded from available sellable stock and cannot be deducted during checkout. |
| `BR19` | Low-Stock Warning Threshold | A product is flagged as low in stock when its total non-expired sellable quantity (`getAvailableStock()`) is greater than or equal to zero and less than or equal to the default threshold of `5` units (or a custom threshold specified by the user). |
| `BR20` | Near-Expiry Warning Window | A batch is flagged as near expiry when it has remaining stock (`quantity > 0`), has not yet expired (`!isExpired()`), and its `expirationDate` falls within the next `7` calendar days (or a custom number of days specified by the user) relative to `LocalDate.now()`. |
| `BR24` | FEFO Stock Deduction | When an order is finalized, inventory is deducted across the product's active, non-expired batches using the First-Expired-First-Out (`FEFO`) algorithm. Valid batches with `quantity > 0` are sorted in ascending order of `expirationDate` (with `importDate` and `batchId` as tie-breakers), and stock is consumed sequentially from the earliest-expiring batch until the order quantity is satisfied. |
| `BR26` | Integer Quantity Constraint | All batch quantities, stock adjustments, and order line-item quantities are represented as 32-bit integers (`int`) rather than floating-point numbers to eliminate IEEE 754 rounding errors during inventory deduction. |

---

## 3. Customer Identity and Loyalty Tier Rules

| Rule ID | Rule Name | Specification |
| :--- | :--- | :--- |
| `BR2` | Unique Customer Identifier | Every registered customer must have a unique, non-empty identifier normalized to the 6-character format `C` followed by 5 digits (`C00001` through `C99999`). |
| `BR15` | Loyalty Tier Discount Calculation | Each registered customer belongs to a concrete subclass of `Customer` that determines discount eligibility via polymorphism. `RegularCustomer` receives a `0%` discount, whereas `VIPCustomer` receives a `10%` discount (`amount * 0.10`) on the order subtotal. |
| `BR16` | Guest Checkout Policy | Customers who purchase items without registering or providing a customer identifier are processed as guest customers (`customerId = null` in runtime memory, serialized as `GUEST` in `orders.csv`). Guest orders are always charged the full subtotal with `0%` discount. |
| `BR25` | Phone Number Normalization and Uniqueness | Every registered customer must provide a valid Vietnamese mobile phone number. Raw input may contain spaces, hyphens, dots, or the international prefix `+84` / `84`, which `InputValidator.normalizePhone` strips and normalizes into a 10-digit string matching `^0[35789]\d{8}$`. Each active customer phone number must be unique across the customer registry. |

---

## 4. Sales Order and Checkout Lifecycle Rules

| Rule ID | Rule Name | Specification |
| :--- | :--- | :--- |
| `BR8` | Order Requirement of Line Items | Every sales order must be assigned a unique 6-character identifier (`O00001` through `O99999`) and must contain at least one `OrderDetail` line item before checkout can be completed. |
| `BR9` | Positive Line-Item Quantity | Each `OrderDetail` added to an order must specify a strictly positive integer quantity (`quantity > 0`). Adding the same product multiple times within the same draft order merges the requested quantities into the existing line item. |
| `BR10` | Available Stock Sufficiency | The cumulative quantity of a product requested in an order cannot exceed the product's currently available non-expired stock (`product.getAvailableStock()`). If requested quantity exceeds available stock either while building the cart or at final checkout, `InsufficientStockException` is thrown. |
| `BR12` | Line-Item Subtotal Formula | The subtotal of each `OrderDetail` is calculated as `quantity * unitPrice`, where `unitPrice` is a historical snapshot of the product's selling price captured at the moment the item is added to the order. |
| `BR13` | Order Financial Totals Formula | The gross `totalAmount` of an `Order` is the sum of all line-item subtotals. The `discount` is determined by passing `totalAmount` to the associated customer's `calculateDiscount(totalAmount)` method (or `0.0` for guest orders), and `finalAmount` equals `Math.max(0.0, totalAmount - discount)`. |
| `BR14` | Transactional Stock Deduction on Completion | Creating a draft order and adding items to the shopping cart do not mutate product batch quantities. Actual `FEFO` stock deduction (`BR24`) and CSV persistence occur atomically only when `OrderController.finalizeOrder` succeeds. |
| `BR17` | Immutable Completed Orders | Once an order is finalized and persisted to `orders.csv` and `order_details.csv`, its line items, captured unit prices, discount, and timestamps are immutable historical records and cannot be edited or deleted. |

---

## 5. Reporting, Persistence, and Data Integrity Rules

| Rule ID | Rule Name | Specification |
| :--- | :--- | :--- |
| `BR18` | Referential Integrity Across Entities | Every `Batch` must reference an existing `productId`. Every `Order` with a non-guest customer must reference an existing `customerId`, and every `OrderDetail` must reference both a valid `orderId` and an existing `productId`. |
| `BR21` | Daily and Monthly Revenue Aggregation | Daily and monthly revenue reports aggregate only completed orders matching the requested `LocalDate` or `YearMonth`. Revenue figures always sum the net `finalAmount` (after VIP discounts) rather than the pre-discount subtotal. |
| `BR22` | Best-Selling Product Ranking | Best-selling product rankings aggregate total units sold (`quantity`) and total gross line-item revenue across all completed orders, sorted in descending order of total quantity sold (with gross revenue as the secondary sort key). |
| `BR23` | Customer Spending Aggregation | Customer spending summaries aggregate total completed order count and cumulative net spending (`finalAmount`) per registered customer, sorted in descending order of total spending. Guest transactions (`GUEST`) are excluded from registered customer loyalty rankings. |
| `BR27` | Soft Delete and Identifier Reservation | Deleting a `Product` or `Customer` sets its `isDeleted` flag to `true` rather than removing the record from the CSV file. Soft-deleted entities are hidden from active catalog, search, and checkout selection screens, but their identifiers remain permanently reserved (`existsById` checks both active and soft-deleted records) so historical orders and reports always resolve the original product and customer names. |
