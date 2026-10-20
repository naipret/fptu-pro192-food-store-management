# Design Patterns Specification

This document describes the five software design patterns implemented in the Food Store Management System, detailing the specific problem each pattern solves, its implementation in the codebase, and its trade-offs.

---

## 1. Static Factory (Simple Factory) Pattern

### 1.1. Context and Problem

When loading customer records from `data/customers.csv` or registering a new customer in `CustomerController`, the application reads a string discriminator (`"Regular"` or `"VIP"`) and must instantiate the corresponding concrete subclass (`RegularCustomer` or `VIPCustomer`). Scattering `if-else` or `switch` instantiation logic across controllers and menus couples callers to every concrete subclass.

### 1.2. Implementation in Codebase

The abstract base class `Customer` provides a static factory method `Customer.create(String type, String id, String fullName, String phone, String address, boolean isDeleted)`:

```java
public static Customer create(String type, String id, String fullName,
        String phone, String address, boolean isDeleted) {
    if (type == null || type.trim().isEmpty()) {
        throw new IllegalArgumentException("Customer type cannot be empty.");
    }
    String normalizedType = type.trim();
    if ("Regular".equalsIgnoreCase(normalizedType)) {
        return new RegularCustomer(id, fullName, phone, address, isDeleted);
    }
    if ("VIP".equalsIgnoreCase(normalizedType)) {
        return new VIPCustomer(id, fullName, phone, address, isDeleted);
    }
    throw new IllegalArgumentException("Unsupported customer type: " + type);
}
```

Placing the factory method directly on `Customer` (following the standard Java convention of `LocalDate.of` or `Collections.unmodifiableList`) centralizes subclass selection without introducing a separate single-method `CustomerFactory` class.

---

## 2. Atomic CSV Persistence Pattern

### 2.1. Context and Problem

Writing directly to a live data file (`data/products.csv` or `data/orders.csv`) using a standard `FileWriter` truncates the existing file immediately. If an `IOException`, power loss, or unexpected termination occurs mid-write, the CSV file is left half-written and corrupted.

### 2.2. Implementation in Codebase

`CsvUtil.writeAtomic(Path targetPath, List<String[]> rows)` implements a two-phase atomic write protocol:

1. Ensure the parent directory (`data/`) exists and write all escaped CSV rows (starting with the header row at index 0) to a sibling temporary file (`<filename>.csv.tmp`), flushing the UTF-8 buffered stream completely.
2. Replace the target file in a single operation using `Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)`, falling back to `StandardCopyOption.REPLACE_EXISTING` if the underlying filesystem does not support atomic move semantics.

Each domain controller (`ProductController`, `CustomerController`, `OrderController`) pairs in-memory collection updates with `saveToCsv()` calls that delegate to `CsvUtil.writeAtomic`, guaranteeing that on-disk files always contain complete, valid snapshots.

---

## 3. Singleton Pattern

### 3.1. Context and Problem

In Java console applications, instantiating multiple `new Scanner(System.in)` objects across different menus and utility classes causes shared input buffer conflicts, skipped lines, and `NoSuchElementException` errors. Moreover, closing any wrapper `Scanner` closes the underlying `System.in` stream permanently for the entire JVM process.

### 3.2. Implementation in Codebase

`ScannerManager` implements an eager, thread-safe Singleton that owns the single `Scanner(System.in, "UTF-8")` instance for the lifetime of the application:

```java
public final class ScannerManager {

    private static final Scanner INSTANCE = new Scanner(System.in, "UTF-8");

    private ScannerManager() {
    }

    public static Scanner getInstance() {
        return INSTANCE;
    }
}
```

`ConsoleUtil` retrieves the shared `Scanner` exclusively through `ScannerManager.getInstance()`, ensuring consistent UTF-8 line reading across all menus.

---

## 4. Strategy Pattern (via Subclass Polymorphism)

### 4.1. Context and Problem

Different customer loyalty tiers apply different discount calculation rules during checkout (`BR15`): `Regular` customers pay full price (`0%` discount), while `VIP` customers receive a `10%` discount on the order subtotal.

### 4.2. Implementation in Codebase

`Customer` declares the abstract discount calculation contract `public abstract double calculateDiscount(double amount)`. Concrete subclasses encapsulate the tier-specific pricing algorithm:

1. `RegularCustomer.calculateDiscount(double amount)` validates that `amount >= 0` and returns `0.0`.
2. `VIPCustomer.calculateDiscount(double amount)` validates that `amount >= 0` and returns `amount * DISCOUNT_RATE` (`0.10`).

During checkout, `Order.calculateTotals(Customer customer)` delegates discount calculation to the passed `Customer` strategy object (`double calculatedDiscount = (customer == null) ? 0.0 : customer.calculateDiscount(sum)`), keeping `Order` independent of loyalty tier rules.

---

## 5. Template Method Pattern

### 5.1. Context and Problem

Every interactive console screen (`MainMenu`, `ProductMenu`, `InventoryMenu`, `CustomerMenu`, `SalesMenu`, `ReportMenu`) follows the same interaction loop: render the 80-column header Banner, list numbered options (`[1]..[N]` and `[0]` Back/Exit), read a validated integer choice, dispatch the selected action, optionally pause for the user to press `Enter`, and repeat until `0` is chosen. Duplicating this `while` loop across six menu classes violates `DRY` and leads to inconsistent navigation behavior.

### 5.2. Implementation in Codebase

The abstract class `Menu` defines the `final` template method `display()`:

```java
public final void display() {
    boolean running = true;
    while (running) {
        ConsoleUtil.clearScreen();
        renderMenuFrame();
        int choice = ConsoleUtil.readInt("Enter your choice [0-" + options.length + "]: ", 0, options.length);
        if (choice == 0) {
            running = false;
        } else {
            handleChoice(choice);
            if (shouldPauseAfterAction()) {
                ConsoleUtil.pressEnterToContinue();
            }
        }
    }
}
```

Subclasses implement the abstract primitive operation `protected abstract void handleChoice(int choice)` and may override the hook method `protected boolean shouldPauseAfterAction()` (which defaults to `true` in `Menu` for leaf action screens and returns `false` in `MainMenu` so returning from a submenu does not trigger a redundant `Press Enter` prompt).
