## 📌 Pull Request Summary

### Description of Changes
<!-- Provide a clear, concise summary of what this PR introduces or fixes. -->

### Related Issue / Business Rules

- Closes: #X <!-- Issue Number -->
- Relevant Business Rules: `BRX` <!-- e.g., BR1, BR7, BR24 -->

---

## 🔍 Pre-Merge Verification Checklist

Please verify and check all items before requesting review:

- [ ] **Java 8 Compatibility**: Code strictly targets Java 8 (No `var`, `List.of()`, or Java 9+ features).
- [ ] **NetBeans 13 Verification**: Successfully compiled and tested inside NetBeans IDE 13 with 0 errors and 0 warnings.
- [ ] **Business Rules In-Code Documentation**: All methods enforcing business logic contain explicit `// BR-*:` reference comments.
- [ ] **Javadoc Documentation**: All new or modified classes and public methods include complete Javadoc (`@param`, `@return`, `@throws`).
- [ ] **Console TUI Standards**:
  - [ ] Terminal UI text, prompts, and table headers are written in **Vietnamese**.
  - [ ] All console views strictly stay within the **80-column width limit**.
  - [ ] Uses only basic ASCII characters (`+`, `-`, `=`, `|`, `*`).
  - [ ] Supports `[0]` to cancel/return cleanly.
- [ ] **CSV Persistence**: All file mutations use atomic writes and handle CSV quotation/escaping properly.

---

## 👥 Reviewer Checklist

- [ ] Code style and naming conventions followed.
- [ ] No race conditions or unhandled runtime exceptions.
