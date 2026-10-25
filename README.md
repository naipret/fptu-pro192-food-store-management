# Food Store Management System (PRO192)

A console-based retail food store management application built with pure Java SE 8 (Java Core 8) for the `PRO192` course at FPT University. The system manages grocery product catalogs, batch-level inventory with `FEFO` stock deduction, customer loyalty tiers, point-of-sale checkout, and analytical revenue reporting.

---

## 1. Technical Stack and Constraints

| Component | Specification |
| :--- | :--- |
| Language | Java SE 8 (Java Core 8, `javac.source=1.8`, `javac.target=1.8`) |
| Dependencies | Standard JDK 8 library only (Zero external third-party JARs or frameworks) |
| Runtime Compatibility | Standard Java 8+ runtime (OpenJDK, Eclipse Temurin, Oracle JDK) |
| User Interface | 80-column VT100 ASCII Console Text User Interface (English) |
| Data Persistence | RFC 4180 CSV flat files (`data/*.csv`) with atomic file replacement |

---

## 2. Project Structure

The codebase follows a standard 3-tier Model-View-Controller (`MVC`) architecture supplemented by stateless utility helpers:

```text
fptu-pro192-food-store-management/
├── CONTRIBUTING.md                            # Git branching, commit, issue, and PR workflow
├── README.md                                  # Project overview and quick start guide
├── scripts/                                   # Dynamic demo dataset generation scripts
│   ├── seed.ps1                               # PowerShell script (Windows)
│   └── seed.sh                                # Bash script (Linux / macOS / WSL)
├── data/                                      # Runtime CSV storage files (auto-initialized with headers)
│   ├── products.csv                           # Retail product catalog records
│   ├── batches.csv                            # Inventory batch records linked to products
│   ├── customers.csv                          # Registered Regular and VIP customer records
│   ├── orders.csv                             # Completed sales order headers
│   └── order_details.csv                      # Line-item snapshots for completed orders
├── docs/                                      # Architectural and engineering specifications
│   ├── BUSINESS_RULES.md                      # Domain validation and operational rules (BR1-BR34)
│   ├── ARCHITECTURE_MODEL.md                  # 3-Tier MVC architecture, UML diagram, and CSV schemas
│   ├── DESIGN_PRINCIPLES.md                   # OOP pillars, SOLID, SoC, DRY, KISS, and YAGNI
│   ├── DESIGN_PATTERNS.md                     # GoF and persistence patterns applied in code
│   ├── UI_SPECIFICATION.md                    # 80-column ASCII terminal standards and table layouts
│   ├── NAMING_CONVENTIONS.md                  # Identifier and architectural naming rules
│   └── CLEAN_CODE.md                          # Defensive coding, exception handling, and Javadoc rules
├── .github/
│   ├── netbeans/                              # Optional pre-packaged NetBeans IDE project metadata
│   ├── pages/                                 # Web Terminal (CheerpJ WebAssembly browser runner)
│   │   ├── README.md                          # Technical architecture of the Web Terminal
│   │   ├── index.html                         # GNOME-style Web Terminal & Dynamic Date Generator
│   │   └── bridge/                            # Native I/O stream bridge classes
│   └── workflows/                             # GitHub Actions CI/CD workflows (release, pages)
└── src/main/java/fptu/pro192/foodstoremanagement/
    ├── Main.java                              # Application entry point and dependency wiring
    ├── model/                                 # Domain entities (Product, Batch, Customer, Order, OrderDetail)
    ├── controller/                            # Business logic, Set/Map/List registries, and CSV persistence
    ├── view/                                  # 80-column interactive console menus (Menu hierarchy)
    └── util/                                  # Stateless utilities (CsvUtil, InputValidator, ConsoleUtil, etc.)
```

---

## 3. Build, Seed, and Run Instructions

### 3.1. Build and Run with Java Core 8 (Command Line)

The project requires only a standard JDK 8+ installation without any build tool dependencies.

Compile all Java source files:

```powershell
# Windows (PowerShell)
$files = Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
javac -d build/classes $files
```

```bash
# Linux / macOS / WSL (Bash)
mkdir -p build/classes
javac -d build/classes $(find src/main/java -name "*.java")
```

Run the console application:

```bash
java -cp build/classes fptu.pro192.foodstoremanagement.Main
```

Or package into a standalone executable JAR:

```bash
mkdir -p dist
jar -cfe dist/fptu-pro192-food-store-management.jar fptu.pro192.foodstoremanagement.Main -C build/classes .
java -jar dist/fptu-pro192-food-store-management.jar
```

---

### 3.2. Generating Dynamic Demo Data (`scripts/`)

The application starts cleanly with empty header-only CSV files automatically. To populate a rich, practical demo dataset (16 products, 23 inventory batches across all shelf-life states, 10 customers, 10 orders across past and present dates):

```powershell
# Windows (PowerShell)
powershell -ExecutionPolicy Bypass -File scripts/seed.ps1
```

```bash
# Linux / macOS / WSL (Bash)
./scripts/seed.sh
```

> [!NOTE]
> All batch production, import, and expiration dates as well as order timestamps are calculated dynamically relative to the current local execution date (`today`). This guarantees that near-expiry alerts, FEFO stock deduction, and revenue reports remain alive and testable at any point in time.

---

### 3.3. Continuous Automated Releases (GitHub Releases)

On every push to `main`, the CI workflow automatically compiles and packages a single release archive:

- Release Asset: `fptu-pro192-food-store-management.zip`
- Contents: Pre-compiled executable JAR in `dist/`, full source code in `src/`, dynamic seed scripts in `scripts/`, empty `data/` directory, and optional NetBeans IDE project metadata for convenient one-click importing.
- Latest Build: [GitHub Releases (Continuous Build)](https://github.com/naipret/fptu-pro192-food-store-management/releases/tag/latest)

---

### 3.4. Live Interactive Web Terminal (GitHub Pages)

Experience the complete application directly inside your web browser without installing any Java runtime:

- Live Web Terminal: [https://naipret.github.io/fptu-pro192-food-store-management/](https://naipret.github.io/fptu-pro192-food-store-management/)
- Runs 100% client-side via CheerpJ 3.0 WebAssembly JVM runtime and xterm.js with a GNOME Terminal dark theme.
- Features client-side dynamic seed data generation, ensuring live dates on every visit.
- For in-depth architectural details on CheerpJ JNI stream bridging and WebAssembly execution, refer to [Web Terminal Technical Documentation (`.github/pages/README.md`)](.github/pages/README.md).

---

## 4. Documentation Index

All architectural decisions, domain rules, and coding standards are maintained in the [`docs/`](docs/) directory:

1. [Business Rules (`docs/BUSINESS_RULES.md`)](docs/BUSINESS_RULES.md): Invariants `BR1` through `BR34` governing product catalog, batch inventory, FEFO deduction, customer loyalty tiers, sales checkout, reporting, and soft deletes.
2. [Architecture and Data Model (`docs/ARCHITECTURE_MODEL.md`)](docs/ARCHITECTURE_MODEL.md): 3-tier MVC package boundaries, UML class diagram, and CSV schemas.
3. [Design Principles (`docs/DESIGN_PRINCIPLES.md`)](docs/DESIGN_PRINCIPLES.md): Four OOP pillars, SOLID principles, and core engineering principles (`SoC`, `DRY`, `KISS`, `YAGNI`).
4. [Design Patterns (`docs/DESIGN_PATTERNS.md`)](docs/DESIGN_PATTERNS.md): Factory Method, Atomic CSV Persistence, Singleton, Strategy, and Template Method patterns.
5. [User Interface Specification (`docs/UI_SPECIFICATION.md`)](docs/UI_SPECIFICATION.md): 80-column ASCII terminal standards, continuous loops, and table formatting.
6. [Naming Conventions (`docs/NAMING_CONVENTIONS.md`)](docs/NAMING_CONVENTIONS.md): Architectural naming conventions for classes, methods, and constants.
7. [Clean Code Standards (`docs/CLEAN_CODE.md`)](docs/CLEAN_CODE.md): Defensive programming, exception handling, and Javadoc standards.
8. [Web Terminal Architecture (`.github/pages/README.md`)](.github/pages/README.md): Client-side WebAssembly execution, xterm.js integration, and JNI stream redirection.

---

## 5. Team Members

| Member Name | Role | Primary Responsibility |
| :--- | :--- | :--- |
| Nguyen Van Phu | Team Leader | Core infrastructure (`util`), `MainMenu`, MVC wiring, and CI/CD automation |
| Tran Cao Thanh | Developer | `Product` and `Batch` models, `ProductController`, `ProductMenu`, and `InventoryMenu` |
| Le Tri Thien | Developer | `Customer` hierarchy, `CustomerController`, and `CustomerMenu` |
| Nguyen Cong Tam | Developer | `Order` and `OrderDetail` models, `OrderController`, `ReportController`, `SalesMenu`, and `ReportMenu` |

---

## 6. License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
