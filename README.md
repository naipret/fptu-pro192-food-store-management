# Food Store Management System

A console-based retail food store management application built with pure Java SE 8 for the `PRO192` course at FPT University. The system manages grocery product catalogs, batch-level inventory with `FEFO` stock deduction, customer loyalty tiers, point-of-sale checkout, and analytical revenue reporting.

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
├── CONTRIBUTING.md
├── README.md
├── scripts/
│   ├── seed.ps1
│   └── seed.sh
├── data/
│   ├── products.csv
│   ├── batches.csv
│   ├── customers.csv
│   ├── orders.csv
│   └── order_details.csv
├── docs/
│   ├── BUSINESS_RULES.md
│   ├── ARCHITECTURE_MODEL.md
│   ├── DESIGN_PRINCIPLES.md
│   ├── DESIGN_PATTERNS.md
│   ├── UI_SPECIFICATION.md
│   ├── NAMING_CONVENTIONS.md
│   └── CLEAN_CODE.md
└── src/main/java/fptu/pro192/foodstoremanagement/
    ├── Main.java
    ├── model/
    ├── controller/
    ├── view/
    └── util/
```

## 3. Generating Dynamic Demo Data

The application starts cleanly with empty header-only CSV files automatically. To populate a rich, practical demo dataset (16 products, 23 inventory batches across all shelf-life states, 10 customers, 10 orders across past and present dates):

```powershell
# powershell
scripts\seed.ps1
```

```bash
# bash
./scripts/seed.sh
```

> [!NOTE]
> All batch production, import, and expiration dates as well as order timestamps are calculated dynamically relative to the current local execution date (`today`). This guarantees that near-expiry alerts, FEFO stock deduction, and revenue reports remain alive and testable at any point in time.

---

## 4. Continuous Automated Releases

On every push to `main`, the CI workflow automatically compiles and packages a single release archive:

- Release Asset: `fptu-pro192-food-store-management.zip`
- Contents: Pre-compiled executable JAR in `dist/`, full source code in `src/`, dynamic seed scripts in `scripts/`, empty `data/` directory, and optional NetBeans IDE project metadata for convenient one-click importing.
- Latest Build: [GitHub Releases](https://github.com/naipret/fptu-pro192-food-store-management/releases/tag/latest)

---

## 5. Live Interactive Web Terminal

Experience the complete application directly inside your web browser without installing any Java runtime:

- Runs 100% client-side via CheerpJ 3.0 WebAssembly JVM runtime and xterm.js with a GNOME Terminal dark theme.
- Features client-side dynamic seed data generation, ensuring live dates on every visit.
- For in-depth architectural details on CheerpJ JNI stream bridging and WebAssembly execution, refer to [Web Terminal Technical Documentation (`.github/pages/README.md`)](.github/pages/README.md).
- Live Web Terminal: [Github Pages](https://naipret.github.io/fptu-pro192-food-store-management/)

---

## 6. Documentation Index

All architectural decisions, domain rules, and coding standards are maintained in the [`docs/`](docs/) directory:

1. [Business Rules (`docs/BUSINESS_RULES.md`)](docs/BUSINESS_RULES.md): Invariants `BR1` through `BR34` governing product catalog, batch inventory, FEFO deduction, customer loyalty tiers, sales checkout, reporting, and soft deletes.
2. [Architecture and Data Model (`docs/ARCHITECTURE_MODEL.md`)](docs/ARCHITECTURE_MODEL.md): 3-tier MVC package boundaries, UML class diagram, and CSV schemas.
3. [Design Principles (`docs/DESIGN_PRINCIPLES.md`)](docs/DESIGN_PRINCIPLES.md): Four OOP pillars, SOLID principles, and core engineering principles (`SoC`, `DRY`, `KISS`, `YAGNI`).
4. [Design Patterns (`docs/DESIGN_PATTERNS.md`)](docs/DESIGN_PATTERNS.md): Factory Method, Atomic CSV Persistence, Singleton, Strategy, and Template Method patterns.
5. [User Interface Specification (`docs/UI_SPECIFICATION.md`)](docs/UI_SPECIFICATION.md): 80-column ASCII terminal standards, continuous loops, and table formatting.
6. [Naming Conventions (`docs/NAMING_CONVENTIONS.md`)](docs/NAMING_CONVENTIONS.md): Architectural naming conventions for classes, methods, and constants.
7. [Clean Code Standards (`docs/CLEAN_CODE.md`)](docs/CLEAN_CODE.md): Defensive programming, exception handling, and Javadoc standards.

---

## 7. License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
