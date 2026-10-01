# Food Store Management System

A clean, zero-dependency, pure Java 8 console-based management system tailored for food distribution operations. Developed for the **PRO192 (Object-Oriented Programming with Java)** course at **FPT University**.

---

## 🚀 Key Specifications

* **Language & Runtime**: Core Java 8 (Java SE 1.8)
* **Build System**: Plain Java Ant Project (Zero external libraries, zero third-party dependencies)
* **Architecture**: Layered MVC (3-Tier Layered Architecture: `ui` -> `service` -> `repository` -> `model`)
* **Persistence**: Atomic flat CSV storage with custom RFC 4180-compliant zero-dependency engine (`MiniCsv`)
* **Terminal Interface (TUI)**:
  * Width: Fixed **80 columns** (VT100 standard)
  * Character Set: Standard ASCII (`+`, `-`, `=`, `|`, `*`)
  * Language: **Vietnamese** for console UI; **English** for source code, comments, and project documentation

---

## 📂 Project Structure

```text
fptu-pro192-food-store-management/
├── .github/
├── data/
├── docs/
├── tasks/
├── src/main/java/fptu/pro192/foodstoremanagement/
│   ├── Main.java
│   ├── model/
│   ├── repository/
│   ├── service/
│   ├── ui/
│   └── util/
├── nbproject/
├── build.xml
├── .editorconfig
├── .gitattributes
├── .gitignore
├── CONTRIBUTING.md
├── LICENSE
└── README.md
```

---

## 📖 Project Documentation Links

* [Business Rules Specification](docs/BUSINESS_RULES.md)
* [Architecture & Domain Model Specification](docs/ARCHITECTURE_MODEL.md)
* [Design Patterns Specification](docs/DESIGN_PATTERNS.md)
* [Design Principles Specification](docs/DESIGN_PRINCIPLES.md)
* [Contributing Guidelines](CONTRIBUTING.md)

---

## 📄 License

This project is licensed under the terms of the [MIT License](LICENSE).
Copyright (c) 2026 **naipret**.
