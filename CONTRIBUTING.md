# Contributing Guidelines

This document defines the version control, branching, commit message, issue tracking, and pull request workflow for the Food Store Management System (`PRO192`). Coding standards, naming conventions, and architectural specifications are documented separately in the [`docs/`](docs/) directory.

---

## 1. Git Branching Strategy

The `main` branch holds stable, compilable, and reviewed code. Direct pushes to `main` are prohibited. All development work must take place on dedicated branches created from the latest `main` branch.

Branch names use a prefix that reflects the nature of the change, followed by a short hyphen-separated identifier:

| Branch Prefix | Purpose | Example |
| :--- | :--- | :--- |
| `feature/<module>` | New functional module or user-facing capability | `feature/product-catalog` |
| `fix/<issue>` | Bug fix for a specific defect | `fix/fefo-stock-deduction` |
| `refactor/<scope>` | Structural code improvement without changing external behavior | `refactor/mvc-and-retail-domain` |
| `docs/<topic>` | Documentation creations or updates | `docs/standardize-specifications` |
| `chore/<task>` | Build configuration, seed data, or release maintenance | `chore/seed-retail-data` |

### Standard Branch Lifecycle

```bash
git checkout main
git pull origin main
git checkout -b feature/<short-description>
```

Keep branches short-lived and focused on a single issue so changes remain easy to review and merge without conflicts.

---

## 2. Commit Message Standards

All commit messages follow the Conventional Commits specification:

```text
<type>(<scope>): <concise summary in imperative mood>

<optional body explaining the rationale and context of the change>

Refs: #<issue-number>
```

### Allowed Commit Types

| Type | When to Use |
| :--- | :--- |
| `feat` | Introducing a new domain model, controller capability, or console menu view |
| `fix` | Resolving a logic bug, validation defect, or runtime error |
| `refactor` | Restructuring classes or methods for clarity without altering system behavior |
| `docs` | Creating or updating Markdown documentation and Javadoc specifications |
| `test` | Adding or updating automated verification tests under `src/test/java` |
| `chore` | Updating build scripts, IDE metadata, `.gitignore`, or seed CSV datasets |

### Commit Message Rules

1. Write the subject line in English using the imperative mood (`add`, `fix`, `update`, not `added` or `adds`).
2. Start the description after the colon with a lowercase letter and omit trailing periods.
3. Keep the subject line under 72 characters.
4. Make each commit atomic so that every commit leaves the project in a compilable state.

### Examples

```text
feat(model): implement Product and Batch composition with FEFO helpers
fix(controller): exclude expired batches when calculating available stock
docs(spec): define 80-column ASCII table layout formulas in UI_SPECIFICATION
```

---

## 3. GitHub Issues and Pull Request Workflow

Every code or documentation change must be tracked by a GitHub Issue and merged through a reviewed Pull Request.

### Issue Tracking

1. Before starting work on a task, verify that a corresponding GitHub Issue exists and is assigned to the responsible team member.
2. Each issue must state its objective, target files, enforced business rules (`BR1` through `BR27` where applicable), and testable acceptance criteria.

### Pull Request Process

1. Rebase or merge the latest `main` branch into your working branch and resolve any conflicts locally.
2. Verify that the project compiles cleanly on Java 8 (`ant clean compile test`) with zero errors and zero warnings.
3. Push the branch to the remote repository and open a Pull Request targeting `main`.
4. Link the Pull Request to its corresponding issue using `Closes #<issue-number>` in the description.
5. At least one team review (or project leader approval) is required before merging into `main`. Delete the working branch after the Pull Request is merged.
