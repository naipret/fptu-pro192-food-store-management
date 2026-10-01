---
name: Development Task Issue
about: Task tracking template conforming to Conventional Commits
title: 'feat(<module>): <task description>'
labels: ['task']
assignees: ''
---

### 👤 Ownership & Roles

- **Assignee:**
- **Reviewer:**
- **Target Module:**

---

### 🎯 Objective & Summary
<!-- Describe what needs to be implemented or modified in this task. -->

---

### 📋 Enforced Business Rules (Mandatory In-Code Comments)
<!-- List all Business Rules enforced in this task. E.g., BR3, BR7, BR24 -->
- `BRX`: <!-- Describe rule -->

---

### 🛠 Technical Specifications & Target Files
<!-- List specific files to create or modify -->
1. `src/...`
2. `src/...`

*Implementation Guidelines:*

- Strictly comply with Java 8 syntax (no `var`, no `List.of()`).
- Add complete Javadoc comments (`@param`, `@return`, `@throws`).
- Add explicit `// BRX:` reference comments above business logic methods.
- Console text must be in Vietnamese, within 80-column width, using basic ASCII characters.

---

### ✅ Acceptance Criteria (Checklist)

- [ ] Compiles cleanly in NetBeans 13 with 0 errors and 0 warnings.
- [ ] Business logic and validations pass all edge cases.
- [ ] Persistence operations execute atomically via CSV.
- [ ] PR created targeting `main` branch.
