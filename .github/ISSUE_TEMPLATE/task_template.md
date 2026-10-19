---
name: Development Task Issue
about: Task tracking template conforming to Conventional Commits
title: 'feat(<module>): <task description>'
labels: ['task']
assignees: ''
---

## Ownership and Scope

- Assignee:
- Reviewer:
- Target Package:

---

## Objective and Summary

<!-- Describe what needs to be implemented or modified in this task. -->

---

## Enforced Business Rules

<!-- List all Business Rules enforced in this task, e.g., BR3, BR7, BR24. -->

- `BRX`:

---

## Technical Specifications and Target Files

1. `src/main/java/fptu/pro192/foodstoremanagement/...`

Implementation requirements:

- Target Java SE 8 strictly (no `var`, `List.of()`, or Java 9+ APIs).
- Document all classes and methods using plain-text Javadoc (`@param`, `@return`, `@throws`) without HTML tags or single-line `//` comments.
- Include explicit `BR-*` traceability tags inside the Javadoc of methods that enforce business rules.
- Ensure all console prompts and table headers are written in English, fit within 80 columns, and use basic ASCII characters.

---

## Acceptance Criteria

- [ ] Compiles cleanly on JDK 8 (`ant clean compile test`) with 0 errors and 0 warnings.
- [ ] Business logic and field validations pass all edge cases.
- [ ] CSV persistence operations execute atomically with uniform header rows.
- [ ] Pull Request created targeting the `main` branch.
