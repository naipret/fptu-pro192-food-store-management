# [TASK #7] feat(sales): implement sales checkout workflow and SalesMenu

### 👤 Ownership & Roles

- **Issue Number:** #7
- **Assignee:** Nguyễn Công Tâm
- **Reviewer:** Nguyễn Văn Phú
- **Target Module:** `sales-reporting`
- **Deadline:** 11/10/2026 (Day 10)
- **Priority:** High (P1)

---

### 🎯 Objective & Summary

Implement the complete transactional checkout engine in `OrderService` and the interactive console presentation menu `SalesMenu` (**Tasks B8, B9, B10**) adhering to the 80-column ASCII terminal standard.

This task delivers:

1. `OrderService`: Manages the lifecycle of an in-memory draft shopping cart, enforces pre-sale inventory and expiration validations (**BR8–BR12**), coordinates VIP discounts (**BR15–BR17**), and executes atomic batch stock reduction via FEFO (**BR14**, **BR24**) upon sale confirmation.
2. `SalesMenu`: Concrete subclass of `AbstractMenu` guiding sales staff through transaction creation (Task B8), interactive item addition (Task B9), and bill confirmation (Task B10).

---

### 📖 Required Reading Before Coding

1. [`docs/BUSINESS_RULES.md`](../docs/BUSINESS_RULES.md):
   - **BR8**: Product must exist and not be marked as deleted.
   - **BR9**: Quantity sold must be greater than zero.
   - **BR10**: Quantity sold cannot exceed available active stock.
   - **BR11**: Expired food products cannot be sold under any circumstances.
   - **BR12**: An order must contain at least one product item.
   - **BR13**: $\text{Total Amount} = \sum (\text{Product Unit Price} \times \text{Quantity})$.
   - **BR14**: Stock quantity is reduced immediately after sales transaction is confirmed.
   - **BR15 & BR16**: Regular (0%) and VIP (10%) discount rules.
   - **BR17**: $\text{Final Amount} = \text{Total Amount} - \text{Discount Amount}$.
   - **BR24**: Atomic stock deduction via FEFO inventory allocation.
2. [`docs/ARCHITECTURE_MODEL.md`](../docs/ARCHITECTURE_MODEL.md):
   - **Section 5.1 & 5.2.3**: 80-Column Cart / Bill Summary Table Layout.
3. [`docs/DESIGN_PATTERNS.md`](../docs/DESIGN_PATTERNS.md):
   - **Section 4**: Strategy Pattern (Customer discount calculation).
   - **Section 5**: Template Method Pattern (`AbstractMenu`).

---

### 💡 Conceptual Deep Dive (For Newbies)

#### The Draft Cart Lifecycle (Atomic Checkout Guarantee)

Sales transactions span multiple user interactions across Tasks B8, B9, and B10:

```markdown
[ Task B8: Create Transaction ] ──> Creates in-memory Draft Order (No stock deducted)
                                            │
                                            ▼
[ Task B9: Add Products ]       ──> Validates active stock & expiry per item (Draft accumulation)
                                            │
                                            ▼
[ Task B10: Bill Summary ]      ──> Displays items, calculates subtotal, VIP discount, and final amount
                                            │
                      ┌─────────────────────┴─────────────────────┐
                      ▼                                           ▼
             [1] Confirm Sale                            [2] Cancel / [0] Back
                      │                                           │
  - Calls ProductService.deductStockFEFO(...)      - Discards Draft Order
  - Generates Transaction & writes CSV             - Stock remains 100% untouched
```

**Crucial Invariant:** Physical warehouse inventory must **NEVER** be decremented while the cart is in draft status. Stock deduction occurs strictly upon explicit user confirmation in Task B10.

---

### 🛠 Technical Specifications & Target Files

#### Target Package: `fptu.pro192.foodstoremanagement.service`

##### 1. `OrderService.java`

- **Fields:**
  - `private final OrderRepository orderRepo;`
  - `private final ProductService productService;`
  - `private final CustomerService customerService;`
- **Methods:**
  - `// Task B8: Creates a fresh in-memory draft order`
    `public Order createDraftOrder(String orderId, String customerId, LocalDate orderDate)`
  - `// Task B9: Validates stock (BR10) & expiry (BR11) before adding item to draft`
    `public void addItemToDraft(Order draft, String productId, int quantity, LocalDate today)`:
    - Finds product; verifies `!product.isDeleted()` (**BR8**).
    - Checks `quantity > 0` (**BR9**, **BR26**).
    - Verifies product has at least one active, non-expired batch (**BR11**).
    - Checks `quantity <= product.getTotalStock()` (**BR10**).
    - Adds line item snapshot to `draft` and recomputes totals (**BR13**, **BR17**).
  - `// Task B10: Finalizes order, executes FEFO batch deduction, records transaction`
    `public synchronized Transaction confirmSale(Order draft, LocalDate today, String paymentMethod)`:
    - Verifies `draft.getItems().size() > 0` (**BR12**).
    - For each `OrderDetail` in draft: calls `productService.deductStockFEFO(item.getProductId(), item.getQuantity(), today)` (**BR14**, **BR24**).
    - Marks `draft.setCompleted(true)`.
    - Creates `Transaction` record with `totalPaid = draft.getFinalAmount()`.
    - Persists order and transaction via `orderRepo.saveOrder(draft)`.
    - Returns completed transaction.

#### Target Package: `fptu.pro192.foodstoremanagement.ui`

##### 2. `SalesMenu.java`

> [!NOTE]
> **Provisional / Pseudo Menu Specification**:
> The checkout flow, prompts, and options shown below are tentative draft references (pseudo-design). They are not finalized and can be adjusted during implementation to fit optimal user experience, provided the 80-column width limit, Vietnamese language, clear screen on transition, and `[0]` to return/cancel are strictly maintained.

- **Extends:** `AbstractMenu`
- **Title:** `"QUẢN LÝ BÁN HÀNG"`
- **Flow (Tentative Draft / Pseudo):**
  1. Prompts for Transaction ID and Customer ID (Task B8).
  2. Enters interactive item addition loop: displays product info, available stock, prompts for quantity (Task B9).
  3. Displays 80-column Bill Summary per Section 5.2.3: subtotal, VIP discount (10%), and total amount (Task B10).
  4. Prompts `[1] Confirm Sale [2] Cancel`.

---

### 🔗 Dependencies & System Impact

- **Depends On:** Task #1 (`AbstractMenu`), Task #4 (`OrderRepository`), Task #5 (`ProductService`), Task #6 (`CustomerService`).
- **Unblocks:**
  - Task #8 (Tâm): Supplies transaction data for monthly revenue and ranking reports.
  - Task #9 (Phú): Attached as submenu 3 in `MainMenu`.

---

### ✅ Acceptance Criteria & Verification

- [ ] NetBeans 13 compiles with 0 errors and 0 warnings under Java 8.
- [ ] Cart validation test: Adding an expired item fails with `"Failed to add product. This food product has expired."` (**BR11**).
- [ ] Insufficient stock test: Adding quantity $> \text{active stock}$ fails with `"Failed to add product. Insufficient stock."` (**BR10**).
- [ ] Cancellation test: Building a draft cart and choosing `[2] Cancel` does not reduce product inventory and leaves CSV files unchanged.
- [ ] Confirmation test: Choosing `[1] Confirm Sale` deducts batches via FEFO, creates `orders.csv` and `order_details.csv` entries, and prints success confirmation (**BR14**).
- [ ] Bill summary table strictly adheres to the 80-column ASCII format.
- [ ] PR created targeting `main`: `feat(sales): implement sales checkout workflow and SalesMenu`.
