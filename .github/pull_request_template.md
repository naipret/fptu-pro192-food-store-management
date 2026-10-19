## Pull Request Summary

### Description of Changes

<!-- Provide a clear, concise summary of what this PR introduces or fixes. -->

### Related Issue and Business Rules

- Closes: #X
- Relevant Business Rules: `BRX`

---

## Pre-Merge Verification Checklist

- [ ] Java 8 Compatibility: Code targets Java SE 8 strictly with no Java 9+ APIs.
- [ ] Build and Test Verification: `ant clean compile test` completes with 0 errors and 0 warnings.
- [ ] Javadoc and Traceability: All classes and methods use plain-text Javadoc (`@param`, `@return`, `@throws`) with `BR-*` tags and zero single-line `//` comments.
- [ ] Console TUI Standards:
  - [ ] All terminal text, prompts, and table headers are written in English.
  - [ ] All console views stay strictly within the 80-column width limit.
  - [ ] Borders use only basic ASCII characters (`+`, `-`, `=`, `|`, `*`).
  - [ ] Menus and input flows support `[0]` to cancel or return cleanly.
- [ ] CSV Persistence: File writes execute atomically via `CsvUtil.writeAtomic` with uniform header rows.

---

## Reviewer Checklist

- [ ] Architecture boundaries (`model`, `view`, `controller`, `util`) and naming conventions are respected.
- [ ] No unhandled runtime exceptions or resource leaks exist.
