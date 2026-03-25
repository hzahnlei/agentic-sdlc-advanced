# Business Rules

Business rules define the invariants and constraints of the domain.
They must be enforced regardless of which use case or API endpoint triggers them —
typically in the domain layer, never only in controllers or validators.

<!--
For the AI coding assistant:
- Every rule listed here MUST be implemented in the domain layer.
- Rules tagged [INVARIANT] must hold at all times; enforce in entity/aggregate constructors and mutators.
- Rules tagged [POLICY] are enforced at use-case boundaries.
- When generating code, add a reference comment: // BR-NNN
-->

<!--
RULE TEMPLATE — copy this block for each new rule, keep the ID unique and sequential.

## BR-NNN: <Rule Name>

**Category**: Invariant | Policy | Calculation | Authorization

**Statement**: One precise, unambiguous sentence.

**Rationale**: Why this rule exists. Business or technical reason.

**Applies to**: Entity / Aggregate / Use Case (use exact glossary term)

**Violated when**: Concrete condition that breaks the rule.

**Error code**: `TODO_ERROR_CODE` — "Human-readable error message"

| Valid | Invalid |
|-------|---------|
| ...   | ...     |
-->

## BR-001: \<Rule Name\>

**Category**: Invariant

**Statement**: TODO — one precise, unambiguous sentence.

**Rationale**: TODO

**Applies to**: TODO — Entity or Aggregate name (use exact glossary term)

**Violated when**: TODO — concrete condition

**Error code**: `TODO_ERROR_CODE` — "TODO error message shown to caller"

| Valid | Invalid |
| ----- | ------- |
| TODO  | TODO    |
