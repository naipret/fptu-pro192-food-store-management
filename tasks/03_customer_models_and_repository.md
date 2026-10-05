# [TASK #3] feat(customer): implement customer hierarchy, phone normalization, and repository

### 👤 Ownership & Roles

- **Issue Number:** #3
- **Assignee:** Lê Trí Thiện
- **Reviewer:** Nguyễn Văn Phú
- **Target Module:** `customer-management`
- **Deadline:** 08/10/2026 (Day 7)
- **Priority:** High (P1)

---

### 🎯 Objective & Summary

Implement the Customer domain model hierarchy (`Customers`, `RegularCustomer`, `VIPCustomer`), Vietnamese phone number normalization utility in `InputValidator`, and the `CustomerRepository` managing in-memory customer caching and atomic CSV persistence (`data/customers.csv`).

This task models:

1. `Customers` (abstract parent class): Defines customer attributes and the polymorphic discount calculation contract (**Strategy Pattern**).
2. `RegularCustomer` and `VIPCustomer`: Concrete classes enforcing **BR15** (0% discount) and **BR16** (10% discount).
3. `InputValidator`: Centralized phone number parser handling irregular Vietnamese phone inputs (**BR25**).
4. `CustomerRepository`: Handles in-memory storage, soft-deletion (**BR27**), and atomic CSV persistence.

---

### 📖 Required Reading Before Coding

1. [`docs/BUSINESS_RULES.md`](../docs/BUSINESS_RULES.md):
   - **BR2**: Unique Customer ID format `C00001`–`C99999` (immutable).
   - **BR15**: Regular customers receive 0% discount.
   - **BR16**: VIP customers receive 10% discount.
   - **BR18**: User inputs must be validated prior to persistence.
   - **BR25**: Vietnamese phone normalization rule: converts `+84...`, `037.224.0629`, `(+84) 37 224 0629` into standard 10-digit format starting with `0` (e.g., `0372240629`).
   - **BR27**: Soft delete (`isDeleted = true`). Customers with transaction history are never purged.
2. [`docs/ARCHITECTURE_MODEL.md`](../docs/ARCHITECTURE_MODEL.md):
   - **Section 2**: Customer Class Hierarchy.
   - **Section 5.2.2**: 80-Column Customer Table Layout.
3. [`docs/DESIGN_PATTERNS.md`](../docs/DESIGN_PATTERNS.md):
   - **Section 4**: Strategy Pattern for Polymorphic Discount Calculation.

---

### 💡 Conceptual Deep Dive (For Newbies)

1. **Strategy Pattern via Object-Oriented Polymorphism:**
   - Rather than embedding `if ("VIP".equals(customer.getType()))` conditionals inside the checkout engine, the discount strategy is encapsulated directly within the polymorphic customer subclasses. Calling `customer.calculateDiscount(amount)` automatically executes the correct calculation without caller modification.
2. **Vietnamese Phone Normalization (BR25):**
   - Users may enter phone numbers in varied styles: `+84 901 234 567`, `090.123.4567`, or `0901234567`. The validator strips all non-numeric characters (spaces, dots, parentheses, dashes). If the string starts with `84`, it is replaced with `0`. The final string must match `^0[3|5|7|8|9][0-9]{8}$`.
3. **Preserving Referential Integrity via Soft Delete (BR27):**
   - Deleting a customer marks `isDeleted = true`. Historical orders referencing `customerId` remain valid, preventing orphaned foreign key references in financial reports.

---

### 🛠 Technical Specifications & Target Files

#### Target Package: `fptu.pro192.foodstoremanagement.util`

##### 1. `InputValidator.java`

- **Method:**
  `// BR25: Normalize raw phone number string to 10-digit standard format starting with 0`
  `public static String normalizePhone(String rawPhone)`
  - Strips non-digit characters: `rawPhone.replaceAll("[^0-9+]", "")`.
  - Replaces `+84` or leading `84` with `0`.
  - Validates resulting string against Vietnamese 10-digit regex: `^0[35789]\\d{8}$`.
  - Returns normalized string; returns `null` or throws `IllegalArgumentException` if invalid.

#### Target Package: `fptu.pro192.foodstoremanagement.model`

##### 2. `Customer.java` (abstract)

- **Fields:**
  - `protected String id;` (BR2)
  - `protected String fullName;`
  - `protected String phone;` (BR25)
  - `protected String address;`
  - `protected boolean isDeleted;` (BR27)
- **Abstract Methods:**
  - `// Strategy contract: Returns discount rate (0.0 to 1.0)`
    `public abstract double getDiscountRate();`
- **Concrete Domain Methods:**
  - `// BR15 & BR16: Calculates discount amount based on subtotal`
    `public double calculateDiscount(double subtotal) { return subtotal * getDiscountRate(); }`
  - Standard getters and setters.

##### 3. `RegularCustomer.java` & `VIPCustomer.java`

- `RegularCustomer`:
  - `// BR15: Regular customers receive no discount`
    `@Override public double getDiscountRate() { return 0.0; }`
- `VIPCustomer`:
  - `// BR16: VIP customers receive 10% discount`
    `@Override public double getDiscountRate() { return 0.10; }`

##### 4. `CustomerFactory.java`

- **Method:**
  `public static Customer create(String type, String id, String fullName, String phone, String address, boolean isDeleted)`
  - Returns `VIPCustomer` if `type.equalsIgnoreCase("VIP")`, else returns `RegularCustomer`.

#### Target Package: `fptu.pro192.foodstoremanagement.repository`

##### 5. `CustomerRepository.java`

- **Fields:**
  - `private final String filePath = "data/customers.csv";`
  - `private final List<Customer> customers = new ArrayList<>();`
- **Methods:**
  - `public void loadFromCsv()`: Reads CSV records via `MiniCsv.read()` and populates `customers`.
  - `public synchronized void saveToCsv()`: Serializes `customers` and writes atomically via `MiniCsv.writeAtomic()`.
  - `public Customer findById(String id)`: Returns active customer matching ID.
  - `public Customer findByPhone(String phone)`: Returns active customer matching normalized phone.
  - `public List<Customer> findAll()`: Returns unmodifiable list of all active (`!isDeleted`) customers.
  - `public void save(Customer customer)`: Appends/updates customer and triggers `saveToCsv()`.
  - `// BR27: Soft delete`
    `public void delete(String id)`: Marks `customer.setDeleted(true)` and calls `saveToCsv()`.

---

### 🔗 Dependencies & System Impact

- **Depends On:** Task #1 (`MiniCsv.java`).
- **Unblocks:**
  - Task #4 (Tâm): Needs customer model to reference in `Order`.
  - Task #6 (Thiện): Needs repository to build `CustomerService` and `CustomerMenu`.
  - Task #7 (Tâm): Needs customer polymorphic discount calculation during checkout.

---

### ✅ Acceptance Criteria & Verification

- [ ] All classes compile cleanly in NetBeans 13 under Java 8 with zero warnings.
- [ ] `normalizePhone` test: Inputs `+84372240629`, `(+84) 37 224 0629`, `037.224.0629` all normalize to `0372240629`. Invalid numbers (e.g., `12345`, `01234567890`) throw `IllegalArgumentException`.
- [ ] Strategy test: `VIPCustomer` returns `10,000` discount on `100,000` subtotal; `RegularCustomer` returns `0`.
- [ ] Atomic persistence verified: `data/customers.csv` persists valid records with proper headers.
- [ ] Pull Request opened following Conventional Commits: `feat(customer): implement customer hierarchy, phone normalization, and repository`.
