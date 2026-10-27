# Contributing Guidelines

This document defines version control practices, branching strategy, commit message standards, issue tracking, and pull request workflows for this project. These guidelines follow industry-standard conventions and standard feature-branching workflows to ensure clear version history, reliable automation, and smooth team collaboration across projects.

---

## 1. Git Branching Strategy

The `main` branch maintains stable, compilable, and reviewed code. Direct commits and pushes to `main` are restricted. All development, enhancements, and bug fixes take place on dedicated branches created from the latest `main` branch.

### 1.1. Branch Naming Conventions

Branch names use a lowercase prefix indicating the nature of the work, followed by a forward slash (`/`) and a short, hyphen-separated description:

| Branch Prefix | Purpose / Scope | Example |
| :--- | :--- | :--- |
| `feature/<name>` | New feature, module, or user-facing capability | `feature/user-authentication` |
| `fix/<name>` | Bug fix or defect resolution | `fix/null-pointer-exception` |
| `refactor/<name>` | Code restructuring without altering external behavior | `refactor/modularize-controllers` |
| `docs/<name>` | Documentation creation, updates, or technical guides | `docs/api-specification` |
| `test/<name>` | Adding or enhancing unit, integration, or E2E tests | `test/order-processing` |
| `perf/<name>` | Performance optimizations and memory tuning | `perf/cache-queries` |
| `chore/<name>` | Tooling, build scripts, dependencies, or maintenance | `chore/update-dependencies` |

### 1.2. Standard Branch Lifecycle

```bash
git checkout main
git pull origin main

git checkout -b feature/<short-description>

git add <files>
git commit -m "feat(scope): add new capability"

git push -u origin feature/<short-description>
```

Keep branches focused and short-lived. A branch should address a single objective or issue to keep reviews fast and prevent merge conflicts.

---

## 2. Commit Message Standards

All commit messages must adhere to the [Conventional Commits](https://www.conventionalcommits.org/) specification.

### 2.1. Commit Message Structure

```text
<type>[optional scope][!]: <description>

[optional body]

[optional footer(s)]
```

- Header line contains the commit type, an optional scope enclosed in parentheses, an optional exclamation mark (`!`) for breaking changes, a required colon and space, and a concise summary.
- Body explains the motivation, context, and details of the change. Must be separated from the header by a blank line and wrapped at 72 characters.
- Footer references issue numbers (e.g., `Closes #123`, `Refs: #45`) or declares breaking changes (`BREAKING CHANGE: <explanation>`). Separated from the body by a blank line.

### 2.2. Allowed Commit Types

The allowed types align with Conventional Commits and standard commit linting configurations (`committed.toml`):

| Type | Description / When to Use | SemVer Correlation |
| :--- | :--- | :--- |
| `feat` | Introduces a new feature or user-facing functional capability | `MINOR` |
| `fix` | Patches a bug, defect, or unexpected runtime behavior | `PATCH` |
| `docs` | Documentation-only changes (README, guides, API docs, code comments) | None |
| `style` | Code formatting, whitespace, indentation, or semicolon adjustments (no logic change) | None |
| `refactor` | Code restructuring that neither fixes a bug nor adds a feature | None |
| `perf` | Code changes that improve execution performance or reduce resource consumption | `PATCH` (or `MINOR`) |
| `test` | Adding missing tests, updating test fixtures, or improving test coverage | None |
| `build` | Changes affecting build tools, project configurations, or external dependencies | None |
| `ci` | Changes to CI/CD workflows, automated pipelines, or deployment scripts | None |
| `chore` | Routine maintenance tasks, repository housekeeping, or data seeding | None |
| `revert` | Reverts a previously merged commit | Varies |

### 2.3. Commit Formatting Rules

1. Write the subject line in the imperative present tense (e.g., `add`, `fix`, `update`, `refactor`, not `added`, `fixes`, or `updating`).
2. Start the description immediately following the colon in lowercase (e.g., `feat: implement user signup`).
3. Do not end the subject line with a period (`.`) or exclamation mark (`!`).
4. Keep the subject line under 72 characters. Wrap body paragraphs at 72 characters with a hard maximum limit of 100 characters for long URLs or tokens.
5. Make each commit self-contained and focused on a single logical change. Every commit should leave the codebase in a compilable and passing state.
6. Indicate breaking changes by placing a `!` before the colon in the header (e.g., `feat(api)!: redesign authentication endpoint`) or by including `BREAKING CHANGE: <description>` in the footer (correlates with `MAJOR` in SemVer).

---

## 3. GitHub Issues and Pull Request Workflow

Every significant change should be tracked by an issue and integrated through a reviewed Pull Request.

### 3.1. Issue Tracking

1. Before starting development, verify that a corresponding GitHub Issue exists and is assigned.
2. Each issue should describe:
   - What problem needs to be solved or what capability needs to be added.
   - Target packages, components, or documentation files affected.
   - Any domain rules, interface contracts, or technical constraints.
   - A verifiable checklist confirming when the task is complete.

### 3.2. Pull Request Process

1. Rebase or merge the latest `main` branch into your working branch and resolve any conflicts locally.
2. Ensure all build steps, test suites, linters (including commit message linters), and quality checks pass cleanly with zero errors.
3. Push the branch to remote and open a Pull Request targeting `main`. Fill out the Pull Request template completely.
4. Link the PR to its corresponding issue using closing keywords (e.g., `Closes #<issue-number>`) in the description.
5. At least one peer review or maintainer approval is required before merging. After merging, delete the feature branch to keep the repository tidy.
