# Glossary

This glossary defines the **ubiquitous language** of this bounded context.

<!--
For the AI coding assistant:
- All class names, method names, field names, and variable names MUST use
  the exact terms from the "Code Name" column.
- Never invent synonyms. If "Order" is the term, do not use "Purchase",
  "Request", or "Transaction" in code.
- The "Scope" column indicates where the term applies:
  D = Domain layer, A = Application layer, I = Infrastructure/API layer, ALL = everywhere.
-->

## Core Terms

| Term       | Code Name        | Definition                                                                            | Scope |
| ---------- | ---------------- | ------------------------------------------------------------------------------------- | ----- |
| TODO: Term | `TODO: CodeName` | TODO: Precise definition. What it is. What it is NOT. Distinguish from similar terms. | ALL   |
| TODO: Term | `TODO: CodeName` | TODO: Precise definition.                                                             | D     |

## Value Objects

Value objects have no identity — they are equal by value. List them here so the
AI generates them as immutable types, not mutable entities.

| Term       | Code Name      | Validation / Constraints                       |
| ---------- | -------------- | ---------------------------------------------- |
| TODO: Term | `TODO: VoName` | TODO: e.g., must be positive, format: ISO 8601 |

## Enumerations

| Enum Name        | Values                          | Notes                                      |
| ---------------- | ------------------------------- | ------------------------------------------ |
| TODO: StatusName | `VALUE_A`, `VALUE_B`, `VALUE_C` | TODO: allowed transitions, terminal states |
