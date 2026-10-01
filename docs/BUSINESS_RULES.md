# Business Rules Specification

This document defines the complete and authoritative Business Rules (BR) for the **Food Store Management System**.\
All developers must strictly reference and preserve the numbering of these rules. In implementation code, every method enforcing business logic must include comments referencing the specific BR identifiers.

---

* **BR1**: Each Product ID must be unique in the system, follows the 6-character format `P00001`–`P99999`, and cannot be modified after creation.
* **BR2**: Each Customer ID must be unique in the system, follows the 6-character format `C00001`–`C99999`, and cannot be modified after creation.
* **BR3**: Product name and category must not be empty or blank.
* **BR4**: Product unit must not be empty or blank (e.g., `Gram`, `Mili lit`, `Goi`, `Tui`, `Hop`, `Thung`, `Lon`, `Khay`).
* **BR5**: Product price must be greater than zero.
* **BR5.1**: Product name must contain alphabetic characters; it cannot be composed entirely of numbers.
* **BR6**: Product stock quantity cannot be negative.
* **BR7**: Production date cannot be after the expiration date, and production date cannot be in the future relative to the system date.
* **BR8**: A product must exist in the system and not be marked as deleted before it can be added to a sales transaction.
* **BR9**: The quantity sold in any transaction must be greater than zero.
* **BR10**: The quantity sold cannot exceed the available active stock of the product.
* **BR11**: Expired food products cannot be sold under any circumstances.
* **BR11.1**: Products whose expiration date is earlier than the current system date must be automatically flagged as `EXPIRED` at system startup / beginning of day, and excluded from active sales.
* **BR12**: A sales transaction must contain at least one product item.
* **BR13**: $\text{Total Amount} = \sum (\text{Product Unit Price} \times \text{Quantity})$.
* **BR14**: Stock quantity is reduced immediately after a sales transaction is successfully confirmed.
* **BR15**: Regular customers receive no discount.
* **BR16**: VIP customers receive a 10% discount.
* **BR17**: $\text{Final Amount} = \text{Total Amount} - \text{Discount Amount}$.
* **BR18**: All user inputs must be validated prior to persistence or business rule processing.
* **BR19**: A food product is considered low stock when its total available quantity across active batches is less than or equal to 5 units (or the defined low-stock threshold).
* **BR20**: A product or batch is considered close to expiration when its expiration date is within 7 days of the system date.
* **BR20.1**: When importing **Chilled/Fresh Food** (`ChilledFood`), if remaining shelf life is $\le 1\text{ day}$, the system must prompt a warning confirmation.
* **BR20.2**: When importing **Dry Food** (`DryFood`), if remaining shelf life is $\le 7\text{ days}$, the system must prompt a warning confirmation.
* **BR21**: Best-selling products ranking is determined by the total quantity sold during a selected period, displaying Top 10 items.
* **BR22**: Highest-spending customers ranking is determined based on total completed purchase value during a selected period.
* **BR23**: Total revenue is calculated exclusively from completed sales transactions.
* **BR24**: When confirming a sale, stock must be deducted automatically from the active batch with the earliest valid expiration date. Depleted batches are closed, and remaining quantities cascade to subsequent earliest batches. Using FEFO (First Expired, First Out) inventory allocation.
* **BR25**: Customer phone number inputs in formats such as `+84372240629`, `+840372240629`, `(+84) 37 224 0629`, `037.224.0629`, or `0372240629` must be normalized to standard 10-digit format starting with `0` (e.g., `0372240629`) before persistence. Empty input is allowed for guest customers.
* **BR26**: **Integer Quantity & Measurement Constraint**: All inventory and transaction quantities must be positive integers (`int`). Weighted goods (meat, fish, produce) must be measured in **Grams**, while countable goods must be measured in discrete packaging units.
* **BR27**: Deleting something sets `isDeleted = true`. Records are never physically purged from CSV storage, preserving historical transaction log integrity.
