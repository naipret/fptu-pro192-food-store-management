# [TASK #10] chore(release): final clean code audit, seed realistic demo data, and presentation dry run

### 👤 Ownership & Roles

- **Issue Number:** #10
- **Assignee:** All Team Members (Nguyễn Văn Phú, Trần Cao Thành, Lê Trí Thiện, Nguyễn Công Tâm)
- **Reviewer:** Nguyễn Văn Phú (Leader)
- **Target Module:** Project-wide
- **Deadline:** 15/10/2026 (Day 14)
- **Priority:** Blocker (P0 - Final Milestone)

---

### 🎯 Objective & Summary

Perform comprehensive project quality assurance, execute the final Clean Code and Javadoc audit, populate realistic mock CSV datasets in `data/*.csv` calibrated to current demo dates, verify NetBeans 13 Clean & Build artifacts, and conduct an oral defense dry run for the PRO192 final evaluation.

This milestone ensures:

1. Complete adherence to [CONTRIBUTING.md](../CONTRIBUTING.md) and [docs/](../docs/) guidelines.
2. 100% in-code Business Rules (`// BR-*:`) traceability across all service and domain methods.
3. Realistic demo datasets in `data/products.csv`, `data/batches.csv`, `data/customers.csv`, `data/orders.csv`, and `data/order_details.csv`.
4. NetBeans 13 Ant build produces a standalone executable JAR (`dist/fptu-pro192-food-store-management.jar`).
5. Rehearsal of the team presentation and Q&A defense.

---

### 📖 Required Reading Before Auditing

1. [`CONTRIBUTING.md`](../CONTRIBUTING.md): Sections 2, 3, 5 (Commit standards, in-code BR documentation, coding standards).
2. [`docs/ARCHITECTURE_MODEL.md`](../docs/ARCHITECTURE_MODEL.md): Section 1.3 (Oral defense presentation pitch).
3. [`docs/DESIGN_PATTERNS.md`](../docs/DESIGN_PATTERNS.md): All 5 design pattern justifications.
4. [`docs/DESIGN_PRINCIPLES.md`](../docs/DESIGN_PRINCIPLES.md): SOLID mapping matrix.

---

### 🛠 Execution Checklist & Deliverables

#### 1. Clean Code & Formatting Audit

- [ ] Format every `.java` file in NetBeans (`Alt + Shift + F`) to enforce 4-space indentation and standard brace placement.
- [ ] Verify naming conventions:
  - Classes / Interfaces: `PascalCase` (`Food`, `ProductRepository`, `ScannerManager`).
  - Methods / Variables: `camelCase` (`deductStockFEFO`, `unitPrice`).
  - Constants: `UPPER_SNAKE_CASE` (`SCREEN_WIDTH`, `DEFAULT_WARNING_DAYS`).
- [ ] Ensure **zero compilation warnings** (`javac -Xlint:all`).
- [ ] Verify no Java 9+ features exist (no `var`, no `List.of()`, no `Map.of()`).

#### 2. In-Code Business Rules (BR) Traceability Check

- [ ] Verify that every method enforcing business rules contains mandatory `// BR-*:` comments:
  - `ProductService`: BR1, BR3, BR4, BR5, BR6, BR7, BR10, BR11, BR14, BR19, BR20, BR24, BR26, BR27.
  - `CustomerService`: BR2, BR15, BR16, BR18, BR25, BR27.
  - `OrderService`: BR8, BR9, BR10, BR11, BR12, BR13, BR14, BR15, BR16, BR17, BR23, BR24.
  - `ReportService`: BR19, BR21, BR22, BR23.

#### 3. Seed Realistic Demo Data (`data/*.csv`)

Populate files with rich, realistic data calibrated to the current week:

- **`data/products.csv`**: At least 10 items spanning all 3 categories (`FROZEN`, `CHILLED`, `DRY`).
- **`data/batches.csv`**:
  - Batches with active shelf life.
  - At least 2 batches that are already **EXPIRED** (to showcase Task B12 and BR11 prevention).
  - At least 2 batches that are **NEAR EXPIRY** within 1–7 days (to showcase Task B13 and BR20 warnings).
  - At least 2 products with **LOW STOCK $\le 5$ units** (to showcase Task B11 and BR19).
- **`data/customers.csv`**: At least 5 customers, including both `Regular` and `VIP` tiers, with normalized phone numbers.
- **`data/orders.csv` & `data/order_details.csv`**: At least 5 completed historical transactions to populate monthly reports and best-seller charts immediately upon startup.

#### 4. NetBeans 13 Ant Build Verification

- Execute **Clean and Build** in NetBeans 13 or run:

  ```powershell
  ant clean compile jar
  ```

- Run the packaged JAR from terminal to verify standalone execution:

  ```powershell
  java -jar dist/fptu-pro192-food-store-management.jar
  ```

#### 5. Oral Defense Rehearsal (Dry Run)

Divide defense topics among the 4 members:

- **Nguyễn Văn Phú (Leader):** Presents Architecture (Layered MVC, 3-Tier Layering, preventing Fat Controller, Singleton, Template Method).
- **Trần Cao Thành:** Presents Product Hierarchy, Polymorphic HACCP storage instructions, Batch aggregation, and FEFO inventory deduction algorithm (**BR24**).
- **Lê Trí Thiện:** Presents Customer Hierarchy, Strategy Pattern for VIP discounts, Regex phone normalization (**BR25**), and soft-delete rationale (**BR27**).
- **Nguyễn Công Tâm:** Presents Sales Order lifecycle (Draft cart vs Completed sale), Price snapshotting in `OrderDetail`, and Analytical reporting queries (**BR21**, **BR22**, **BR23**).

---

### ✅ Final Acceptance Criteria

- [ ] All 9 previous feature branches are merged into `main` via reviewed Pull Requests.
- [ ] Project builds cleanly from source with `ant compile`.
- [ ] Application starts, runs, and terminates with 0 exceptions.
- [ ] Demo data allows seamless demonstration of all SRS tasks (B1 through B18) in under 10 minutes.
