# [TASK #2] feat(product): implement food inheritance, batch model, and product repository

### 👤 Ownership & Roles

- **Issue Number:** #2
- **Assignee:** Trần Cao Thành
- **Reviewer:** Nguyễn Văn Phú
- **Target Module:** `product-inventory`
- **Deadline:** 08/10/2026 (Day 7)
- **Priority:** High (P1)

---

### 🎯 Objective & Summary

Implement the complete Object-Oriented domain hierarchy for food products and inventory batches, alongside the in-memory `ProductRepository` handling atomic synchronization with CSV storage (`data/products.csv` and `data/batches.csv`).

This task models:

1. `Batch`: Represents individual physical inventory shipments with distinct production and expiration dates.
2. `Food` (abstract parent class): Encapsulates common product attributes and defines polymorphic contracts.
3. Three concrete subclasses (`FrozenFood`, `ChilledFood`, `DryFood`): Encapsulate HACCP food safety storage instructions and expiration warning thresholds.
4. `ProductFactory`: Implements the Factory Method pattern to instantiate the appropriate `Food` subclass when parsing CSV records.
5. `ProductRepository`: Manages in-memory product collections and coordinates atomic CSV reads/writes.

---

### 📖 Required Reading Before Coding

1. [`docs/BUSINESS_RULES.md`](../docs/BUSINESS_RULES.md):
   - **BR1**: Unique Product ID format `P00001`–`P99999` (immutable).
   - **BR3**: Product name and category cannot be empty or blank.
   - **BR4**: Product unit cannot be empty or blank (e.g., `Gram`, `Mili lit`, `Goi`, `Hop`, `Khay`).
   - **BR5 & BR5.1**: Product price > 0; product name must contain alphabetic characters.
   - **BR6**: Product stock quantity cannot be negative.
   - **BR7**: Production date $\le$ Expiration date, and Production date $\le$ Current system date.
   - **BR20.1 & BR20.2**: HACCP threshold warnings (Chilled $\le 1$ day, Dry $\le 7$ days).
   - **BR26**: Quantity must be an integer (`int`). Weighted goods measured in Grams.
   - **BR27**: Soft-delete semantics (`isDeleted = true`). Records are never physically purged.
2. [`docs/ARCHITECTURE_MODEL.md`](../docs/ARCHITECTURE_MODEL.md):
   - **Section 2**: Mermaid Class Diagram of `Food`, `Batch`, and product subclasses.
   - **Section 3**: Rubric rationale for explicit subclasses over enums (Polymorphism & OCP).
   - **Section 4**: HACCP temperature and humidity bounds table.
3. [`docs/DESIGN_PATTERNS.md`](../docs/DESIGN_PATTERNS.md):
   - **Section 1**: Factory Method Pattern (`ProductFactory`).
   - **Section 2**: Lightweight Repository Pattern (`ProductRepository`).

---

### 💡 Conceptual Deep Dive (For Newbies)

1. **Why is `Food` an Abstract Class?**
   - In a food distribution store, "Food" is an abstract generalization. You cannot stock an abstract item; you stock specific goods with concrete storage rules: frozen beef (`FrozenFood`), fresh milk (`ChilledFood`), or instant noodles (`DryFood`). By declaring `Food` as `abstract`, we prevent invalid instantiation `new Food(...)` while enforcing polymorphic contracts.
2. **Aggregation: One Product has Multiple Batches (`Food 1 --- * Batch`):**
   - Instant Noodles is a single catalog item (`P00001`). However, the warehouse may hold 50 packages imported on October 1st (expiring Oct 2027) and 70 packages imported on October 5th (expiring Nov 2027). The active stock of `P00001` is the sum of quantities across non-expired batches.
3. **Decoupled CSV Storage (`products.csv` vs `batches.csv`):**
   - Storing batch details directly inside product rows creates data duplication. We split them cleanly: `products.csv` stores immutable catalog metadata, while `batches.csv` stores stock levels and expiry dates linked via `productId`.

---

### 🛠 Technical Specifications & Target Files

#### Target Package: `fptu.pro192.foodstoremanagement.model`

##### 1. `Batch.java`

- **Fields:**
  - `private String batchId;` (e.g., `B00001`)
  - `private String productId;` (foreign key linking to `Food.id`)
  - `private LocalDate importDate;`
  - `private LocalDate productionDate;`
  - `private LocalDate expirationDate;`
  - `private int quantity;`
- **Methods:**
  - Standard getters and setters enforcing **BR7** (production date $\le$ expiration date) and **BR26** (quantity $\ge 0$).
  - `// BR11: Check if this batch is expired relative to currentDate`
    `public boolean isExpired(LocalDate currentDate)`
  - `// BR20: Check if batch is within warning days threshold`
    `public boolean isCloseToExpiry(LocalDate currentDate, int warningDays)`
  - `public void deduct(int amount)`: Deducts stock, throwing `IllegalArgumentException` if `amount > quantity`.

##### 2. `Food.java` (abstract)

- **Fields:**
  - `protected String id;` (BR1)
  - `protected String name;` (BR3, BR5.1)
  - `protected String category;` (BR3)
  - `protected String unit;` (BR4)
  - `protected double price;` (BR5)
  - `protected double minTemperature;`
  - `protected double maxTemperature;`
  - `protected double minHumidity;`
  - `protected double maxHumidity;`
  - `protected boolean isDeleted;` (BR27)
  - `protected List<Batch> batches;`
- **Abstract Methods:**
  - `public abstract String getStorageInstructions();`
  - `public abstract int getDaysBeforeExpiryWarning();`
- **Concrete Domain Methods:**
  - `public int getTotalStock()`: Sums `quantity` of all batches where `!isExpired(today)`.
  - `public List<Batch> getActiveBatches(LocalDate today)`: Returns sorted list of non-expired batches.
  - `public void addBatch(Batch batch)`: Appends a batch to internal list.
  - `public List<Batch> getBatches()`: Returns `Collections.unmodifiableList(batches)`.

##### 3. Concrete Subclasses: `FrozenFood.java`, `ChilledFood.java`, `DryFood.java`

- Override `getStorageInstructions()` returning human-readable HACCP guidelines (e.g., `Bảo quản đông lạnh từ -25°C đến -18°C`).
- Override `getDaysBeforeExpiryWarning()`:
  - `FrozenFood`: returns `14` days.
  - `ChilledFood`: returns `1` day (**BR20.1**).
  - `DryFood`: returns `7` days (**BR20.2**).

##### 4. `ProductFactory.java`

- **Pattern:** Factory Method.
- **Method:**
  `public static Food create(String type, String id, String name, String category, String unit, double price, double minTemp, double maxTemp, double minHumidity, double maxHumidity, boolean isDeleted)`
  - Instantiates `FrozenFood`, `ChilledFood`, or `DryFood` based on `type`.

#### Target Package: `fptu.pro192.foodstoremanagement.repository`

##### 5. `ProductRepository.java`

- **Pattern:** Lightweight Repository.
- **Fields:**
  - `private final String productFilePath = "data/products.csv";`
  - `private final String batchFilePath = "data/batches.csv";`
  - `private final List<Food> products = new ArrayList<>();`
- **Methods:**
  - `public void loadFromCsv()`:
    1. Reads `products.csv` using `MiniCsv.read()`.
    2. Instantiates `Food` instances via `ProductFactory`.
    3. Reads `batches.csv` using `MiniCsv.read()`, creates `Batch` objects, and attaches them to corresponding `Food` objects via `productId`.
  - `public synchronized void saveToCsv()`:
    1. Serializes `products` and `batches` into CSV rows.
    2. Calls `MiniCsv.writeAtomic()` for both files.
  - `public Food findById(String id)`: Returns active (`!isDeleted`) food item matching ID.
  - `public List<Food> findAll()`: Returns unmodifiable list of active food items.
  - `public void save(Food food)`: Inserts food and calls `saveToCsv()`.
  - `// BR27: Soft delete`
    `public void delete(String id)`: Marks `isDeleted = true` and triggers `saveToCsv()`.

---

### 🔗 Dependencies & System Impact

- **Depends On:** Task #1 (`MiniCsv.java`).
- **Unblocks:**
  - Task #4 (Tâm): Needs `Food` reference inside `OrderDetail`.
  - Task #5 (Thành): Needs `ProductRepository` to implement business rules in `ProductService`.
  - Task #7 (Tâm): Needs `ProductRepository` for stock verification.

---

### ✅ Acceptance Criteria & Verification

- [ ] All classes compile cleanly under Java 8 with zero warnings.
- [ ] Invariants strictly validated in constructors: Invalid ID format, empty names, negative price, or negative quantity throw descriptive exceptions.
- [ ] Polymorphism verified: Iterating a `List<Food>` containing `FrozenFood` and `DryFood` yields distinct storage instructions.
- [ ] Persistence verified: Adding products and batches writes properly formatted records to `data/products.csv` and `data/batches.csv`. Reading back reproduces identical domain state.
- [ ] Pull Request submitted conforming to Conventional Commits: `feat(product): implement food inheritance, batch model, and product repository`.
