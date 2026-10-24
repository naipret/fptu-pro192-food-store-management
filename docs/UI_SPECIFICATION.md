# Console User Interface Specification

This document defines the terminal layout constraints, localization policy, navigation conventions, string truncation rules, and 80-column ASCII table designs for all console views in `fptu.pro192.foodstoremanagement.view`.

---

## 1. Terminal Standards and Localization Policy

| Attribute | Standard |
| :--- | :--- |
| Maximum Line Width | Strictly `80` characters per line (`TableFormatter.SCREEN_WIDTH = 80`) |
| Border Character Set | Basic 7-bit ASCII characters only (`+`, `-`, `=`, `\|`, `*`). Box-drawing Unicode characters are prohibited to prevent garbled output in the NetBeans 13 Output window and Windows `cmd.exe`. |
| Interface Language | Full English across all menu titles, prompts, table headers, status messages, categories, and units |
| Character Set Guarantee | Pure 100% 7-bit ASCII text across all prompts, tables, seeded CSVs, customer names, product names, and addresses. Immune to console code page mismatches (CP437, UTF-8, Windows CMD, NetBeans, PowerShell, macOS, Linux). |
| Date Format | `dd/MM/yyyy` for all console prompts and table columns (converted to ISO `yyyy-MM-dd` only when reading/writing CSV files) |
| Currency and Number Format | `Locale.US` comma-separated integers amounts (`8,000`, `1,250,000`) and plain integers (`int`) for stock and cart quantities |

---

## 2. Navigation and Input Conventions

### 2.1. Menu Selection and Universal Cancellation

Every menu rendered via `Menu.display()` lists numbered options starting from `[1]` through `[N]` and places `[0]` at the bottom to exit or return to the parent menu:

1. In `MainMenu`, selecting `[0]` exits the application cleanly.
2. In submenus (`ProductMenu`, `InventoryMenu`, `CustomerMenu`, `SalesMenu`, `ReportMenu`), selecting `[0]` returns immediately to `MainMenu`.
3. **Universal Cancellation with `0`:** Across all input fields in all menus (entity addition, update, batch replenishment, inventory adjustment, date prompts, search queries), entering `0` immediately cancels the ongoing operation and safely returns to the menu without modifying any data.

### 2.2. Default Values and Enter-to-Keep Convention

1. **Update Workflows (`1.4`, `2.4`):** When modifying existing records, pressing `[Enter]` without typing any input retains the current value for that field. Current category and customer tier are displayed on a dedicated line above choice options supporting `Enter to keep, or 0 to cancel`.
2. **Report Date/Period Defaults (`5.1`, `5.2`):**
   - In Daily Revenue (`5.1`), pressing `[Enter]` defaults to today's date (`dd/MM/yyyy`).
   - In Monthly Revenue (`5.2`), user inputs `MM/yyyy` directly (no separate month/year prompts), with `[Enter]` defaulting to the current month.
3. **Report & Alert Threshold Defaults:**
   - In Low Stock (`4.5`), pressing `[Enter]` defaults to threshold `5`.
   - In Near-Expiry (`4.6`), pressing `[Enter]` defaults to window `7` days.
   - In Expired Batches (`4.7`) and Rankings (`5.3`, `5.4`), pressing `[Enter]` skips date filters to include all records.

### 2.3. System-Managed Identifiers

All entity identifiers (`P00001` for products, `C00001` for customers, `B00001` for batches, `O00001` for orders) are generated automatically and sequentially by controllers (`BR1`, `BR2`, `BR6`, `BR8`, `BR33`). Users are never prompted to enter or invent IDs when creating new items.

### 2.4. Dual Table Verification and [Y/N] Confirmation

1. **Pre- and Post-Modification ASCII Tables:** In all update and adjustment flows (`1.4`, `1.5`, `2.4`, `2.5`, `4.2`, `4.3`), the system displays the current entity state in a formatted 80-column ASCII table before editing, and displays the refreshed table immediately after the change is persisted.
2. **Explicit Confirmation (`ConsoleUtil.confirm`):** All create, update, adjust, remove, and checkout actions require an explicit `[Y/N]` confirmation step (`Are you sure ...? [Y/N]:`) before persisting changes.

### 2.5. Continuous Search and Detail Inspection Loops

Search screens (`1.2`, `2.2`, `3.3`) and detail viewers (`3.4` view invoice by ID, `4.4` view batches of product) loop continuously until `0` is entered to exit. They disable the redundant post-action pause prompt (`shouldPauseAfterAction(choice) == false`) for fluid, seamless user experience.

### 2.6. Screen Clearing and Breadcrumb Navigation

To maintain a clean and focused user experience during terminal interaction:

1. **Terminal Screen Reset (`ConsoleUtil.clearScreen()`):** Before rendering any menu frame and after completing major workflow actions, the terminal viewport is cleared using ANSI escape sequence `\033[H\033[2J` supplemented by a 25-blank-lines fallback to support legacy Windows `cmd.exe` and IDE console windows without ANSI support.
2. **Centered Breadcrumb Headers (`Menu.printBreadcrumbHeader(String)`):** Every dispatched submenu action renders an 80-column centered breadcrumb banner at the top of the screen before executing the selected handler. The banner follows the format `MAIN MENU > [<MenuIndex>] <MENU_NAME> > [<ChoiceIndex>] <Action Name>` (for example, `MAIN MENU > [1] PRODUCTS > [3] Add New Product`) and is centered horizontally using `TableFormatter.center(...)` without redundant prefix tags like `LOCATION:`.

### 2.7. Standardized Status Prefixes

All user feedback messages printed to the console use explicit ASCII status prefixes:

| Prefix | Meaning | Example |
| :--- | :--- | :--- |
| `[OK]` | Operation completed and persisted successfully | `[OK] Product P00001 added successfully.` |
| `[ERROR]` | Validation failure or business rule violation | `[ERROR] Price must be greater than zero.` |
| `[WARN]` | Non-fatal warning or threshold alert | `[WARN] Product P00003 is low on stock (3 units remaining).` |
| `[INFO]` | Neutral informational message or empty result notice | `[INFO] Operation cancelled. Returning to menu.` |

---

## 3. String Truncation and Alignment Rules

To prevent long product names, customer names, or addresses from wrapping past column 80 and breaking table borders, all dynamic cell values must pass through `TableFormatter`:

| Method | Behavior |
| :--- | :--- |
| `TableFormatter.fit(String text, int width)` | Trims leading/trailing whitespace. If `text.length() <= width`, right-pads with spaces to exactly `width` characters. If `text.length() > width` and `width > 2`, truncates the string to `width - 2` characters and appends `..` so the returned string is always exactly `width` characters long. |
| `TableFormatter.center(String text, int width)` | Centers `text` within `width` characters using balanced left and right space padding, truncating via `fit` if `text` exceeds `width`. |
| `TableFormatter.repeat(char ch, int count)` | Generates a horizontal border line of `count` repeated ASCII characters (`=` or `-`). |

---

## 4. 80-Column Table Width Layouts

Every ASCII table is constructed so the sum of its column content widths plus border separators (`|` and `|`) never exceeds `80` columns.

### 4.1. Standard Menu Frames (`80` Columns)

#### Root Main Menu

```text
================================================================================
                          FOOD STORE MANAGEMENT SYSTEM
================================================================================
  [1] Products
  [2] Customers
  [3] Sales
  [4] Inventory
  [5] Reports
  [0] Exit
--------------------------------------------------------------------------------
Enter your choice [0-5]:
```

#### Level-2 Submenu (Hierarchical Breadcrumb Header)

```text
================================================================================
                            MAIN MENU > [1] PRODUCTS
================================================================================
  [1] View All Products
  [2] Search Products
  [3] Add New Product
  [4] Update Product Information
  [5] Remove Product
  [0] Back to Main Menu
--------------------------------------------------------------------------------
Enter your choice [0-5]:
```

### 4.2. Product Catalog Table (`80` Columns)

Formula: `1 + (8) + 1 + (24) + 1 + (14) + 1 + (8) + 1 + (12) + 1 + (7) + 1 = 80` columns.

| Column Header | Content Width (`fit`) | Padded Cell Width | Alignment |
| :--- | :--- | :--- | :--- |
| `ID` | `6` (`P00001`) | `8` (`" P00001 "`) | Left |
| `Product Name` | `22` | `24` | Left |
| `Category` | `12` | `14` | Left |
| `Unit` | `6` | `8` | Left |
| `Price` | `10` | `12` | Right |
| `Stock` | `5` | `7` | Right |

```text
+--------+------------------------+--------------+--------+------------+-------+
| ID     | Product Name           | Category     | Unit   | Price      | Stock |
+--------+------------------------+--------------+--------+------------+-------+
| P00001 | ST25 Fragrant Rice 5kg | Grains       | Bag    |    165,000 |    45 |
| P00002 | Vinamilk Fresh Milk 1L | Dairy        | Carton |     36,000 |    12 |
+--------+------------------------+--------------+--------+------------+-------+
```

### 4.3. Batch and Expiry Table (`80` Columns)

Formula: `1 + (8) + 1 + (8) + 1 + (18) + 1 + (12) + 1 + (12) + 1 + (7) + 1 + (8) + 1 = 80` columns.

| Column Header | Content Width (`fit`) | Padded Cell Width | Description |
| :--- | :--- | :--- | :--- |
| `Batch` | `6` (`B00001`) | `8` | Batch identifier |
| `ProdID` | `6` (`P00001`) | `8` | Product identifier |
| `Product Name` | `16` | `18` | Truncated product name |
| `Prod Date` | `10` (`dd/MM/yyyy`) | `12` | Production date |
| `Exp Date` | `10` (`dd/MM/yyyy`) | `12` | Expiration date |
| `Qty` | `5` | `7` | Remaining batch quantity |
| `Status` | `6` (`ACTIVE`/`NEAR`/`EXPRD`) | `8` | Shelf-life status |

### 4.4. Customer Directory Table (`80` Columns)

Formula: `1 + (8) + 1 + (21) + 1 + (12) + 1 + (24) + 1 + (9) + 1 = 80` columns.

| Column Header | Content Width (`fit`) | Padded Cell Width | Description |
| :--- | :--- | :--- | :--- |
| `ID` | `6` (`C00001`) | `8` | Customer identifier |
| `Full Name` | `19` | `21` | Customer full name |
| `Phone` | `10` (`0901234567`) | `12` | Normalized 10-digit phone |
| `Address` | `22` | `24` | Residential address |
| `Tier` | `7` (`Regular`/`VIP`) | `9` | Loyalty tier (untruncated) |

### 4.5. Sales Cart and Invoice Table (`80` Columns)

Formula: `1 + (5) + 1 + (8) + 1 + (25) + 1 + (7) + 1 + (13) + 1 + (15) + 1 = 80` columns.

| Column Header | Content Width (`fit`) | Padded Cell Width | Description |
| :--- | :--- | :--- | :--- |
| `No.` | `3` | `5` | Line number (`1..N`) |
| `ProdID` | `6` (`P00001`) | `8` | Product identifier |
| `Product Name` | `23` | `25` | Product name |
| `Qty` | `5` | `7` | Integer quantity |
| `Unit Price` | `11` | `13` | Snapshot selling price |
| `Subtotal` | `13` | `15` | Line-item subtotal (`Qty * Unit Price`) |

### 4.6. Sales Summary Table (`80` Columns)

Formula: `1 + (10) + 1 + (10) + 1 + (12) + 1 + (13) + 1 + (13) + 1 + (15) + 1 = 80` columns.

| Column Header | Content Width (`fit`) | Padded Cell Width | Description |
| :--- | :--- | :--- | :--- |
| `OrdID` | `8` | `10` | Order identifier |
| `CustID` | `8` | `10` | Customer identifier |
| `Date` | `10` | `12` | Order date (`dd/MM/yyyy`) |
| `Gross` | `11` | `13` | Gross amount before discount |
| `Discount` | `11` | `13` | Loyalty discount amount |
| `Final` | `13` | `15` | Net finalized payment |

---

## 5. Submenu Hierarchies and Universal Search

### 5.1. Streamlined Menu Hierarchies

1. **Products Menu (`ProductMenu`):**
   - `[1] Add New Product` (prompts name, category, unit, price with continuous loop)
   - `[2] View All Products` (table listing with live keyword search and Enter refresh loop)
   - `[3] Update Product Information` (lookup by ID, Enter to keep current values, continuous loop)
   - `[4] Remove Product` (lookup by ID, soft-deletes after confirmation, continuous loop)
   - `[0] Back to Main Menu`

2. **Customers Menu (`CustomerMenu`):**
   - `[1] Register New Customer` (prompts full name in Title Case, phone, address, tier with continuous loop)
   - `[2] View All Customers` (directory table with live keyword search across all fields and Enter refresh loop)
   - `[3] Update Customer Information` (lookup by ID, Enter to keep current values, continuous loop)
   - `[4] Remove Customer` (lookup by ID, soft-deletes after confirmation, continuous loop)
   - `[0] Back to Main Menu`

3. **Sales Menu (`SalesMenu`):**
   - `[1] New Sale (Checkout)` (prompts Customer ID or Enter for Guest; Live Dashboard displaying Cart Table at top, Add to Cart, Complete Checkout, Cancel)
   - `[2] View All Sales Orders` (summary table with smart Order ID auto-expansion into full invoice details, keyword search, Enter refresh loop)
   - `[0] Back to Main Menu`

4. **Inventory Menu (`InventoryMenu`):**
   - `[1] Import New Batch for Product` (prompts Product ID, production/expiration dates, quantity with continuous loop)
   - `[2] Adjust Existing Batch Stock` (lookup batch across products, set exact non-negative stock with confirmation)
   - `[3] View All Batches` (all active batches table with keyword and Product ID inspection loop)
   - `[4] Alert Low-Stock Products` (interactive threshold prompt with default 5)
   - `[5] Alert Near-Expiry Batches` (interactive warning window prompt with default 7 days)
   - `[6] Alert Expired Batches` (lists expired batches with look-back filter)
   - `[0] Back to Main Menu`

5. **Reports Menu (`ReportMenu`):**
   - `[1] Daily Sales & Revenue Report` (prompts date with Enter defaulting to today)
   - `[2] Monthly Sales & Revenue Report` (prompts month/year with Enter defaulting to current month)
   - `[3] Top-Selling Products Ranking` (supports optional period filtering and top N limit with default 5)
   - `[4] Top Customers by Spending` (supports optional period filtering and top N limit with default 5)
   - `[0] Back to Main Menu`

### 5.2. Universal Multi-Field Search Specification

To provide maximum usability for grocery store clerks:

- **Product Search:** Case-insensitive substring matching against Product ID, Product Name, Category, or Measurement Unit. Supports shorthand ID normalization (e.g., `P1` or `1` matches `P00001`). Prompts loop continuously until `0` is entered to return to the menu.
- **Customer Search:** Case-insensitive substring matching against Customer ID, Full Name, Phone Number, or Residential Address (e.g., `HCM` or `Le Loi`). Supports shorthand ID normalization (e.g., `C1` or `1` matches `C00001`). Prompts loop continuously until `0` is entered to return to the menu.
- **Order Search:** Substring matching against Order ID, Customer ID, Customer Name, or Order Date representations (e.g. `2026`, `10/2026`, `19/10/2026`). Smart Order ID detection automatically expands and displays the full itemized invoice table when shorthand or canonical Order ID is provided (`4`, `O4`, or `O00004`). Prompts loop continuously until `0` is entered to return to the menu.
