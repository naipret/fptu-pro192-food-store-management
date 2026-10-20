# Food Store Management System (PRO192)

A console-based retail food store management application built with Java SE 8 for the `PRO192` (Object-Oriented Programming) course at FPT University. The system manages grocery product catalogs, batch-level inventory with First-Expired-First-Out (`FEFO`) stock deduction, customer loyalty tiers, point-of-sale checkout, and analytical revenue reporting.

---

## 1. Technical Stack and Constraints

| Component | Specification |
| :--- | :--- |
| Language | Java SE 8 (`javac.source=1.8`, `javac.target=1.8`) |
| Primary IDE | Apache NetBeans IDE 13 (Java with Ant `j2seproject`) |
| Dependencies | Standard JDK 8 library only (no external third-party JARs) |
| User Interface | 80-column VT100 ASCII Console Text User Interface (English) |
| Data Persistence | RFC 4180 CSV flat files (`data/*.csv`) with atomic file replacement |

---

## 2. Project Structure

The codebase follows a standard 3-tier Model-View-Controller (`MVC`) architecture supplemented by a stateless utility package:

```text
fptu-pro192-foodstore-management/
├── build.xml                                  # Apache Ant build script (imports nbproject/build-impl.xml)
├── manifest.mf                                # JAR manifest configuration
├── CONTRIBUTING.md                            # Git branching, commit, issue, and PR workflow
├── README.md                                  # Project overview and quick start guide
├── nbproject/                                 # NetBeans 13 project metadata
├── scripts/                                   # Standalone dataset generation scripts
│   ├── seed.ps1                               # PowerShell script generating dynamic relative-date demo CSVs
│   └── seed.sh                                # Bash script generating dynamic relative-date demo CSVs
├── data/                                      # Runtime CSV storage files (auto-initialized with headers)
│   ├── products.csv                           # Retail product catalog records
│   ├── batches.csv                            # Inventory batch records linked to products
│   ├── customers.csv                          # Registered Regular and VIP customer records
│   ├── orders.csv                             # Completed sales order headers
│   └── order_details.csv                      # Line-item snapshots for completed orders
├── docs/                                      # Architectural and engineering specifications
│   ├── BUSINESS_RULES.md                      # Domain validation and operational rules (BR1-BR27)
│   ├── ARCHITECTURE_MODEL.md                  # 3-Tier MVC architecture, UML diagram, and CSV schemas
│   ├── DESIGN_PRINCIPLES.md                   # OOP pillars, SOLID, SoC, DRY, KISS, and YAGNI
│   ├── DESIGN_PATTERNS.md                     # GoF and persistence patterns applied in code
│   ├── UI_SPECIFICATION.md                    # 80-column ASCII terminal standards and table layouts
│   ├── NAMING_CONVENTIONS.md                  # Identifier and architectural naming rules
│   └── CLEAN_CODE.md                          # Defensive coding, exception handling, and Javadoc rules
├── .github/
│   ├── workflows/                             # GitHub Actions CI/CD workflows
│   └── pages/                                 # Web Terminal assets for GitHub Pages deployment
├── src/main/java/fptu/pro192/foodstoremanagement/
│   ├── Main.java                              # Application entry point and dependency wiring
│   ├── model/                                 # Domain entities (Product, Batch, Customer, Order, OrderDetail)
│   ├── controller/                            # Business logic, Set/Map/List registries, and CSV persistence
│   ├── view/                                  # 80-column interactive console menus (Menu hierarchy)
│   └── util/                                  # Stateless utilities (CsvUtil, InputValidator, ConsoleUtil, etc.)
└── src/test/java/fptu/pro192/foodstoremanagement/
    └── ...                                    # Automated verification test suites
```

---

## 3. Build, Test, and Run Instructions

### Option A: Using Apache NetBeans IDE 13

1. Open Apache NetBeans IDE 13 and register a Java 8 platform (`Tools` -> `Java Platforms` -> `JDK 1.8`).
2. Select `File` -> `Open Project` and choose the repository root directory.
3. Press `Shift + F11` (`Clean and Build Project`) to compile all sources and package `dist/fptu-pro192-food-store-management.jar`.
4. Press `F6` (`Run Project`) to launch the interactive console application, or `Alt + F6` (`Test Project`) to run the verification suite.

### Option B: Using Apache Ant from the Command Line

Ensure `JAVA_HOME` points to a valid JDK 8 installation before running Ant:

```powershell
# Clean, compile, run tests, and package executable JAR
ant clean compile test jar

# Launch the packaged application
java -jar dist/fptu-pro192-food-store-management.jar
```

### Option C: Generating Realistic Demo Data (Optional)

The application starts up cleanly with empty header-only CSVs automatically. To populate realistic demo datasets with dynamic dates relative to today (active stock, near-expiry alerts, expired batches, and sample invoices):

```powershell
# PowerShell (Windows)
powershell -ExecutionPolicy Bypass -File scripts/seed.ps1

# Bash (Linux / macOS / WSL)
./scripts/seed.sh
```

### Option D: Live Interactive Web Terminal (GitHub Pages)

Test and interact with the application directly inside your web browser without installing Java or NetBeans locally:

- **Live Web Terminal**: [https://naipret.github.io/fptu-pro192-food-store-management/](https://naipret.github.io/fptu-pro192-food-store-management/)
- Built with **CheerpJ 3.0** WebAssembly runtime and **xterm.js** terminal emulator.
- Automated continuous delivery via GitHub Actions on every push to `main`.

---

## 4. Documentation Index

All architectural decisions, domain rules, and coding standards are maintained in the [`docs/`](docs/) directory:

1. [Business Rules (`docs/BUSINESS_RULES.md`)](docs/BUSINESS_RULES.md) defines rules `BR1` through `BR27` governing product catalog management, batch inventory and FEFO deduction, customer loyalty tiers, sales checkout, reporting, and soft deletes.
2. [Architecture and Data Model (`docs/ARCHITECTURE_MODEL.md`)](docs/ARCHITECTURE_MODEL.md) details the 3-tier MVC package boundaries, the UML class diagram, and the column schemas for all five CSV files.
3. [Design Principles (`docs/DESIGN_PRINCIPLES.md`)](docs/DESIGN_PRINCIPLES.md) explains how the four OOP pillars, the five SOLID principles, and core software engineering principles (`SoC`, `DRY`, `KISS`, `YAGNI`) are realized in the codebase.
4. [Design Patterns (`docs/DESIGN_PATTERNS.md`)](docs/DESIGN_PATTERNS.md) documents the five patterns used in the project: Factory Method, Atomic CSV Persistence, Singleton, Strategy, and Template Method.
5. [User Interface Specification (`docs/UI_SPECIFICATION.md`)](docs/UI_SPECIFICATION.md) specifies the 80-column VT100 ASCII terminal layout, English localization policy, navigation prompts, string truncation rules, and table column widths.
6. [Naming Conventions (`docs/NAMING_CONVENTIONS.md`)](docs/NAMING_CONVENTIONS.md) establishes naming rules for packages, classes, methods, variables, constants, and architectural layer suffixes.
7. [Clean Code Standards (`docs/CLEAN_CODE.md`)](docs/CLEAN_CODE.md) sets the rules for named constants, method decomposition, defensive programming, exception handling, and plain-text Javadoc with `BR-*` traceability tags.

---

## 5. Team Members

| Member Name | Role | Primary Responsibility |
| :--- | :--- | :--- |
| Nguyen Van Phu | Team Leader | Core infrastructure (`util`), `MainMenu`, MVC wiring, and build integration |
| Tran Cao Thanh | Developer | `Product` and `Batch` models, `ProductController`, `ProductMenu`, and `InventoryMenu` |
| Le Tri Thien | Developer | `Customer` hierarchy, `CustomerController`, and `CustomerMenu` |
| Nguyen Cong Tam | Developer | `Order` and `OrderDetail` models, `OrderController`, `ReportController`, `SalesMenu`, and `ReportMenu` |

---

## 6. License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
