package fptu.pro192.foodstoremanagement.product;

import fptu.pro192.foodstoremanagement.model.Batch;
import fptu.pro192.foodstoremanagement.model.DryFood;
import fptu.pro192.foodstoremanagement.model.Food;
import fptu.pro192.foodstoremanagement.repository.ProductRepository;
import fptu.pro192.foodstoremanagement.service.InsufficientStockException;
import fptu.pro192.foodstoremanagement.service.ProductService;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Verification test suite for Task #5 (ProductService and FEFO deduction).
 * Pure Java 8, zero third-party dependencies.
 */
public class ProductServiceTest {

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   RUNNING TASK #5 PRODUCT SERVICE VERIFICATION TESTS           ");
        System.out.println("================================================================");

        testAddProductValidation();
        testFEFODeductionAlgorithm();
        testLowStockFilter();
        testExpiredAndNearExpiryQueries();
        testSelectiveUpdateAndReplenishment();
        testSearch();

        System.out.println("\n----------------------------------------------------------------");
        System.out.printf("Test Summary: %d passed, %d failed.\n", totalPassed, totalFailed);
        System.out.println("----------------------------------------------------------------");

        if (totalFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s\n", testName);
            totalPassed++;
        } else {
            System.err.printf("  [FAIL] %s\n", testName);
            totalFailed++;
        }
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        boolean match = (expected == null && actual == null)
                || (expected != null && expected.equals(actual));
        if (match) {
            System.out.printf("  [PASS] %s\n", testName);
            totalPassed++;
        } else {
            System.err.printf("  [FAIL] %s: Expected [%s] but got [%s]\n", testName, expected, actual);
            totalFailed++;
        }
    }

    private static void testAddProductValidation() {
        System.out.println("\n--- 1. Testing Add Product Validation (BR1, BR3, BR5) ---");
        String pPath = "data/test_service_p1.csv";
        String bPath = "data/test_service_b1.csv";
        cleanFiles(pPath, bPath);

        try {
            ProductRepository repo = new ProductRepository(pPath, bPath);
            ProductService service = new ProductService(repo);

            Food f = new DryFood("P00001", "Mi Hao Hao", "DRY", "Goi", 8000.0);
            Batch b = new Batch("B00001", "P00001", LocalDate.now(), LocalDate.now(), LocalDate.now().plusMonths(6), 100);

            service.addProduct(f, b, LocalDate.now());
            assertEquals("Product added to repository", 1, service.getAllProducts().size());
            assertEquals("Product total stock includes initial batch", 100, service.findById("P00001").getTotalStock());

            // Duplicate ID rejection (BR1)
            boolean duplicateRejected = false;
            try {
                Food dup = new DryFood("P00001", "Mi Omachi", "DRY", "Goi", 12000.0);
                service.addProduct(dup, null, LocalDate.now());
            } catch (IllegalArgumentException e) {
                duplicateRejected = true;
            }
            assertTrue("Rejects duplicate product ID (BR1)", duplicateRejected);

        } finally {
            cleanFiles(pPath, bPath);
        }
    }

    private static void testFEFODeductionAlgorithm() {
        System.out.println("\n--- 2. Testing FEFO Batch Allocation Algorithm (BR10, BR11, BR14, BR24) ---");
        String pPath = "data/test_service_p2.csv";
        String bPath = "data/test_service_b2.csv";
        cleanFiles(pPath, bPath);

        try {
            ProductRepository repo = new ProductRepository(pPath, bPath);
            ProductService service = new ProductService(repo);

            LocalDate today = LocalDate.of(2026, 10, 8);

            Food food = new DryFood("P00001", "Mi Hao Hao", "DRY", "Goi", 8000.0);
            // Batch A: Expiring earlier (2026-10-15), 5 units
            Batch batchA = new Batch("B00001", "P00001", today, today, LocalDate.of(2026, 10, 15), 5);
            // Batch B: Expiring later (2026-10-25), 5 units
            Batch batchB = new Batch("B00002", "P00001", today, today, LocalDate.of(2026, 10, 25), 5);
            // Batch C: Already expired (2026-10-01), 10 units (BR11)
            Batch batchC = new Batch("B00003", "P00001", today.minusDays(30), today.minusDays(30), LocalDate.of(2026, 10, 1), 10);

            food.addBatch(batchA);
            food.addBatch(batchB);
            food.addBatch(batchC);
            repo.save(food);

            // Active stock should be 5 + 5 = 10 (excluding 10 expired units)
            assertEquals("Active stock excludes expired batch", 10, food.getTotalStock(today));

            // Acceptance Criteria: Selling 6 units empties Batch A (5 units) and deducts 1 unit from Batch B
            service.deductStockFEFO("P00001", 6, today);

            assertEquals("Batch A is completely emptied (0 units)", 0, batchA.getQuantity());
            assertEquals("Batch B has 4 units remaining", 4, batchB.getQuantity());
            assertEquals("Total active stock remaining is 4", 4, food.getTotalStock(today));
            assertEquals("Expired batch quantity untouched (10 units)", 10, batchC.getQuantity());

            // BR10: Attempting to deduct more than active stock throws InsufficientStockException
            boolean insufficientStockThrown = false;
            try {
                service.deductStockFEFO("P00001", 10, today);
            } catch (InsufficientStockException e) {
                insufficientStockThrown = true;
            }
            assertTrue("Deducting > active stock throws InsufficientStockException (BR10)", insufficientStockThrown);

            // Non-positive quantity throws IllegalArgumentException
            boolean invalidQtyThrown = false;
            try {
                service.deductStockFEFO("P00001", 0, today);
            } catch (IllegalArgumentException e) {
                invalidQtyThrown = true;
            }
            assertTrue("Deducting 0 throws IllegalArgumentException", invalidQtyThrown);

            // BR14: Verify atomic disk persistence
            ProductRepository reloaded = new ProductRepository(pPath, bPath);
            Food reloadedFood = reloaded.findById("P00001");
            assertEquals("Reloaded repository reflects post-deduction stock (4 units)", 4, reloadedFood.getTotalStock(today));

        } finally {
            cleanFiles(pPath, bPath);
        }
    }

    private static void testLowStockFilter() {
        System.out.println("\n--- 3. Testing Low Stock Filter (BR19) ---");
        String pPath = "data/test_service_p3.csv";
        String bPath = "data/test_service_b3.csv";
        cleanFiles(pPath, bPath);

        try {
            ProductRepository repo = new ProductRepository(pPath, bPath);
            ProductService service = new ProductService(repo);

            LocalDate today = LocalDate.now();

            // P00001: 3 units (low stock <= 5)
            Food f1 = new DryFood("P00001", "Gao ST25", "DRY", "Kg", 40000.0);
            f1.addBatch(new Batch("B00001", "P00001", today, today, today.plusMonths(12), 3));

            // P00002: 5 units (low stock threshold boundary <= 5)
            Food f2 = new DryFood("P00002", "Duong Trang", "DRY", "Kg", 25000.0);
            f2.addBatch(new Batch("B00002", "P00002", today, today, today.plusMonths(12), 5));

            // P00003: 20 units (normal stock)
            Food f3 = new DryFood("P00003", "Dau An Neptune", "DRY", "Chai", 55000.0);
            f3.addBatch(new Batch("B00003", "P00003", today, today, today.plusMonths(12), 20));

            repo.save(f1);
            repo.save(f2);
            repo.save(f3);

            List<Food> lowStock = service.getLowStockProducts();
            assertEquals("Low stock count is 2 (BR19: stock <= 5)", 2, lowStock.size());
            assertTrue("P00001 is included in low stock", lowStock.stream().anyMatch(p -> p.getId().equals("P00001")));
            assertTrue("P00002 is included in low stock", lowStock.stream().anyMatch(p -> p.getId().equals("P00002")));

        } finally {
            cleanFiles(pPath, bPath);
        }
    }

    private static void testExpiredAndNearExpiryQueries() {
        System.out.println("\n--- 4. Testing Expired and Near-Expiry Batches Queries (BR11.1, BR20) ---");
        String pPath = "data/test_service_p4.csv";
        String bPath = "data/test_service_b4.csv";
        cleanFiles(pPath, bPath);

        try {
            ProductRepository repo = new ProductRepository(pPath, bPath);
            ProductService service = new ProductService(repo);

            LocalDate today = LocalDate.of(2026, 10, 8);

            Food f = new DryFood("P00001", "Sua Hop", "DRY", "Hop", 30000.0);
            // Expired 2 days ago
            Batch bExpired = new Batch("B00001", "P00001", today.minusDays(30), today.minusDays(30), today.minusDays(2), 10);
            // Near expiry (expires in 4 days <= 7 days)
            Batch bNear = new Batch("B00002", "P00001", today.minusDays(10), today.minusDays(10), today.plusDays(4), 15);
            // Fresh (expires in 30 days)
            Batch bFresh = new Batch("B00003", "P00001", today, today, today.plusDays(30), 50);

            f.addBatch(bExpired);
            f.addBatch(bNear);
            f.addBatch(bFresh);
            repo.save(f);

            // BR11.1: Expired batches query
            List<Batch> expired = service.getExpiredBatches(today);
            assertEquals("Expired batch count is 1", 1, expired.size());
            assertEquals("Expired batch is B00001", "B00001", expired.get(0).getBatchId());

            // BR20: Near-expiry batches query (within 7 days)
            List<Batch> nearExpiry = service.getNearExpiryBatches(today);
            assertEquals("Near-expiry batch count is 1", 1, nearExpiry.size());
            assertEquals("Near-expiry batch is B00002", "B00002", nearExpiry.get(0).getBatchId());

        } finally {
            cleanFiles(pPath, bPath);
        }
    }

    private static void testSelectiveUpdateAndReplenishment() {
        System.out.println("\n--- 5. Testing Product Update and Replenishment (Tasks B2, B14) ---");
        String pPath = "data/test_service_p5.csv";
        String bPath = "data/test_service_b5.csv";
        cleanFiles(pPath, bPath);

        try {
            ProductRepository repo = new ProductRepository(pPath, bPath);
            ProductService service = new ProductService(repo);

            LocalDate today = LocalDate.now();
            Food f = new DryFood("P00001", "Ca Phe Goi", "DRY", "Hop", 45000.0);
            Batch b = new Batch("B00001", "P00001", today, today, today.plusMonths(6), 20);
            f.addBatch(b);
            repo.save(f);

            // Task B2: Update price selectively
            service.updateProduct("P00001", 50000.0, null);
            assertEquals("Price updated to 50,000 VND", 50000.0, repo.findById("P00001").getPrice());
            assertEquals("Stock remains 20", 20, repo.findById("P00001").getTotalStock());

            // Task B14: Replenish existing batch (+15 units)
            service.replenishStock("P00001", "B00001", 15);
            assertEquals("Batch quantity replenished to 35", 35, repo.findById("P00001").getTotalStock());

            // Task B14: Attach second batch to product
            Batch b2 = new Batch("B00002", "P00001", today, today, today.plusMonths(12), 40);
            service.addBatchToProduct("P00001", b2);
            assertEquals("Total stock now 75 across 2 batches", 75, repo.findById("P00001").getTotalStock());
            assertEquals("Product has 2 batches", 2, repo.findById("P00001").getBatches().size());

        } finally {
            cleanFiles(pPath, bPath);
        }
    }

    private static void testSearch() {
        System.out.println("\n--- 6. Testing Product Search (Task B4) ---");
        String pPath = "data/test_service_p6.csv";
        String bPath = "data/test_service_b6.csv";
        cleanFiles(pPath, bPath);

        try {
            ProductRepository repo = new ProductRepository(pPath, bPath);
            ProductService service = new ProductService(repo);

            repo.save(new DryFood("P00001", "Mi Hao Hao Tom Chua Cay", "DRY", "Goi", 8000.0));
            repo.save(new DryFood("P00002", "Sua Tuoi Tiet Trung", "CHILLED", "Hop", 35000.0));
            repo.save(new DryFood("P00003", "Tom Su Dong Lanh", "FROZEN", "Khay", 120000.0));

            // Search by name substring
            List<Food> nameMatches = service.search("Tom");
            assertEquals("Search 'Tom' matches 2 products (Mi Hao Hao Tom, Tom Su)", 2, nameMatches.size());

            // Search by category
            List<Food> catMatches = service.search("FROZEN");
            assertEquals("Search 'FROZEN' matches 1 product", 1, catMatches.size());

            // Search by ID substring
            List<Food> idMatches = service.search("P00002");
            assertEquals("Search 'P00002' matches 1 product", 1, idMatches.size());

            // Null or blank keyword returns all active
            assertEquals("Null search returns all 3 products", 3, service.search(null).size());
            assertEquals("Empty search returns all 3 products", 3, service.search("   ").size());

        } finally {
            cleanFiles(pPath, bPath);
        }
    }

    private static void cleanFiles(String pPath, String bPath) {
        new File(pPath).delete();
        new File(bPath).delete();
        new File(pPath + ".tmp").delete();
        new File(bPath + ".tmp").delete();
    }
}
