# Design Principles Specification

This document details the foundational software engineering and object-oriented design principles applied across the **Food Store Management System**.

---

## 1. SOLID Principles

The system strictly enforces all five SOLID principles to achieve low coupling and high cohesion across all modules.

### 1.1. Single Responsibility Principle (SRP)
>
> *"A class should have one, and only one, reason to change."*

Each class in the system is assigned a single, well-defined responsibility:

* **`MiniCsv`**: Solely responsible for RFC 4180 CSV serialization, escaping, and disk file parsing. It has zero knowledge of products, orders, or business logic.
* **`ProductService` / `OrderService` / `CustomerService`**: Solely responsible for enforcing business rules (BR1–BR27), state validation, and transactional workflows.
* **`ConsoleView` / `ProductView`**: Solely responsible for formatting data into an 80-column ASCII terminal user interface and capturing raw user input.
* **`InputValidator`**: Solely responsible for validating data formats (phone numbers, IDs, date boundaries, price ranges).

### 1.2. Open/Closed Principle (OCP)
>
> *"Software entities should be open for extension, but closed for modification."*

The architecture allows adding new business capabilities without modifying tested core logic:

* **Extending Product Categories**: If the business introduces a new category (e.g., `CannedFood` or `BeverageFood`), developers create a new subclass extending `Food` and implement the polymorphic methods `getStorageInstructions()` and `getDaysBeforeExpiryWarning()`. The checkout engine in `OrderService` and table renderers in `ConsoleView` require **zero modifications**.
* **Extending Customer Tiers**: Introducing a new customer tier (e.g., `WholesaleCustomer` with 15% discount) requires creating a new subclass of `Customers` overriding `getDiscountRate()`. Billing calculations remain unchanged.

### 1.3. Liskov Substitution Principle (LSP)
>
> *"Objects of a superclass should be replaceable with objects of its subclasses without breaking the application."*

All subclasses maintain behavioral compatibility with their parent contracts:

* **Product Substitution**: `FrozenFood`, `ChilledFood`, and `DryFood` can be treated interchangeably as `Food` references throughout `OrderDetail`, `InventoryService`, and sorting algorithms without type-checking hacks (`instanceof`).
* **Customer Substitution**: `RegularCustomer` and `VIPCustomer` can be substituted wherever a `Customers` is expected during order calculations; neither subclass throws unsupported exceptions or alters pre/post-conditions.

### 1.4. Interface Segregation Principle (ISP)
>
> *"Clients should not be forced to depend upon interfaces that they do not use."*

Instead of large, monolithic interfaces ("fat interfaces"), the system defines small, highly cohesive interface contracts:

```java
// Focuses only on identity
public interface Identifiable<ID> {
    ID getId();
}

// Focuses only on terminal tabular rendering
> *"High-level modules should not depend on low-level modules. Both should depend on abstractions."*

* High-level business services (`ProductService`, `OrderService`) do not depend directly on concrete disk file readers, writers, or hardcoded CSV paths.
* Services interact with clean repository abstractions (`ProductRepository`, etc.) which encapsulate data access, allowing business domain logic to remain independent of file storage mechanisms.

---

## 2. General Clean Code & Architecture Principles

### 2.1. Separation of Concerns (SoC) / 3-Tier Layered Architecture

The application is organized into three distinct architectural layers with strict unidirectional dependencies:
[ Presentation Layer (TUI / Menu / Views / ConsoleUtil) ]
                        │
                        ▼
[ Business Logic / Service Layer (OrderService, ProductService, CustomerService) ]
                        │
                        ▼
[ Data Access / Repository Layer (Repositories, MiniCsv Engine) ]
                        │
                        ▼
[ Domain Model Layer (Food, Batch, Customer, Order, OrderDetail, Transaction) ]
```

* **Service Layer** coordinaMenuions and validates rules, remaining agnostic of console formatting.
* **Data Access / Repository Layer** abstracts file storage, coordinates in-memory data, and guarantees atomic writes.
* **Domain Layer** contains core entities and domain-specific operations (e.g., `calculateTotals()`, `isExpired()`).

### 2.2. Don't Repeat Yourself (DRY)

* User input loops and error prompting are encapsulated in `ConsoleUtil` and `ScannerManager`.
* 80-column ASCII table drawing, text truncation, and padding logic are centralized in `TableFormatter`.

### 2.3. Keep It Simple, Stupid (KISS) & YAGNI

* Built strictly on Core Java 8 SE without heavyweight external libraries, enterprise containers, or premature micro-frameworks.
* Avoids unnecessary complexity: Flat CSV files provide straightforward, inspectable persistence meeting all course requirements.

* **Input Validation**: Bad inputs are caught immediately at the UI layer before being passed down to services (`BR18`).
* **Encapsulation & Defensive Copying**: Internal collections (such as `batches` in `Food` or `items` in `Order`) return unmodifiable wrappers (`Collections.unmodifiableList(...)`) to prevent external code from mutating state without going through domain methods.
* **Precondition Checking**: Business methods validate invariants immediately (e.g., non-negative quantity, valid expiration date) and throw descriptive domain exceptions (`InsufficientStockException`, `InvalidProductDataException`).

---

## 3. Principle-to-Component Mapping Matrix

| Principle | Primary Components / Classes | Realization in Code |
| :--- | :--- | :--- |
| **SRP** | `MiniCsv`, `ProductRepository`, `OrderService`, `ConsoleView` | Dedicated file I/O, persistence, business rules, and UI rendering. |
| **OCP** | `Food` $\to$ (`FrozenFood`, `ChilledFood`, `DryFood`) | Add new food types without modifying checkout or inventory calculation. |
| **LSP** | `Customers` $\to$ (`RegularCustomer`, `VIPCustomer`) | Transparent polymorphic discount calculation via `getDiscountRate()`. |
| **ISP** | `Identifiable`, `TableDisplayable` | Segregated, purpose-built interfaces. |
| **DIP** | `ProductService` depends on `ProductRepository` | Decoupled business logic from low-level CSV file I/O. |
| **DRY** | `ScannerManager`, `ConsoleUtil`, `TableFormatter` | Single Scanner lifecycle and shared ASCII layout rendering. |
| **SoC** | 3-Tier Layering (`view` $\to$ `service` $\to$ `repository`) | Strict boundary enforcement between UI, logic, and persistence. |
