# Naming Conventions Specification

This document defines the naming rules for packages, classes, methods, variables, constants, and domain identifiers across the Food Store Management System.

---

## 1. General Java Identifier Rules

| Element Type | Case Convention | Grammatical Form | Project Examples |
| :--- | :--- | :--- | :--- |
| Package | `lowercase` | Concatenated domain words without underscores | `fptu.pro192.foodstoremanagement.controller` |
| Class / Abstract Class | `PascalCase` | Singular noun or noun phrase | `Product`, `Batch`, `Customer`, `Menu`, `CsvUtil` |
| Method | `camelCase` | Imperative verb or verb phrase | `deductStockFEFO`, `calculateDiscount`, `writeToFile` |
| Local Variable / Field / Parameter | `camelCase` | Descriptive noun or noun phrase | `productId`, `expirationDate`, `availableStock` |
| Constant (`static final`) | `UPPER_SNAKE_CASE` | Uppercase words separated by underscores | `SCREEN_WIDTH`, `DISCOUNT_RATE`, `DEFAULT_LOW_STOCK_THRESHOLD` |

Single-letter variable names (`a`, `b`, `x`, `tmp`) are prohibited except for standard loop indices (`i`, `j`) inside short indexed loops.

---

## 2. Architectural Suffixes by Package

Every class outside the `model` package uses a suffix that identifies its architectural role at a glance:

| Package | Required Suffix | Responsibility | Examples |
| :--- | :--- | :--- | :--- |
| `model` | None (Domain Noun) | Represents a domain entity or value record | `Product`, `Batch`, `Customer`, `RegularCustomer`, `VIPCustomer`, `Order`, `OrderDetail` |
| `controller` | `*Controller` | Coordinates business rules, collection queries, and CSV persistence | `ProductController`, `CustomerController`, `OrderController`, `ReportController` |
| `view` | `*Menu` | Renders an 80-column interactive console menu screen | `Menu`, `MainMenu`, `ProductMenu`, `InventoryMenu`, `CustomerMenu`, `SalesMenu`, `ReportMenu` |
| `util` | `*Util`, `*Validator`, `*Formatter`, or `*Manager` | Provides stateless helper routines or singleton stream management | `CsvUtil`, `InputValidator`, `ConsoleUtil`, `TableFormatter`, `ScannerManager` |

---

## 3. Method Verb and Boolean Prefix Conventions

Method names must begin with a verb or prefix that accurately reflects whether the method queries state, mutates state, or transforms input:

| Prefix | Contract and Return Type | Project Examples |
| :--- | :--- | :--- |
| `is*`, `has*`, `should*` | Side-effect-free predicate returning `boolean` | `isExpired()`, `isNearExpiry(int)`, `isLowStock(int)`, `isDeleted()`, `shouldShowBreadcrumb()` |
| `get*` | Accessor or computed query returning a value or unmodifiable collection | `getAvailableStock()`, `getTotalStock()`, `getSubTotal()`, `getBatches()`, `getCategories()` |
| `set*` | Validated field mutator returning `void` | `setName(String)`, `setPrice(double)`, `setQuantity(int)`, `setDeleted(boolean)` |
| `find*` | Single-entity lookup returning an entity or `null` if not found | `findById(String)`, `findByIdIncludeDeleted(String)`, `findByPhone(String)` |
| `search*` | Multi-entity query returning a `List<T>` (empty list if no matches) | `searchByName(String)`, `searchByCategory(String)`, `searchByExpirationDate(LocalDate)` |
| `calculate*` | Mathematical computation of discounts or financial totals | `calculateDiscount(double)`, `calculateTotals(Customer)` |
| `validate*` | Stateless invariant check throwing `IllegalArgumentException` on failure | `validatePositivePrice(double)`, `validatePositiveQuantity(int)`, `validateDateOrder(LocalDate, LocalDate)`, `validateBatchDates(LocalDate, LocalDate, LocalDate)` |
| `normalize*` | Trims, formats, validates, and returns a canonical string | `normalizeId(String, char)`, `normalizePhone(String)`, `normalizeCustomerName(String)`, `normalizeProductName(String)`, `normalizeAddress(String)` |
| `load*` / `save*` | Reads or writes persistent CSV snapshots | `loadFromCsv()`, `saveToCsv()` |

---

## 4. Domain Identifier Formatting Rules

All primary entity identifiers follow a uniform 6-character uppercase pattern consisting of a 1-letter entity prefix followed by a 5-digit zero-padded number (`00001` through `99999`):

| Entity | Prefix | Canonical Format | Shorthand Input Accepted by `InputValidator.normalizeId` |
| :--- | :--- | :--- | :--- |
| `Product` | `P` | `P00001` – `P99999` | `"1"`, `"p1"`, `"P01"` -> `"P00001"` |
| `Batch` | `B` | `B00001` – `B99999` | `"1"`, `"b1"`, `"B01"` -> `"B00001"` |
| `Customer` | `C` | `C00001` – `C99999` | `"1"`, `"c1"`, `"C01"` -> `"C00001"` |
| `Order` | `O` | `O00001` – `O99999` | `"1"`, `"o1"`, `"O01"` -> `"O00001"` |

---

## 5. Compliant vs. Non-Compliant Examples

| Category | Non-Compliant Name | Compliant Name | Reason |
| :--- | :--- | :--- | :--- |
| Class Name | `Food`, `MiniCsv`, `AbstractMenu` | `Product`, `CsvUtil`, `Menu` | Aligns with retail domain terminology and standard architectural suffixes |
| Boolean Method | `checkExpired()`, `expiredStatus()` | `isExpired()` | Boolean predicates must start with `is`, `has`, or `should` |
| Stock Query | `getStock()` | `getAvailableStock()` vs. `getTotalStock()` | Distinguishes non-expired sellable stock (`BR11`) from raw physical batch sum |
| Constant | `double d = 0.1;` | `public static final double DISCOUNT_RATE = 0.10;` | Replaces magic number with a descriptive `UPPER_SNAKE_CASE` constant |
| Collection Variable | `List<Product> list1` | `List<Product> availableProducts` | Describes the domain contents of the collection |
