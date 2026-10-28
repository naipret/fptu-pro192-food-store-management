# Web Terminal

An interactive web-based terminal emulator that executes the Food Store Management System directly inside the browser using Java WebAssembly (CheerpJ 3.0) and xterm.js, with zero server-side backend.

---

## 1. Architecture Overview

```text
Browser Window (Client Device)
+------------------------------------------------------------------------+
| GNOME Terminal Web UI (HTML5 / CSS3 / JetBrains Mono)                  |
|                                                                        |
| +--------------------------------------------------------------------+ |
| | xterm.js (Terminal Canvas Emulator, cols >= 80)                    | |
| +----------------------------------+---------------------------------+ |
|                                    | ANSI Text Streams / Key Events    |
| +----------------------------------v---------------------------------+ |
| | Native JNI Bridge (web.WebTerminalManager)                         | |
| | - System.out / System.err Output Stream Pipe                       | |
| | - System.in Asynchronous Input Stream Pipe                         | |
| | - Dynamic Seed CSV Supplier (getInitialCsv)                        | |
| +----------------------------------+---------------------------------+ |
|                                    | In-Memory Bytecode Execution      |
| +----------------------------------v---------------------------------+ |
| | CheerpJ 3.0 Java WebAssembly Runtime (JDK 8 JVM Engine)            | |
| | +-- Application JAR: fptu-pro192-food-store-management.jar         | |
| | \-- Virtual Filesystem (/files/data/*.csv -> Browser IndexedDB)    | |
| +----------------------------------+---------------------------------+ |
|                                    | Local Storage Persistence         |
| +----------------------------------v---------------------------------+ |
| | Client-Side IndexedDB Storage (No Server, 100% Offline Capable)    | |
| +--------------------------------------------------------------------+ |
+------------------------------------------------------------------------+
```

---

## 2. Key Technical Features

### 2.1. Pure Client-Side Execution

- The entire Java application bytecode runs inside the user's browser via the CheerpJ 3.0 WebAssembly JVM runtime.
- No server-side container, socket proxy, or cloud VM is required. The web terminal is hosted as static assets on GitHub Pages.

### 2.2. GNOME Terminal Aesthetic

- Styled with the classic Linux GNOME Terminal (Adwaita Dark) color palette (`#1e1e1e` background, `#2e2e2e` headerbar).
- Rendered using the programming font JetBrains Mono.
- Locked to a minimum of 80 columns (`cols: 80`) with horizontal scrolling support. Tables formatted for 80 characters never wrap or distort, even on small screens or when the browser is zoomed up to 200%-300%.
- Supports dynamic font scaling via `Ctrl + MouseWheel` for high-DPI displays.

### 2.3. Native Stream

- [`WebTerminalManager.java`](bridge/WebTerminalManager.java): Declares native bridge methods (`print`, `readLine`, `getInitialCsv`).
- [`WebMain.java`](bridge/WebMain.java):
  - Redirects `System.out` and `System.err` to `WebTerminalManager.print()`, sending ANSI escape sequences to `xterm.js`.
  - Rebinds `System.in` to an asynchronous `InputStream` that consumes keystroke lines from `WebTerminalManager.readLine()`.
  - Automatically initializes runtime data files from the client-side seed generator if `data/*.csv` does not exist in IndexedDB.

### 2.4. Client-Side Dynamic Date Seed Generator

- Instead of baking stale, static CSV data into web bundles, the web page includes a JavaScript-based dynamic generator (`generateDynamicSeedData()`).
- When the page loads or when the user clicks Reset Data, the script reads the client's current device clock (`new Date()`) and generates dynamic dates:
  - Batch production and import dates relative to today (`-60d`, `-30d`, `-15d`, `-5d`, `-2d`).
  - Batch expiration dates covering all shelf-life states: active long (`+180d`), medium (`+60d`), short (`+14d`), near-expiry (`+2d`, `+5d`), and expired (`-3d`, `-12d`).
  - Orders distributed across past days, yesterday, and today.
- This ensures test scenarios (such as near-expiry alerts and FEFO stock deduction) remain fully functional regardless of when the reviewer opens the website.

### 2.5. Local Session Persistence and Safe Reset

- All file writes performed by the application (creating orders, importing batches, updating records) are saved to the browser's IndexedDB virtual filesystem.
- Clicking Reset Data in the headerbar clears the local IndexedDB and triggers fresh dynamic data generation for the current day.

---

## 3. Directory Layout

```text
.github/pages/
├── README.md
├── index.html
└── bridge/
    ├── WebMain.java
    └── WebTerminalManager.java
```

---

## 4. Automated Deployment Pipeline

The web terminal is automatically built and deployed to GitHub Pages on every push to `main` via [`.github/workflows/deploy-pages.yml`](../workflows/deploy-pages.yml):

1. Sets up JDK 8 (Temurin) and builds the core application JAR.
2. Compiles `WebMain.java` and `WebTerminalManager.java` against the core JAR.
3. Packages the web payload into `_site` containing `index.html`, `web-runner.jar`, and `fptu-pro192-food-store-management.jar`.
4. Uploads and deploys the artifact to GitHub Pages.
