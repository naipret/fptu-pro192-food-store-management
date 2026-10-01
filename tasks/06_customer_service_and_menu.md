# [TASK #6] feat(customer): implement CustomerService and 80-col CustomerMenu

### 👤 Ownership & Roles

- **Issue Number:** #6
- **Assignee:** Lê Trí Thiện
- **Reviewer:** Nguyễn Văn Phú
- **Target Module:** `customer-management`
- **Deadline:** 11/10/2026 (Day 10)
- **Priority:** High (P1)

---

### 🎯 Objective & Summary

Implement the `CustomerService` layer enforcing customer management business rules (validation, phone normalization, tier assignment, soft deletion) and the terminal presentation menu `CustomerMenu` conforming to the fixed 80-column ASCII standard (**Tasks B5–B7**).

This task delivers:

1. `CustomerService`: Handles customer registration (**BR2**, **BR18**, **BR25**), selective updates, soft deletion (**BR27**), and keyword search.
2. `CustomerMenu`: Concrete subclass of `AbstractMenu` providing interactive console UI for adding, modifying, searching, and viewing customer accounts.

---

### 📖 Required Reading Before Coding

1. [`docs/BUSINESS_RULES.md`](../docs/BUSINESS_RULES.md):
   - **BR2**: Customer ID format `C00001`–`C99999` (unique and immutable).
   - **BR15 & BR16**: Regular (0%) and VIP (10%) discount tiers.
   - **BR18**: User inputs must be validated prior to persistence.
   - **BR25**: Phone normalization (converts variants to 10 digits starting with `0`).
   - **BR27**: Soft-delete policy (`isDeleted = true`).
2. [`docs/ARCHITECTURE_MODEL.md`](../docs/ARCHITECTURE_MODEL.md):
   - **Section 5.1**: Text Truncation (`TableFormatter.fit(text, width)`).
   - **Section 5.2.2**: Exact 80-Column Customer Table specification.
3. [`docs/DESIGN_PATTERNS.md`](../docs/DESIGN_PATTERNS.md):
   - **Section 5**: Template Method Pattern (`AbstractMenu`).

---

### 💡 Conceptual Deep Dive (For Newbies)

1. **Selective Updates (Task B6):**
   - When updating customer information (Task B6), the UI prompts for new phone and address with the instruction `(leave blank to skip)`. If the user presses `ENTER` without entering text, the existing value must be retained rather than replaced with an empty string.
2. **Phone Number Re-validation during Update:**
   - If a new phone number is provided during update, it must undergo the exact same normalization (**BR25**) and duplicate phone check before being committed to the repository.
3. **Table Column Allocation (80 Columns Total):**
   `| Mã KH (6) | Họ Và Tên (25) | Số ĐT (12) | Địa Chỉ (15) | Hạng (6) |`
   With 6 borders `|` and 10 spaces padding ($64 + 16 = 80$). Full name and address must be wrapped with `TableFormatter.fit(name, 25)` and `TableFormatter.fit(address, 15)`.

---

### 🛠 Technical Specifications & Target Files

#### Target Package: `fptu.pro192.foodstoremanagement.service`

##### 1. `CustomerService.java`

- **Fields:** `private final CustomerRepository customerRepo;`
- **Methods:**
  - `// BR2, BR18, BR25: Add customer with phone normalization and duplicate checks`
    `public void addCustomer(String id, String fullName, String rawPhone, String address, String type)`:
    - Validates `id` against `^C\\d{5}$` and checks `customerRepo.findById(id) == null`.
    - Normalizes phone via `InputValidator.normalizePhone(rawPhone)`.
    - Checks no active customer already shares this phone number.
    - Creates customer instance via `CustomerFactory.create()` and saves to repository.
  - `// Task B6: Selective update`
    `public void updateCustomer(String id, String newPhone, String newAddress)`:
    - If `newPhone != null && !newPhone.trim().isEmpty()`, normalizes and updates phone.
    - If `newAddress != null && !newAddress.trim().isEmpty()`, updates address.
    - Calls `customerRepo.saveToCsv()`.
  - `// BR27: Soft delete`
    `public void deleteCustomer(String id)`:
    - Finds customer and marks `setDeleted(true)`.
  - `public List<Customer> getAllActive()`: Returns list of non-deleted customers.
  - `public List<Customer> search(String keyword)`: Matches by name (case-insensitive substring) or phone number.

#### Target Package: `fptu.pro192.foodstoremanagement.ui`

##### 2. `CustomerMenu.java`

> [!NOTE]
> **Provisional / Pseudo Menu Specification**:
> The menu layout, titles, and numeric option choices shown below are tentative and for reference/pseudo-code only. They are not finalized and can be adjusted during implementation as needed, provided the 80-column width limit, Vietnamese language, clear screen on transition, and `[0]` to return/cancel are strictly maintained.

- **Extends:** `AbstractMenu`
- **Title:** `"QUẢN LÝ KHÁCH HÀNG"`
- **Options Rendered (Tentative Draft / Pseudo):**

  ```text
  1. Thêm khách hàng mới (Task B5)
  2. Cập nhật thông tin khách hàng (Task B6)
  3. Xem danh sách khách hàng (Task B7)
  4. Tìm kiếm khách hàng theo Tên / SĐT
  5. Xóa khách hàng (Soft Delete)
  0. Quay lại Menu chính
  ```

- **Table Renderer:** Renders 80-column ASCII table per Section 5.2.2.

---

### 🔗 Dependencies & System Impact

- **Depends On:** Task #1 (`AbstractMenu`, `ConsoleUtil`), Task #3 (`CustomerRepository`, `InputValidator`).
- **Unblocks:**
  - Task #7 (Tâm): Sales checkout flow selects customers from `CustomerService`.
  - Task #9 (Phú): Attached as submenu 2 in `MainMenu`.

---

### ✅ Acceptance Criteria & Verification

- [ ] NetBeans 13 compiles with 0 errors and 0 warnings under Java 8.
- [ ] Adding customer with phone `+84 901 234 567` stores `0901234567` (**BR25**).
- [ ] Attempting to add an existing customer ID or invalid phone format displays a clear error and rejects creation (**BR2**, **BR18**).
- [ ] Updating with blank inputs retains existing values without overwriting them.
- [ ] Soft deletion marks record deleted without removing it from `data/customers.csv` (**BR27**).
- [ ] Customer table matches exact 80-column width with zero alignment distortion.
- [ ] PR created targeting `main`: `feat(customer): implement CustomerService and 80-col CustomerMenu`.
