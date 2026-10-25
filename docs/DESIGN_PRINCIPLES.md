# Software Design Principles Specification

This document explains how the four `OOP` pillars, the five `SOLID` principles, and foundational software engineering principles (`SoC`, `DRY`, `KISS`, `YAGNI`) are applied across the Food Store Management System.

---

## 1. The Four Pillars of OOP

### 1.1. Encapsulation

All domain classes in `fptu.pro192.foodstoremanagement.model` (`Product`, `Batch`, `Customer`, `Order`, `OrderDetail`) declare their state fields as `private`. Mutator methods and constructors validate inputs through `InputValidator` before assigning state, ensuring no entity can enter an invalid state in memory. Internal list fields (`Product.batches` and `Order.items`) are never exposed as mutable references; accessor methods return `Collections.unmodifiableList(...)` views so external callers cannot bypass domain rules.

### 1.2. Inheritance

Shared attributes and behaviors are factored into abstract base classes where genuine "is-a" taxonomy and behavioral specialization exist:

1. `Customer` defines the common identity attributes (`id`, `fullName`, `phone`, `address`, `isDeleted`) and validation logic inherited by `RegularCustomer` and `VIPCustomer`.
2. `Menu` defines the common console rendering frame (`title`, `options`, 80-column header and prompt loop) inherited by `MainMenu`, `ProductMenu`, `InventoryMenu`, `CustomerMenu`, `SalesMenu`, and `ReportMenu`.

Conversely, retail products are modeled as a single concrete `Product` class composed with `Batch` objects (`1-to-N` composition) rather than splitting products into artificial subclasses by grocery aisle, because all retail grocery items share identical attribute structures and `FEFO` inventory behavior.

### 1.3. Polymorphism

Runtime polymorphism eliminates conditional type-checking (`instanceof` or `switch` statements on customer tier strings) during checkout and menu navigation:

1. When `Order.calculateTotals(Customer customer)` computes an order's discount, it invokes `customer.calculateDiscount(this.totalAmount)`. At runtime, the JVM dispatches the call to `RegularCustomer.calculateDiscount` (`0.0`) or `VIPCustomer.calculateDiscount` (`amount * 0.10`) based on the concrete instance.
2. When `Menu.display()` executes user selections, it invokes `handleChoice(choice)`, dispatching automatically to the active concrete menu screen.

### 1.4. Abstraction

Abstract classes expose high-level contracts while hiding implementation details from callers:

1. `OrderController` interacts solely with the abstract `Customer` reference when calculating discounts and persisting orders, remaining unaware of how specific loyalty tiers compute their discount rates.
2. `CsvUtil` abstracts the low-level mechanics of UTF-8 byte streams, RFC 4180 quote escaping, and temporary `.tmp` file replacement behind two methods: `CsvUtil.readCsv(Path)` and `CsvUtil.writeToFile(Path, List<String[]>)`.

---

## 2. SOLID Principles

### 2.1. Single Responsibility Principle (SRP)

Every class is responsible for a single, well-defined concern:

1. `Product`, `Batch`, `Customer`, `Order`, and `OrderDetail` represent domain state and intrinsic domain calculations (`getAvailableStock`, `isExpired`, `calculateDiscount`, `getSubTotal`).
2. `ProductController`, `CustomerController`, `OrderController`, and `ReportController` coordinate domain workflows, collection queries, and persistence for their respective modules.
3. `ProductMenu`, `InventoryMenu`, `CustomerMenu`, `SalesMenu`, and `ReportMenu` handle terminal rendering and user interaction without embedding business calculations or file I/O.
4. `InputValidator` centralizes stateless string normalization, regex validation, and date parsing.
5. `CsvUtil` centralizes CSV line splitting, cell escaping, and atomic file replacement.

### 2.2. Open/Closed Principle (OCP)

Software entities are open for extension but closed for modification:

1. Adding a new customer loyalty tier (such as `GoldCustomer` with a `15%` discount) requires creating a new subclass of `Customer` and registering its discriminator in `Customer.create(...)`. Neither `Order.calculateTotals` nor `OrderController.finalizeOrder` requires any modification.
2. Adding a new product category or measurement unit requires no code modification at all because `ProductController` manages categories and units dynamically in `Set<String>` (`LinkedHashSet`) registries (`BR5`).
3. Adding a new console screen requires extending `Menu` and implementing `handleChoice(int)` without modifying the base loop in `Menu.display()`.

### 2.3. Liskov Substitution Principle (LSP)

Subtypes are unconditionally substitutable for their base types without altering program correctness:

1. Both `RegularCustomer` and `VIPCustomer` honor the postcondition of `Customer.calculateDiscount(double amount)`: for any non-negative `amount`, the returned discount is never negative and never exceeds `amount`.
2. Every concrete menu (`MainMenu`, `ProductMenu`, `InventoryMenu`, `CustomerMenu`, `SalesMenu`, `ReportMenu`) honors the `Menu` lifecycle contract and terminates cleanly when the user selects `0`.

### 2.4. Interface Segregation Principle (ISP)

Classes are never forced to depend on methods or collaborators they do not use. Rather than creating a monolithic `StoreController` containing dozens of unrelated methods, controller operations are segregated by domain capability (`ProductController`, `CustomerController`, `OrderController`, `ReportController`). Each view class receives only the specific controller instances required for its screen: `CustomerMenu` depends only on `CustomerController`, and `ProductMenu` and `InventoryMenu` depend only on `ProductController`.

### 2.5. Dependency Inversion Principle (DIP)

High-level orchestration modules do not instantiate their own low-level collaborators internally. Instead, all shared controllers and menus are constructed once in `Main.java` and injected explicitly through constructors (`OrderController(Path, Path, ProductController, CustomerController)` and `ReportController(OrderController, ProductController, CustomerController)`). Removing hidden default constructors that silently allocate duplicate repositories guarantees that all screens operate on the same in-memory state.

---

## 3. Core Software Engineering Principles

### 3.1. Separation of Concerns (SoC)

Presentation formatting, business validation, and file persistence are strictly separated across packages. No class in `model` or `controller` calls `System.out.println` or reads from `Scanner`, and no class in `view` manipulates `.csv` files or mutates batch quantities directly.

### 3.2. Don't Repeat Yourself (DRY)

Repeated logic is extracted into shared utilities and base classes:

1. All 6-character ID padding and validation (`P00001`, `B00001`, `C00001`, `O00001`), Unicode name checks, phone normalization, and `dd/MM/yyyy` date parsing reside in `InputValidator`.
2. All interactive console reads (`readString`, `readInt`, `readDouble`, `confirm`) reside in `ConsoleUtil`.
3. All 80-column string truncation, centering, and padding logic resides in `TableFormatter`.
4. All CSV reading and atomic writing logic resides in `CsvUtil`.

### 3.3. Keep It Simple, Stupid (KISS)

Implementations favor clear, readable Java 8 code over complex machinery:

1. `CsvUtil` reads files line-by-line via `BufferedReader.readLine()` and parses cells with a straightforward loop over commas and quotes rather than a multi-state stream parser.
2. `FEFO` stock deduction (`BR24`) filters non-expired batches, sorts them with a clear `Comparator<Batch>` by `expirationDate`, and deducts quantities in a single forward pass.

### 3.4. You Aren't Gonna Need It (YAGNI)

Speculative abstractions and unused domain classes are excluded from the codebase. Industrial cold-storage attributes (temperature and humidity ranges), separate single-method factory classes (`ProductFactory`, `CustomerFactory`), and unused wrapper records (`Transaction.java`) are omitted so every line of code directly serves a requirement in the retail food store specification.
