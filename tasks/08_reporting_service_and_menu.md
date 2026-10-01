# [TASK #8] feat(report): implement monthly revenue and ranking reports with ReportMenu

### 👤 Ownership & Roles

- **Issue Number:** #8
- **Assignee:** Nguyễn Công Tâm
- **Reviewer:** Trần Cao Thành
- **Target Module:** `sales-reporting`
- **Deadline:** 13/10/2026 (Day 12)
- **Priority:** High (P1)

---

### 🎯 Objective & Summary

Implement the analytical query engine in `ReportService` and the interactive console presentation menu `ReportMenu` (**Tasks B15, B16, B17, B18**) adhering to the fixed 80-column ASCII terminal standard.

This task delivers:

1. `ReportService`: Calculates monthly sales metrics (**BR23**), aggregates top-selling food items by quantity sold (**BR21**), identifies highest-spending customers by completed transaction volume (**BR22**), and extracts low-stock inventory alerts (**BR19**).
2. `ReportMenu`: Concrete subclass of `AbstractMenu` providing interactive reporting screens and tabular summaries.

---

### 📖 Required Reading Before Coding

1. [`docs/BUSINESS_RULES.md`](../docs/BUSINESS_RULES.md):
   - **BR19**: Low stock threshold ($\le 5$ units).
   - **BR21**: Best-selling food products ranking: Top items sorted by total quantity sold.
   - **BR22**: Highest-spending customers ranking: sorted by total purchase value across completed transactions.
   - **BR23**: Total revenue is calculated exclusively from completed sales transactions.
2. Đề bài Task B15 (Monthly Sales Report), Task B16 (Best-Selling Food Products), Task B17 (Highest-Spending Customers), Task B18 (Low Stock Report).
3. [`docs/ARCHITECTURE_MODEL.md`](../docs/ARCHITECTURE_MODEL.md):
   - **Section 5.1**: Text Truncation (`TableFormatter.fit(text, width)`).
   - **Section 5**: 80-Column ASCII layout standards.

---

### 💡 Conceptual Deep Dive (For Newbies)

1. **Revenue Calculation (BR23):**
   - Must only aggregate `orders` and `transactions` where `isCompleted == true`. Unconfirmed draft orders or cancelled carts must never distort revenue figures.
2. **Aggregation via Java 8 Maps & Streams:**
   - **Best-Selling Products (BR21):** Iterate through all `OrderDetail` records from completed orders. Accumulate total quantities sold into a `Map<String, Integer> productQtyMap`. Sort entries in descending order by quantity and take the top 10.
   - **Highest-Spending Customers (BR22):** Iterate through all completed `Order` records. Accumulate `finalAmount` into a `Map<String, Double> customerSpendMap`. Sort entries in descending order by total spend.
3. **Currency & Number Formatting:**
   - Format all currency outputs with thousand separators followed by `VND` (e.g., `485,500,000 VND`), using `java.text.NumberFormat` or `DecimalFormat("#,##0 VND")`.

---

### 🛠 Technical Specifications & Target Files

#### Target Package: `fptu.pro192.foodstoremanagement.service`

##### 1. `ReportService.java`

- **Fields:**
  - `private final OrderRepository orderRepo;`
  - `private final ProductRepository productRepo;`
  - `private final CustomerRepository customerRepo;`
- **DTOs / Helper Classes:**
  - `public static class MonthlyReport`: contains `int totalTransactions`, `int totalItemsSold`, `double totalRevenue`.
  - `public static class ProductSalesStat`: contains `String productId`, `String productName`, `int quantitySold`.
  - `public static class CustomerSpendStat`: contains `String customerId`, `String customerName`, `double totalSpent`.
- **Methods:**
  - `// BR23: Calculates monthly sales report for specified month and year (Task B15)`
    `public MonthlyReport getMonthlySalesReport(int month, int year)`
  - `// BR21: Returns Top N best-selling products by quantity sold (Task B16)`
    `public List<ProductSalesStat> getBestSellingProducts(int limit)`
  - `// BR22: Returns Top N highest-spending customers (Task B17)`
    `public List<CustomerSpendStat> getHighestSpendingCustomers(int limit)`
  - `// BR19: Returns products with total stock <= 5 (Task B18)`
    `public List<Food> getLowStockReport()`

#### Target Package: `fptu.pro192.foodstoremanagement.ui`

##### 2. `ReportMenu.java`

> [!NOTE]
> **Provisional / Pseudo Menu Specification**:
> The menu layout, titles, and numeric option choices shown below are tentative draft references (pseudo-design). They are not finalized and can be adjusted during implementation as needed, provided the 80-column width limit, Vietnamese language, clear screen on transition, and `[0]` to return/cancel are strictly maintained.

- **Extends:** `AbstractMenu`
- **Title:** `"BÁO CÁO & THỐNG KÊ DOANH THU"`
- **Options Rendered (Tentative Draft / Pseudo):**

  ```text
  1. Báo cáo doanh thu theo tháng (Task B15)
  2. Top sản phẩm bán chạy nhất (Task B16)
  3. Top khách hàng chi tiêu cao nhất (Task B17)
  4. Báo cáo sản phẩm tồn kho thấp (Task B18)
  0. Quay lại Menu chính
  ```

- **Table Renderers:**
  - Renders 80-column ASCII tables for rankings with properly aligned currency headers.

---

### 🔗 Dependencies & System Impact

- **Depends On:** Task #1 (`AbstractMenu`), Task #4 (`OrderRepository`), Task #5 (`ProductRepository`), Task #6 (`CustomerRepository`).
- **Unblocks:**
  - Task #9 (Phú): Attached as submenu 5 in `MainMenu`.

---

### ✅ Acceptance Criteria & Verification

- [ ] NetBeans 13 compiles with 0 errors and 0 warnings under Java 8.
- [ ] Monthly report math test: Matches transaction count, total product units sold, and total revenue of completed orders for that month/year.
- [ ] Ranking test: If Product P01 has 250 units sold and P02 has 150 units, P01 ranks first (**BR21**).
- [ ] Customer spending test: Correctly ranks customers by total money spent (**BR22**).
- [ ] Low stock test: Correctly lists all food products whose total available stock $\le 5$ (**BR19**).
- [ ] All table and summary outputs fit strictly within 80 columns without broken borders.
- [ ] PR created targeting `main`: `feat(report): implement monthly revenue and ranking reports with ReportMenu`.
