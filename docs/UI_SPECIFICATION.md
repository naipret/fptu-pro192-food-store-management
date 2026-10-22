# Console User Interface Specification

This document defines the terminal layout constraints, localization policy, navigation conventions, string truncation rules, and 80-column ASCII table designs for all console views in `fptu.pro192.foodstoremanagement.view`.

---

## 1. Terminal Standards and Localization Policy

| Attribute | Standard |
| :--- | :--- |
| Maximum Line Width | Strictly `80` characters per line (`TableFormatter.SCREEN_WIDTH = 80`) |
| Border Character Set | Basic 7-bit ASCII characters only (`+`, `-`, `=`, `\|`, `*`). Box-drawing Unicode characters are prohibited to prevent garbled output in the NetBeans 13 Output window and Windows `cmd.exe`. |
| Interface Language | Full English across all menu titles, prompts, table headers, status messages, categories, and units |
| Proper Name Support | Full Unicode letter support (`\p{L}`) for user-entered product names, customer names, and addresses |
| Date Format | `dd/MM/yyyy` for all console prompts and table columns (converted to ISO `yyyy-MM-dd` only when reading/writing CSV files) |
| Currency and Number Format | `Locale.US` comma-separated integers amounts (`8,000`, `1,250,000`) and plain integers (`int`) for stock and cart quantities |

---

## 2. Navigation and Input Conventions

### 2.1. Menu Selection and Exit Convention

Every menu rendered via `Menu.display()` lists numbered options starting from `[1]` through `[N]` and places `[0]` at the bottom to exit or return to the parent menu:

1. In `MainMenu`, selecting `[0]` exits the application cleanly.
2. In submenus (`ProductMenu`, `InventoryMenu`, `CustomerMenu`, `SalesMenu`, `ReportMenu`), selecting `[0]` returns immediately to `MainMenu`.
3. During multi-step data entry flows (such as adding a product, registering a customer, or adding items to a shopping cart), entering `0` at the primary prompt cancels the operation safely without mutating state.

### 2.2. Screen Clearing and Breadcrumb Navigation

To maintain a clean and focused user experience during terminal interaction:

1. **Terminal Screen Reset (`ConsoleUtil.clearScreen()`):** Before rendering any menu frame and after completing major workflow actions, the terminal viewport is cleared using ANSI escape sequence `\033[H\033[2J` supplemented by a 25-blank-lines fallback to support legacy Windows `cmd.exe` and IDE console windows without ANSI support.
2. **Centered Breadcrumb Headers (`Menu.printBreadcrumbHeader(String)`):** Every dispatched submenu action renders an 80-column centered breadcrumb banner at the top of the screen before executing the selected handler. The banner follows the format `MAIN MENU > [<MenuIndex>] <MENU_NAME> > [<ChoiceIndex>] <Action Name>` (for example, `MAIN MENU > [1] PRODUCTS > [3] Add New Product`) and is centered horizontally using `TableFormatter.center(...)` without redundant prefix tags like `LOCATION:`.

### 2.3. Standardized Status Prefixes

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
   - `[1] View All Products` (lists all active food products and their available inventory)
   - `[2] Search Products` (universal multi-field query across ID, Name, Category, and Unit)
   - `[3] Add New Product` (prompts for name, category, unit, and price)
   - `[4] Update Product` (allows updating name, category, unit, price with Enter to keep current)
   - `[5] Remove Product` (soft-deletes product after confirmation)
   - `[0] Back to Main Menu`

2. **Customers Menu (`CustomerMenu`):**
   - `[1] View All Customers` (lists all registered customers and their tiers)
   - `[2] Search Customers` (universal multi-field query across ID, Full Name, Phone, and Address)
   - `[3] Register New Customer` (prompts for full name, phone, address, and loyalty tier)
   - `[4] Update Customer Information` (allows updating name, phone, address, tier with Enter to keep current)
   - `[5] Remove Customer` (soft-deletes customer after confirmation)
   - `[0] Back to Main Menu`

3. **Sales Menu (`SalesMenu`):**
   - `[1] New Sale (Checkout)` (prompts for customer ID or Guest; interactive cart with FEFO stock deduction)
   - `[2] View All Sales Orders` (lists all completed sales orders in 80-column summary format)
   - `[3] Search Sales Orders` (universal query across Order ID, Customer ID, and Date patterns)
   - `[4] View Order Invoice by ID` (displays itemized 80-column invoice breakdown)
   - `[0] Back to Main Menu`

4. **Inventory Menu (`InventoryMenu`):**
   - `[1] Import New Batch for Product`
   - `[2] Replenish Existing Batch Quantity (+Stock)`
   - `[3] Adjust Existing Batch Quantity (Set Exact Stock)`
   - `[4] View All Batches of a Product`
   - `[5] View Low-Stock Products Alert`
   - `[6] View Near-Expiry Batches Alert`
   - `[7] View Expired Batches Report`
   - `[0] Back to Main Menu`

5. **Reports Menu (`ReportMenu`):**
   - `[1] Daily Revenue Report (by Date)`
   - `[2] Monthly Sales & Revenue Report (by Month/Year)`
   - `[3] Best-Selling Products Ranking` (supports optional From Date and To Date period filtering per BR21)
   - `[4] Customer Spending Summary Ranking` (supports optional From Date and To Date period filtering per BR22)
   - `[0] Back to Main Menu`

### 5.2. Universal Multi-Field Search Specification

To provide maximum usability for grocery store clerks:

- **Product Search:** Case-insensitive substring matching against Product ID, Product Name, Category, or Measurement Unit.
- **Customer Search:** Case-insensitive substring matching against Customer ID, Full Name, Phone Number, or Residential Address (enabling lookups by city or street such as `Quảng Ngãi` or `Lê Lợi`).
- **Order Search:** Substring matching against Order ID, Customer ID, or Order Date representations (e.g. `2026`, `10/2026`, `19/10/2026`).
