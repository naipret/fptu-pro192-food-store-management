# [TASK #5] feat(product): implement FEFO deduction in ProductService and 80-col ProductMenu

### 👤 Ownership & Roles

- **Issue Number:** #5
- **Assignee:** Trần Cao Thành
- **Reviewer:** Nguyễn Văn Phú
- **Target Module:** `product-inventory`
- **Deadline:** 11/10/2026 (Day 10)
- **Priority:** High (P1)

---

### 🎯 Objective & Summary

Implement the core business service `ProductService` orchestrating inventory business rules—including the critical **First Expired, First Out (FEFO)** batch deduction algorithm (**BR24**)—and the terminal presentation menu `ProductMenu` conforming to strict 80-column ASCII formatting (**Tasks B1–B4, B11–B14**).

This task delivers:

1. `ProductService`: Enforces business rules (BR1, BR3–BR7, BR10, BR11, BR14, BR19, BR20, BR24, BR27).
2. FEFO inventory allocation algorithm: Automatically allocates sold goods from the earliest valid expiring batch, cascading across batches until fulfillment.
3. `ProductMenu`: Concrete subclass of `AbstractMenu` providing interactive console UI for catalog management, stock replenishment, and shelf-life monitoring.

---

### 📖 Required Reading Before Coding

1. [`docs/BUSINESS_RULES.md`](../docs/BUSINESS_RULES.md):
   - **BR10**: Sold quantity cannot exceed active available stock.
   - **BR11 & BR11.1**: Expired food products cannot be sold under any circumstances.
   - **BR14**: Stock quantity is reduced immediately upon transaction confirmation.
   - **BR19**: Low stock threshold: total active stock $\le 5$ units.
   - **BR20**: Close to expiration: within 7 days of system date.
   - **BR20.1 & BR20.2**: Shelf-life warnings on import (Chilled $\le 1$ day, Dry $\le 7$ days).
   - **BR24**: **FEFO (First Expired, First Out) Inventory Allocation Algorithm**.
2. [`docs/ARCHITECTURE_MODEL.md`](../docs/ARCHITECTURE_MODEL.md):
   - **Section 5.1**: Text Truncation & Monospace Fit Rule (`TableFormatter.fit(text, width)`).
   - **Section 5.2.1**: Exact 80-column Product Table specification.
3. [`docs/DESIGN_PATTERNS.md`](../docs/DESIGN_PATTERNS.md):
   - **Section 5**: Template Method Pattern (`AbstractMenu`).

---

### 💡 Conceptual Deep Dive (For Newbies)

#### Detailed Walkthrough of FEFO Batch Deduction (BR24)

Suppose a customer orders **7 packages** of Instant Noodles (`P00001`):

1. Retrieve all batches of `P00001` where `!isExpired(today)` and `quantity > 0`.
2. Sort candidate batches in **ascending order by `expirationDate`** (earliest expiring first).
3. If total active stock across batches is $< 7$, throw `InsufficientStockException` (**BR10**).
4. Iterate through sorted batches:
   - **Batch 1 (Expires 10/10/2026, Qty: 3)**: Cannot satisfy all 7 units. Deduct 3 units (`qty = 0`). Remaining needed: 4 units.
   - **Batch 2 (Expires 15/10/2026, Qty: 10)**: Can satisfy remaining 4 units. Deduct 4 units (`qty = 6`). Remaining needed: 0 units.
5. Save updated batches atomically to CSV (**BR14**).

#### Rigid 80-Column Layout Compliance

Terminal tables must not overflow 80 columns:
`| Mã SP (6) | Tên Sản Phẩm (22) | Phân Loại (9) | ĐVT (6) | Đơn Giá (11) | Tồn Kho (7) |`
All name fields must be wrapped with `TableFormatter.fit(name, 22)` to prevent ragged vertical borders `|`.

---

### 🛠 Technical Specifications & Target Files

#### Target Package: `fptu.pro192.foodstoremanagement.service`

##### 1. `ProductService.java`

- **Fields:** `private final ProductRepository productRepo;`
- **Methods:**
  - `// BR1, BR3-BR7, BR20.1, BR20.2: Add new product with initial batch`
    `public void addProduct(Food product, Batch initialBatch, LocalDate today)`
  - `// BR24: Deducts stock using First Expired, First Out (FEFO)`
    `public synchronized void deductStockFEFO(String productId, int quantity, LocalDate today)`:
    - Throws `InsufficientStockException` if requested `quantity > getTotalStock()`.
    - Sorts active batches by `expirationDate` ascending.
    - Deducts iteratively, cascades across batches, and triggers `productRepo.saveToCsv()`.
  - `// BR19: Returns products with total stock <= 5`
    `public List<Food> getLowStockProducts()`
  - `// BR11.1: Returns all batches expired relative to today`
    `public List<Batch> getExpiredBatches(LocalDate today)`
  - `// BR20: Returns active batches expiring within 7 days`
    `public List<Batch> getNearExpiryBatches(LocalDate today)`
  - `public void updateProduct(String id, Double newPrice, Integer newQuantity)` (Task B2)
  - `public void replenishStock(String productId, String batchId, int addQty)` (Task B14)
  - `public List<Food> search(String keyword)` (Task B4)

#### Target Package: `fptu.pro192.foodstoremanagement.ui`

##### 2. `ProductMenu.java`

> [!NOTE]
> **Provisional / Pseudo Menu Specification**:
> The menu layout, titles, and numeric option choices shown below are tentative and for reference/pseudo-code only. They are not finalized and can be adjusted during implementation as needed, provided the 80-column width limit, Vietnamese language, clear screen on transition, and `[0]` to return/cancel are strictly maintained.

- **Extends:** `AbstractMenu`
- **Title:** `"QUẢN LÝ SẢN PHẨM & KHO HÀNG"`
- **Options Rendered (Tentative Draft / Pseudo):**

  ```text
  1. Thêm sản phẩm mới (Task B1)
  2. Cập nhật thông tin sản phẩm (Task B2)
  3. Xem danh sách tất cả sản phẩm (Task B3)
  4. Tìm kiếm sản phẩm theo tên / loại (Task B4)
  5. Xem sản phẩm tồn kho thấp (Task B11)
  6. Xem sản phẩm đã hết hạn (Task B12)
  7. Xem sản phẩm cận hạn sử dụng (Task B13)
  8. Cập nhật tồn kho / Nhập thêm hàng (Task B14)
  0. Quay lại Menu chính
  ```

- **Table Renderer:** Renders 80-column ASCII table as specified in Section 5.2.1 of Architecture Model.

---

### 🔗 Dependencies & System Impact

- **Depends On:** Task #1 (`AbstractMenu`, `TableFormatter`), Task #2 (`ProductRepository`, `Food`).
- **Unblocks:**
  - Task #7 (Tâm): `OrderService` will invoke `productService.deductStockFEFO(...)` during order finalization.
  - Task #9 (Phú): Attached as submenu 1 in `MainMenu`.

---

### ✅ Acceptance Criteria & Verification

- [ ] NetBeans 13 compiles with 0 errors and 0 warnings under Java 8.
- [ ] FEFO logic verified: When product has Batch A (expiring earlier, 5 units) and Batch B (expiring later, 5 units), selling 6 units empties Batch A completely and deducts 1 unit from Batch B.
- [ ] Attempting to sell more than total available active stock throws `InsufficientStockException` with a descriptive message (**BR10**).
- [ ] Expired batches are excluded from saleable stock and flagged in Task B12 view (**BR11**).
- [ ] 80-column verification: Product table renders with exact width 80 characters, no line wraps or misaligned border columns on terminal.
- [ ] PR created targeting `main`: `feat(product): implement FEFO deduction in ProductService and 80-col ProductMenu`.
