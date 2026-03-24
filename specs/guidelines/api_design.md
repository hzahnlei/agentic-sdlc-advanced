# API Design Guidelines

## REST APIs

- **Resources:** plural nouns, lowercase (`/orders`, `/users/{id}`). HTTP verbs express the action.
- **Versioning:** version in the URL path (`/v1/orders`); never break a published version.
- **HTTP status codes:** use precisely:

| Situation                 | Code |
| ------------------------- | ---- |
| Success with body         | 200  |
| Created                   | 201  |
| Success without body      | 204  |
| Client / validation error | 400  |
| Unauthenticated           | 401  |
| Forbidden                 | 403  |
| Not found                 | 404  |
| Conflict                  | 409  |
| Unprocessable entity      | 422  |
| Server error              | 500  |

- **Error body:** consistent envelope: `{ "error": { "code": "INVALID_INPUT", "message": "...", "details": [...] } }`.
- **Pagination:** cursor-based preferred; offset+limit acceptable; always include total count and next cursor.
- **Idempotency:** PUT and DELETE must be idempotent; provide idempotency keys for POST when needed.
- **Document with OpenAPI:** keep spec in `specs/APIs/provided/` (APIs this service exposes) or `specs/APIs/consumed/` (external APIs this service calls); validate code against spec in CI.

## Asynchronous / Event APIs

- **Document with AsyncAPI:** keep spec in `specs/APIs/provided/` (APIs this service exposes) or `specs/APIs/consumed/` (external APIs this service calls).
- **Message envelope:** include `type`, `version`, `timestamp`, `correlationId`, `source`.
- **Versioning:** embed schema version in each message; consumers handle unknown versions gracefully.
- **Idempotent consumers:** design for at-least-once delivery; handle duplicates safely.
- **Dead letter queue:** route unprocessable messages to a DLQ for inspection and replay.

## Remote Procedure Calls (gRPC / Protobuf)

Prefer RPC for internal service-to-service communication where strong typing, performance, or streaming matter. Use REST for public or browser-facing APIs.

### Protocol Buffers

- Use proto3. Keep `.proto` files in `specs/APIs/provided/` (APIs this service exposes) or `specs/APIs/consumed/` (external APIs this service calls); they are the authoritative spec.
- Field names: `snake_case`. Service and message names: `PascalCase`. RPC method names: `PascalCase` verbs (`CreateOrder`, `ListUsers`).
- Include a package name with version: `package myservice.v1;`.

### Backward Compatibility — Strict Rules

- Never delete or renumber fields — mark removed fields `reserved`.
- Never rename fields (the name is part of JSON serialization).
- Never change a field's type.
- Adding new fields with default values is always safe.

### Method Types — choose deliberately

| Type                    | Use when                                    |
| ----------------------- | ------------------------------------------- |
| Unary                   | Default — single request, single response   |
| Server streaming        | Large result sets, live feeds               |
| Client streaming        | Bulk uploads, chunked ingestion             |
| Bidirectional streaming | Real-time, low-latency duplex communication |

### Error Handling

- Use gRPC status codes precisely — do not map everything to `UNKNOWN`:

| Situation          | Status code          |
| ------------------ | -------------------- |
| Validation failure | `INVALID_ARGUMENT`   |
| Not found          | `NOT_FOUND`          |
| Already exists     | `ALREADY_EXISTS`     |
| Auth failure       | `UNAUTHENTICATED`    |
| Forbidden          | `PERMISSION_DENIED`  |
| Rate limited       | `RESOURCE_EXHAUSTED` |
| Dependency down    | `UNAVAILABLE`        |
| Server bug         | `INTERNAL`           |

- Attach structured error details using `google.rpc.Status` + `ErrorInfo` / `BadRequest` etc.

### Deadlines and Timeouts

- Clients must always set a deadline — never rely on server-side timeouts alone.
- Servers must check `ctx.Done()` and abort early if the deadline has passed.
- Propagate deadlines to downstream calls; never reset them.

### Observability

- Implement logging, metrics, and tracing via interceptors — not inline in handler logic.
- Attach `correlationId` / `traceId` via gRPC metadata.
