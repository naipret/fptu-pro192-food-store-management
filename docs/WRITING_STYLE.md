# Engineering Writing Style Guide

This style guide establishes standards for technical prose across project documentation, architectural records, pull requests, and commit summaries. Its goal is to maintain high information density, preserve reading comfort, and eliminate formulaic writing patterns common in AI-generated text.

---

## 1. Principles of Technical Voice

Technical writing exists to inform engineers with clarity and precision. It does not exist to market, persuade, or simulate conversational enthusiasm.

- State facts and mechanisms directly. Avoid throat-clearing introductory clauses.
- Use the active voice. Identify the actor or component performing the action.
- Write with an objective, neutral tone. Omit subjective praise, marketing jargon, and exaggerated metaphors.
- Conclude a section when the technical explanation is complete. Do not append ceremonial summary paragraphs that merely restate earlier points.
- Do not use decorative emojis in technical prose, headings, or lists.

---

## 2. Typography and Visual Noise

Formatting must clarify structural hierarchy. It must never be used to compensate for poor sentence structure or to force visual excitement.

### The Prohibition of Mid-Sentence Bold Text

Do not bold words inside running sentences to create emotional or conversational emphasis.

- Unacceptable: You can **quickly execute** the migration to **seamlessly optimize** system performance.
- Acceptable: You can run the migration script to optimize database performance.

Bold styling is reserved strictly for table column headers, safety-critical warning labels, and the single initial definition of an unfamiliar term.

### The Robotic Bullet Formula

AI writing frequently falls into the mechanical habit of prefixing every list item with a bolded noun followed by a colon. This turns lists into repetitive, fragmented walls of text.

- Unacceptable:
  - **Authentication**: Verifies user identity via tokens.
  - **Authorization**: Restricts access to privileged routes.
  - **Auditing**: Records user actions to the event log.

- Acceptable (Complete sentences):
  - The authentication layer verifies user identity using JSON Web Tokens.
  - Role-based authorization limits access to administrative routes.
  - The audit logger persists security events to an append-only table.

- Acceptable (Structured comparison table):

| Component | Responsibility |
| --- | --- |
| Authentication | Verifies user identity using JSON Web Tokens. |
| Authorization | Restricts access to administrative endpoints based on assigned roles. |
| Auditing | Writes security and lifecycle events to an append-only log. |

### Inline Code Spans

Wrap exact system identifiers in single backticks. This applies to file paths, function signatures, class names, variables, environment variables, commands, HTTP status codes, and configuration keys.

Do not wrap ordinary English words or abstract architectural concepts in backticks.

- Unacceptable: The `service layer` communicates with the `database`.
- Acceptable: The service layer invokes methods on `UserRepository` to query PostgreSQL.

---

## 3. Punctuation and Mechanical Rules

Ambiguous punctuation degrades the precision of technical specifications.

### Ampersands (`&`)

Spell out the word "and" in regular prose. The ampersand character is not a casual shorthand for the English conjunction.

The ampersand is permitted only in:

- Programming language operators (such as boolean logic or address-of syntax).
- Formal trademarked names (such as AT&T).
- Table column headers where horizontal cell width is physically constrained.

### Slashes (`/`)

Do not join nouns or adjectives with slashes. A slash obscures whether the joined elements represent alternatives, cumulative requirements, or a parent-child relationship.

- Unacceptable: Configure customer/order/inventory sync intervals.
- Acceptable: Configure synchronization intervals for customers, orders, and inventory.

### Parentheses

Keep critical technical qualifications within the main sentence structure. If a condition or constraint is necessary for correctness, state it openly. If it is trivial or obvious, delete it. Do not hide substantial technical caveats inside parenthetical remarks.

---

## 4. Anti-Patterns and Filler Elimination

The phrases below waste reader attention without providing technical value. Replace or remove them according to the rules below.

| Formulaic Pattern | Problem | Required Revision |
| --- | --- | --- |
| "It is important to note that..." | Empty throat-clearing | Delete the introductory phrase entirely and begin with the subject. |
| "Plays a crucial role in..." | Vague importance without explanation | Name the exact function, data flow, or interface contract. |
| "Heart of the system", "powerhouse" | Melodramatic metaphor | State the concrete architectural role (e.g., "coordinates batch deduction"). |
| "Seamless", "robust", "cutting-edge" | Unsubstantiated marketing praise | Provide concrete metrics, latency targets, or algorithmic guarantees. |
| "In order to..." | Wordy prepositional phrase | Replace with the single word "to". |
| "Let's explore", "Now let us delve into" | Conversational meta-commentary | Use a clear, descriptive technical section heading instead. |
| "In summary", "In conclusion" | Redundant wrap-up | Remove the paragraph entirely. Stop writing when the information ends. |

---

## 5. Pre-Merge Review Checklist

Before submitting technical documentation, specifications, or pull request descriptions, verify:

- No words are bolded mid-sentence for conversational emphasis.
- No bulleted lists rely on repetitive bold-term-plus-colon formulas.
- The word "and" is written out rather than replaced with an ampersand.
- No slash-separated compound words remain in body text.
- Technical terms, file names, and code symbols use accurate backtick formatting.
- Empty conversational phrases and marketing adjectives have been eliminated.
- The document concludes without redundant summary sections.
