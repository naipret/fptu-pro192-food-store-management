# [TASK #1] feat(core): setup netbeans 13 ant project and core utilities

### 👤 Ownership & Roles

- **Issue Number:** #1
- **Assignee:** Nguyễn Văn Phú
- **Reviewer:** Trần Cao Thành
- **Target Module:** `core-infrastructure`
- **Deadline:** 04/10/2026 (Day 3)
- **Priority:** Blocker (P0 - Entire team is blocked by this task)

---

### 🎯 Objective & Summary

Establish the foundational project scaffolding for NetBeans 13 (Ant-based Java Application targeting Java 8 SE) and develop zero-dependency core utilities in `src/main/java/fptu/pro192/foodstoremanagement/util` and `ui`.

This includes:

1. Configuring NetBeans 13 Ant project files (`build.xml`, `nbproject/project.xml`, `nbproject/project.properties`) with `src.dir=src/main/java` and UTF-8 encoding.
2. Developing the custom RFC 4180-compliant CSV engine (`MiniCsv`) featuring atomic file replacement (`ATOMIC_MOVE`).
3. Implementing the Singleton `ScannerManager` to prevent standard input stream corruption.
4. Implementing the `TableFormatter` with deterministic string truncation (`fit()`) ensuring rigid 80-column ASCII borders.
5. Implementing `ConsoleUtil` for screen clearing and bulletproof user input validation.
6. Implementing the base `AbstractMenu` using the **Template Method Pattern** to drive standard console screen execution lifecycles.

---

### 📖 Required Reading Before Coding

1. [`docs/ARCHITECTURE_MODEL.md`](../docs/ARCHITECTURE_MODEL.md):
   - **Section 1**: 3-Tier Layered MVC Architecture.
   - **Section 5**: 80-column VT100 standard and truncation rule (`fit(text, width)`).
2. [`docs/DESIGN_PATTERNS.md`](../docs/DESIGN_PATTERNS.md):
   - **Section 3**: Singleton Pattern (`ScannerManager`).
   - **Section 5**: Template Method Pattern (`AbstractMenu`).
3. [`CONTRIBUTING.md`](../CONTRIBUTING.md):
   - Conventional Commits, Javadoc standards, and Git branch `feature/core-infrastructure`.

---

### 💡 Conceptual Deep Dive (For Newbies)

1. **Why `src/main/java` instead of root `src/`?**
   - Industry-standard Maven/Gradle directory layout. By setting `src.dir=src/main/java` in `nbproject/project.properties`, NetBeans 13 recognizes it seamlessly, while enabling instant future migration without relocating source files.
2. **Why Singleton for `Scanner`?**
   - In Java console applications, wrapping `System.in` in multiple `Scanner` instances or calling `scanner.close()` permanently closes the JVM's underlying `System.in` input descriptor. Subsequent calls throw unrecoverable `NoSuchElementException`. A centralized `ScannerManager` Singleton maintains a single persistent scanner instance.
3. **How Atomic CSV Writes Prevent File Corruption:**
   - Writing directly to an active file risks zero-byte truncation if the JVM crashes or the user terminates execution mid-write. By writing to a temporary file (`products.csv.tmp`) and calling `Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)`, the operating system replaces the inode atomically.

---

### 🛠 Technical Specifications & Target Files

#### Target Package: `fptu.pro192.foodstoremanagement.util`

##### 1. `ScannerManager.java`

- **Pattern:** Singleton.
- **Fields:** `private static ScannerManager instance; private final Scanner scanner;`
- **Methods:**
  - `private ScannerManager()`: initializes `new Scanner(System.in)`.
  - `public static synchronized ScannerManager getInstance()`: lazy initialization.
  - `public Scanner getScanner()`: returns the managed `Scanner` reference.

##### 2. `MiniCsv.java`

- **Responsibility:** Pure zero-dependency RFC 4180 CSV serializer and parser.
- **Methods:**
  - `public static List<String[]> read(String filePath)`: Parses CSV rows, correctly handling commas enclosed within double quotes (`"..."`) and escaped quotes (`""`).
  - `public static void writeAtomic(String filePath, List<String> headers, List<String[]> rows)`:
    - Writes content to `filePath + ".tmp"` using `BufferedWriter` with `StandardCharsets.UTF_8`.
    - Automatically escapes cells containing commas, quotes, or newlines by enclosing them in quotes and doubling existing quotes.
    - Atomically replaces target file using `Files.move(..., REPLACE_EXISTING, ATOMIC_MOVE)`.

##### 3. `TableFormatter.java`

- **Responsibility:** Terminal layout formatting conforming to fixed 80-column width.
- **Constants:** `public static final int SCREEN_WIDTH = 80;`
- **Methods:**
  - `public static String fit(String text, int width)`:
    - If `text == null`, defaults to empty string.
    - If `text.length() > width`, truncates to `width - 2` and appends `..`.
    - Right-pads with spaces to guarantee exact character length equals `width`.
  - `public static String center(String text, int width)`: Centers text within the specified column span with equal flanking whitespace.
  - `public static String repeat(char ch, int count)`: Generates a repeated character line.

##### 4. `ConsoleUtil.java`

- **Responsibility:** Screen control and defensive console input reading.
- **Methods:**
  - `public static void clearScreen()`: Prints ANSI escape sequence `\033[H\033[2J` and flushes `System.out`, with a fallback printing 50 blank lines for non-ANSI Windows terminals.
  - `public static void pressEnterToContinue()`: Displays `Nhấn phím [ENTER] để tiếp tục...` and waits for newline input.
  - `public static int readInt(String prompt, int min, int max)`: Continuously prompts the user until a valid integer within `[min, max]` is entered.
  - `public static String readString(String prompt, boolean allowEmpty)`: Defensively reads line input, enforcing non-blank entries when `allowEmpty == false`.

#### Target Package: `fptu.pro192.foodstoremanagement.ui`

##### 5. `AbstractMenu.java`

> [!NOTE]
> **Provisional / Pseudo Menu Specification**:
> The menu execution lifecycle shown below provides the foundational Template Method skeleton. Concrete menu options, headers, and navigation details implemented in downstream tasks (#5, #6, #7, #8, #9) are tentative draft references (pseudo-design), which may be adjusted during feature development as long as the 80-column width, Vietnamese prompts, and `[0]` return conventions are preserved.

- **Pattern:** Template Method.
- **Fields:** `protected final String title; protected static final int MENU_WIDTH = 80;`
- **Methods:**
  - `public void display()`: **Template Method**.

    ```java
    public void display() {
        boolean running = true;
        while (running) {
            ConsoleUtil.clearScreen();
            renderHeader();
            renderBody();
            renderFooter();
            int choice = ConsoleUtil.readInt("Chọn chức năng: ", 0, 9);
            if (choice == 0) {
                break; // Option 0: Cancel / Return
            }
            running = handleOption(choice);
            if (running) {
                ConsoleUtil.pressEnterToContinue();
            }
        }
    }
    ```

  - `protected void renderHeader()`: Renders double line `=` with centered title.
  - `protected abstract void renderBody()`: Implemented by concrete menus.
  - `protected void renderFooter()`: Renders single line `-` with `[0] Quay lại / Hủy`.
  - `protected abstract boolean handleOption(int choice)`: Handles numeric choice selection.

---

### 🔗 Dependencies & System Impact

- **Depends On:** None (Root Scaffolding).
- **Unblocks:**
  - Task #2 (Thành): Requires `MiniCsv` for `ProductRepository`.
  - Task #3 (Thiện): Requires `MiniCsv` and `ConsoleUtil`.
  - Task #4 (Tâm): Requires `MiniCsv` for `OrderRepository`.
  - Task #9 (Phú): Requires `AbstractMenu` for `MainMenu`.

---

### ✅ Acceptance Criteria & Verification

- [ ] Project opens cleanly in NetBeans IDE 13 without warnings or broken reference badges.
- [ ] Source directory is recognized as `src/main/java`.
- [ ] Running Ant build (`ant compile`) succeeds with 0 errors and 0 warnings under Java 8 (`-source 1.8 -target 1.8`).
- [ ] `MiniCsv` test verification: Reads and writes multi-column CSV containing commas, quotes, and Vietnamese diacritics without data loss.
- [ ] `TableFormatter.fit("Thịt heo ba rọi đóng hộp cao cấp", 15)` produces `"Thịt heo ba r.."` with length exactly 15.
- [ ] Code strictly complies with Java 8 syntax (no `var`, no `java.net.http`).
- [ ] All methods have complete Javadoc (`@param`, `@return`, `@throws`).
- [ ] Pull Request opened targeting `main` following Conventional Commits format: `feat(core): setup netbeans 13 ant project and core utilities`.
