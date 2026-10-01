# Contributing Guidelines

This document defines the development workflow, commit standards, and coding conventions for all team members.

---

## 1. Git Branching Strategy

We follow a simplified GitHub Flow model centered around the `main` branch:

```markdown
[feature/<module>] ---> Pull Request ---> [main]
```

* **`main`**: The primary, production-ready branch. **Direct pushes are strictly prohibited.** All code must be merged via Pull Requests.
* **`feature/<module>`**: Short-lived feature branches branched directly off `main`.
  * Example: `feature/food-products`, `feature/customer-mgmt`, `feature/sales-reporting`

---

## 2. Commit Message Standards (Conventional Commits)

All commit messages must follow the [Conventional Commits v1.0.0](https://www.conventionalcommits.org/) specification.\
Also strictly atomic commits are required, meaning each commit should encapsulate a single logical change.

```markdown
<type>(<scope>): <short description>
```

### Allowed Types

* `feat`: A new feature (e.g., `feat(product): implement FrozenFood and batch deduction`)
* `fix`: A bug fix (e.g., `fix(phone): correct regex normalization for +84 prefix`)
* `docs`: Documentation updates only (e.g., `docs(br): update BR24 FEFO definition`)
* `style`: Code style, formatting, white-space changes (no production code change)
* `refactor`: Code restructuring without bug fixes or new features
* `test`: Adding or refactoring tests
* `chore`: Maintenance, repository setup, GitHub Actions updates

---

## 3. Mandatory In-Code Business Rules (BR) Documentation

Developers **must** annotate every business-critical method with corresponding BR codes:

```java
/**
 * Deducts stock from batches based on the First Expired, First Out (FEFO) principle.
 *
 * BR10: The quantity sold cannot exceed the available active stock of the product.
 * BR11: Expired food products cannot be sold under any circumstances.
 * BR14: Stock quantity is reduced immediately after a sales transaction is successfully confirmed.
 * BR24: When confirming a sale, stock must be deducted automatically from the active batch with the earliest valid expiration date. Depleted batches are closed, and remaining quantities cascade to subsequent earliest batches. Using FEFO (First Expired, First Out) inventory allocation.
 *
 * @param productId The 6-character product ID
 * @param quantity The integer quantity to deduct
 * @throws InsufficientStockException If requested quantity exceeds stock
 * @throws ExpiredProductException If active batches are expired
 */
public void deductStockFEFO(String productId, int quantity)
        throws InsufficientStockException, ExpiredProductException {
    // ...
}
```

---

## 4. GitHub Project Issues Convention

All development tasks on GitHub Projects must be tracked as Issues.

### Issue Title Format

Must follow the Conventional Commits format.

### Issue Body Template

Every issue must specify:

1. **Assignee**
2. **Reviewer**
3. **Module**
4. **Target Business Rules**: Specific `BR-*` identifiers
5. **Technical Tasks & Files**: List of specific files to create or modify
6. **Acceptance Criteria**: Verifiable checklist

---

## 5. Coding Standards

* **Java Version Compatibility**: Strictly Java 8.
* **Naming Conventions**:
  * Classes / Interfaces: `PascalCase` (`Food`, `ProductDao`)
  * Methods / Variables: `camelCase` (`calculateTotal`, `productPrice`)
  * Constants: `UPPER_SNAKE_CASE` (`MAX_COLUMN_WIDTH`, `DEFAULT_PAGE_SIZE`)
* **Javadoc**: Full Javadoc on all public classes and methods (`@param`, `@return`, `@throws`).
* **Console UI Language**: Vietnamese for all prompts, menus, and table columns.
* **Console UI Formatting**: Strictly within **80 columns width**, using basic ASCII characters only.
