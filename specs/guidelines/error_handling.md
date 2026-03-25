# Error Handling Guidelines

Applies regardless of whether the language uses exceptions (Java, C++) or error values (Go).

- Never swallow errors — always propagate or handle explicitly.
- Add context when wrapping: what operation failed and on what input.
- Distinguish recoverable errors (propagate) from unrecoverable ones (abort/panic/terminate).
- Use typed or structured errors where the language permits.
- Never use exceptions for control flow; expected cases (e.g., "not found") belong in return values or typed results.
- Handle errors at boundaries only (API handlers, message consumers, top-level entry point) — not mid-stack.
- Translate low-level errors to domain errors at the `infra`→`usecase` boundary.
- Log at the point of handling — never at intermediate layers; never log and re-throw (causes duplicate log entries).
- Programmer errors (broken invariants) → abort/panic. Operational errors (timeouts, I/O) → propagate and handle.
