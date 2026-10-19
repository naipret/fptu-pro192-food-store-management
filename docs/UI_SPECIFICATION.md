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
| Currency and Number Format | `Locale.US` comma-separated integers for VND amounts (`8,000 VND`, `1,250,000 VND`) and plain integers (`int`) for stock and cart quantities |

---

## 2. Navigation and Input Conventions

### 2.1. Menu Selection and Exit Convention

Every menu rendered via `Menu.display()` lists numbered options starting from `[1]` through `[N]` and places `[0]` at the bottom to exit or return to the parent menu:

1. In `MainMenu`, selecting `[0]` exits the application cleanly.
2. In submenus (`ProductMenu`, `InventoryMenu`, `CustomerMenu`, `SalesMenu`, `ReportMenu`), selecting `[0]` returns immediately to `MainMenu`.
3. During multi-step data entry flows (such as adding a product, registering a customer, or adding items to a shopping cart), entering `0` at the primary prompt cancels the operation safely without mutating state.

### 2.2. Standardized Status Prefixes

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

### 4.1. Standard Menu Frame (`80` Columns)

```text
================================================================================
                         FOOD STORE MANAGEMENT SYSTEM
================================================================================
  [1] Product Catalog Management
  [2] Customer Management
  [3] Sales & Checkout Management
  [4] Inventory & Batch Management
  [5] Analytical & Revenue Reports
  [0] Exit Application
--------------------------------------------------------------------------------
Enter your choice [0-5]:
```

### 4.2. Product Catalog Table (`79` Columns)

Formula: `1 + (8) + 1 + (24) + 1 + (14) + 1 + (8) + 1 + (12) + 1 + (6) + 1 = 79` columns.

| Column Header | Content Width (`fit`) | Padded Cell Width | Alignment |
| :--- | :--- | :--- | :--- |
| `ID` | `6` (`P00001`) | `8` (`" P00001 "`) | Left |
| `Product Name` | `22` | `24` | Left |
| `Category` | `12` | `14` | Left |
| `Unit` | `6` | `8` | Left |
| `Price (VND)` | `10` | `12` | Right |
| `Stock` | `4` | `6` | Right |

```text
+--------+------------------------+--------------+--------+------------+------+
| ID     | Product Name           | Category     | Unit   | Price(VND) |Stock |
+--------+------------------------+--------------+--------+------------+------+
| P00001 | ST25 Fragrant Rice 5kg | Grains       | Bag    |    165,000 |   45 |
| P00002 | Vinamilk Fresh Milk 1L | Dairy        | Carton |     36,000 |   12 |
+--------+------------------------+--------------+--------+------------+------+
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

### 4.4. Customer Directory Table (`79` Columns)

Formula: `1 + (8) + 1 + (22) + 1 + (12) + 1 + (24) + 1 + (7) + 1 = 79` columns.

| Column Header | Content Width (`fit`) | Padded Cell Width | Description |
| :--- | :--- | :--- | :--- |
| `ID` | `6` (`C00001`) | `8` | Customer identifier |
| `Full Name` | `20` | `22` | Customer full name |
| `Phone` | `10` (`0901234567`) | `12` | Normalized 10-digit phone |
| `Address` | `22` | `24` | Residential address |
| `Tier` | `5` (`Reg`/`VIP`) | `7` | Loyalty tier |

### 4.5. Sales Cart and Invoice Table (`78` Columns)

Formula: `1 + (5) + 1 + (8) + 1 + (24) + 1 + (7) + 1 + (13) + 1 + (14) + 1 = 78` columns.

| Column Header | Content Width (`fit`) | Padded Cell Width | Description |
| :--- | :--- | :--- | :--- |
| `No.` | `3` | `5` | Line number (`1..N`) |
| `ProdID` | `6` (`P00001`) | `8` | Product identifier |
| `Product Name` | `22` | `24` | Product name |
| `Qty` | `5` | `7` | Integer quantity |
| `Unit Price` | `11` | `13` | Snapshot selling price |
| `Subtotal` | `12` | `14` | Line-item subtotal (`Qty * Unit Price`) |
