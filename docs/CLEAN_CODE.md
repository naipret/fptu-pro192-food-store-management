# Clean Code and Documentation Standards

This document defines the coding hygiene, method decomposition, defensive programming, exception handling, and Javadoc documentation rules enforced across the Food Store Management System.

---

## 1. Named Constants Over Magic Literals

Hardcoded numeric literals and repeated format strings ("magic numbers" and "magic strings") are prohibited inside method bodies. Every domain threshold, discount rate, layout width, and date pattern must be declared as a named `static final` constant in `UPPER_SNAKE_CASE`:

```java
public static final int SCREEN_WIDTH = 80;
public static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;
public static final int DEFAULT_NEAR_EXPIRY_DAYS = 7;
public static final double DISCOUNT_RATE = 0.10;
public static final String DISPLAY_DATE_PATTERN = "dd/MM/yyyy";
```

---

## 2. Small, Focused Methods

Methods must perform a single logical task at one level of abstraction:

1. Keep methods concise (ideally under 35 lines) and limit control-flow nesting (`if`, `for`, `while`, `try`) to at most 3 levels.
2. Use guard clauses (early returns or early exception throws at the top of a method) to handle invalid inputs or edge cases before the main execution path.
3. Whenever a block of code inside a method requires an inline explanation to understand its purpose, extract that block into a private helper method with a self-documenting verb phrase name.

---

## 3. Defensive Programming and Centralized Validation

### 3.1. Fail-Fast Invariant Validation via `InputValidator`

All stateless input normalization and domain validation rules reside in `fptu.pro192.foodstoremanagement.util.InputValidator`. Both `model` constructors/setters and `controller` entry points validate arguments through `InputValidator` immediately upon entry so invalid data can never corrupt in-memory collections or CSV files.

### 3.2. Protecting Internal Collections

Classes that own internal `List` or `Set` fields (`Product.batches`, `Order.items`, `ProductController.products`, `ProductController.categories`, `ProductController.units`, `CustomerController.customers`, `OrderController.orders`) must never return mutable collection references from getter methods. Always wrap returned collections with `Collections.unmodifiableList(...)` or `Collections.unmodifiableSet(...)`.

### 3.3. Explicit Dependency Injection

Controllers and menus must receive their shared collaborators explicitly through constructor parameters wired in `Main.java`. Do not create no-argument constructors in controllers or menus that silently instantiate duplicate `ProductController` or `CustomerController` instances in memory.

---

## 4. Exception Handling Discipline

1. Throw specific, informative exceptions when a contract or business rule is violated:
   - Use `IllegalArgumentException` for invalid field formats, negative prices/quantities, chronological date errors (`BR7`), duplicate identifiers (`BR1`, `BR2`, `BR6`), duplicate phone numbers (`BR25`), or insufficient non-expired stock (`BR10`, `BR24`).
   - Use `IllegalStateException` when an operation is invoked in an invalid lifecycle state, such as finalizing an empty order (`BR8`) or encountering corrupted CSV records.
2. Never use empty `catch (Exception e) {}` blocks that swallow errors silently.
3. Controllers propagate exceptions upward to the `view` tier, where menu classes catch them and print clear `[ERROR] <message>` notifications to the user.

---

## 5. Comment Policy, Plain-Text Javadoc, and `BR-*` Traceability

### 5.1. Zero Single-Line or Block Comments

Single-line comments (`// ...`) and inline block comments (`/* ... */`) are strictly prohibited anywhere in `.java` source files. Code structure and method names must be self-explanatory.

### 5.2. Plain-Text Javadoc Only

Every class, constructor, and non-trivial method must be documented using a standard Javadoc block (`/** ... */`) written in clear, plain English prose:

1. Do not use HTML tags inside Javadoc comments (`<p>`, `<ul>`, `<li>`, `<ol>`, `<b>`, `<i>`, `<br>`).
2. Do not use inline Javadoc markup tags (`{@code ...}`, `{@link ...}`) or HTML character entities (`&lt;`, `&gt;`, `&amp;`).
3. Include `@param`, `@return`, and `@throws` tags for all method parameters, return values, and thrown exceptions.

### 5.3. Mandatory Business Rule (`BR-*`) Traceability in Javadoc

Every method in `model`, `controller`, or `util` that enforces one or more rules from [`docs/BUSINESS_RULES.md`](BUSINESS_RULES.md) must cite the exact rule identifiers (`BR1` through `BR27`) directly inside its Javadoc description:

```java
/**
 * Deducts the requested quantity of a product across its active batches
 * using the First-Expired-First-Out algorithm (BR10, BR11, BR24, BR26).
 * Expired batches and depleted batches are skipped automatically.
 *
 * @param productId the unique identifier of the product to deduct stock from
 * @param requestedQuantity the positive integer quantity to deduct
 * @throws IllegalArgumentException if the product does not exist, quantity is not positive,
 *                                  or available non-expired stock is less than requestedQuantity
 */
public void deductStockFEFO(String productId, int requestedQuantity) {
```
