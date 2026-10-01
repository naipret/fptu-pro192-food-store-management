# [TASK #9] feat(core): implement MainMenu, wire application dependencies, and integration testing

### 👤 Ownership & Roles

- **Issue Number:** #9
- **Assignee:** Nguyễn Văn Phú
- **Reviewer:** All team members (Thành, Thiện, Tâm)
- **Target Module:** `core-infrastructure`
- **Deadline:** 14/10/2026 (Day 13)
- **Priority:** Blocker (P0)

---

### 🎯 Objective & Summary

Implement the root console navigation menu `MainMenu`, construct the application dependency wiring in `Main.java`, and execute end-to-end integration testing across all 4 feature modules.

This task delivers:

1. `MainMenu`: The primary navigation dashboard matching the SRS specifications, routing users seamlessly to `ProductMenu`, `CustomerMenu`, `SalesMenu`, Inventory Management, and `ReportMenu`.
2. `Main.java`: The application entry point responsible for manual dependency injection (Repositories $\to$ Services $\to$ Menus), graceful shutdown hooks, and top-level exception guards.
3. System-wide integration verification ensuring smooth transitions between screens without terminal state corruption.

---

### 📖 Required Reading Before Coding

1. SRS Specification: Section 1 (MAIN MENU layout and navigation options).
2. [`docs/ARCHITECTURE_MODEL.md`](../docs/ARCHITECTURE_MODEL.md):
   - **Section 1**: 3-Tier Layered MVC Architecture and manual dependency injection.
   - **Section 5**: 80-Column VT100 standard and navigation conventions.
3. [`docs/DESIGN_PATTERNS.md`](../docs/DESIGN_PATTERNS.md):
   - **Section 5**: Template Method Pattern (`AbstractMenu`).

---

### 💡 Conceptual Deep Dive (For Newbies)

1. **Pure Java 8 Dependency Injection (No Frameworks):**
   - Without Spring or Guice, the `main()` method acts as the application's composition root:

     ```markdown
     [ Repositories ] ──> Injected into ──> [ Services ] ──> Injected into ──> [ Menus ]
     ```

   - This ensures objects are instantiated once, dependencies are explicit, and components remain testable.
2. **Global Uncaught Exception Guard:**
   - A command-line program should never crash with a raw stack trace in front of an evaluator or user. The main execution loop is wrapped with a defensive `try-catch (Throwable t)` block that logs the incident and safely returns the user to the main menu.
3. **Consistent Screen Transitions:**
   - Every submenu return (`choice == 0`) returns execution control back to `MainMenu.display()`, which clears the screen and re-renders the clean dashboard.

---

### 🛠 Technical Specifications & Target Files

#### Target Package: `fptu.pro192.foodstoremanagement.ui`

##### 1. `MainMenu.java`

> [!NOTE]
> **Provisional / Pseudo Menu Specification**:
> The menu layout, titles, and numeric option choices shown below are tentative draft references (pseudo-design). They are not finalized and can be adjusted during implementation as needed, provided the 80-column width limit, Vietnamese language, clear screen on transition, and `[0]` to return/exit are strictly maintained.

- **Extends:** `AbstractMenu`
- **Title:** `"HỆ THỐNG QUẢN LÝ BÁN HÀNG THỰC PHẨM"`
- **Fields:**
  - `private final ProductMenu productMenu;`
  - `private final CustomerMenu customerMenu;`
  - `private final SalesMenu salesMenu;`
  - `private final ReportMenu reportMenu;`
- **Rendered Options (Tentative Draft / Pseudo per SRS Section 1):**

  ```text
  ================================================================================
                         HỆ THỐNG QUẢN LÝ BÁN HÀNG THỰC PHẨM
  ================================================================================
  1. Quản lý Sản phẩm Thực phẩm (Manage Food Products)
  2. Quản lý Khách hàng (Manage Customers)
  3. Quản lý Bán hàng & Tạo Đơn (Sales Management)
  4. Quản lý Tồn kho & Hạn Dùng (Inventory Management)
  5. Báo cáo & Thống kê Doanh thu (Reports)
  6. Thoát hệ thống (Exit)
  --------------------------------------------------------------------------------
  [0] Thoát | Chọn chức năng [1 - 6]: _
  --------------------------------------------------------------------------------
  ```

- **Option Handling:**
  - `1`: Calls `productMenu.display()`.
  - `2`: Calls `customerMenu.display()`.
  - `3`: Calls `salesMenu.display()`.
  - `4`: Calls inventory submenu or routes to product inventory features.
  - `5`: Calls `reportMenu.display()`.
  - `6` or `0`: Exits the application cleanly.

#### Target Package: `fptu.pro192.foodstoremanagement`

##### 2. `Main.java`

- **Class:** `public class Main`
- **Method:** `public static void main(String[] args)`
- **Execution Flow:**
  1. Ensures `data/` directory exists (`Files.createDirectories(Paths.get("data"))`).
  2. Instantiates repositories: `ProductRepository`, `CustomerRepository`, `OrderRepository`.
  3. Instantiates services: `ProductService`, `CustomerService`, `OrderService`, `ReportService`.
  4. Instantiates menus: `ProductMenu`, `CustomerMenu`, `SalesMenu`, `ReportMenu`, `MainMenu`.
  5. Executes `mainMenu.display()` wrapped in global exception handling.
  6. Prints warm exit farewell message.

---

### 🔗 Dependencies & System Impact

- **Depends On:** Tasks #1 through #8.
- **Unblocks:** Task #10 (Final audit and seed data).

---

### ✅ Acceptance Criteria & Verification

- [ ] NetBeans 13 compiles and runs `Main.java` with 0 errors and 0 warnings.
- [ ] Navigation flows seamlessly from Main Menu into any Submenu and returns back cleanly on input `0`.
- [ ] Invalid inputs (e.g., letters, numbers $> 6$) are rejected gracefully with user-friendly warnings.
- [ ] System handles empty data files or first-time runs without throwing NullPointerExceptions.
- [ ] All 80-column borders align uniformly across transitions.
- [ ] PR created targeting `main`: `feat(core): implement MainMenu, wire application dependencies, and integration testing`.
