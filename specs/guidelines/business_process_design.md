# Business Process Design Guidelines

Grounded in [Domain-Driven Design](https://www.domainlanguage.com/ddd/) (Evans),
[Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/) (Hohpe & Woolf),
and the [BPMN 2.0 standard](https://www.omg.org/spec/BPMN/2.0.2/About-BPMN).

## Process Modeling

- Model before you code — capture the process as a state diagram or [BPMN 2.0](https://www.omg.org/spec/BPMN/2.0.2/About-BPMN) activity diagram before writing implementation.
- Identify explicitly: trigger event, steps, terminal states (success and all failure modes), external actors.
- Use the domain's ubiquitous language for state and transition names — never technical abbreviations.
- Keep a state/transition table in `specs/processes/` alongside the code.

## State Management

- Entities with a lifecycle (order, payment, shipment) must have a closed, named set of states.
- Declare a valid-transition table; reject any illegal transition with a typed domain error.
- Persist state atomically with its version — never infer state from derived or aggregated data.
- State transitions are the unit of business work; keep side-effects (notifications, downstream calls) outside the transition itself.

## Distributed Transactions & Sagas

Distributed transactions must use the [Saga pattern](https://www.cs.cornell.edu/andru/cs711/2002fa/reading/sagas.pdf) (Garcia-Molina & Salem) rather than 2PC. Each step is a local transaction; failure triggers compensating transactions for all preceding steps. See also [microservices.io Saga](https://microservices.io/patterns/data/saga.html).

### Choreography vs. Orchestration

| Style             | Use when                                                                                       |
| ----------------- | ---------------------------------------------------------------------------------------------- |
| **Choreography**  | Steps are loosely coupled; each service reacts to domain events. Low coordination overhead.    |
| **Orchestration** | Complex branching, timeouts, or explicit rollback sequencing required. Prefer an orchestrator. |

- Design compensating transactions before the happy-path steps — they are equally important.
- Document the rollback sequence in `specs/processes/` as carefully as the forward sequence.
- Every saga step must be idempotent (see Idempotency below).

### Workflow Orchestration Framework

For complex, long-running sagas prefer a durable execution framework over hand-rolled state machines:

- **Go / Java**: [`Temporal`](https://temporal.io/) — workflows as code, durable execution, automatic retries and state replay; used in production by Stripe, Netflix, Datadog.
- **C++**: No official `Temporal` SDK exists; implement sagas using an explicit state machine persisted to the backing store, or use a message broker with a correlation-ID-keyed state table.

## Idempotency

- Every saga step and every process-initiating call must be safe to execute more than once.
- Use a deduplication / idempotency key (business ID or client-generated UUID) stored alongside the entity.
- Check for an existing result before executing; return the previous result on duplicate.
- Test idempotency explicitly: send the same request twice and assert the same outcome with no side-effect duplication.

## Error Handling & Recovery

- Define explicit failure states — not a single generic `ERROR` state.
- Failed steps that cannot be retried must transition to a state that enables manual intervention or compensation.
- Route unprocessable events to a dead-letter queue for inspection and replay (see [Enterprise Integration Patterns](https://www.enterpriseintegrationpatterns.com/)).
- Never silently drop a failed step — every failure must be observable via metrics and structured logs (see Monitoring Guidelines, Logging Guidelines).

## Timeouts & Escalation

- Every step that waits for an external event must have a timeout.
- Define the escalation path explicitly: retry, notify operator, abort, or trigger compensation.
- Track SLA deadlines as first-class domain data, separate from technical call timeouts.
- Emit a metric and structured log event when an SLA deadline is breached.

## Audit & Traceability

- Write an immutable audit event for every state transition: state-before, state-after, actor, timestamp, reason.
- Use a stable business identifier (e.g. `orderId`, `workflowId`) that correlates events across all services.
- Link the business identifier to the technical `correlationId` / `traceId` for end-to-end tracing (see Monitoring Guidelines).
- Never delete or mutate audit records.

## Testing

- Write BDD scenarios (see Testing Guidelines) for the happy path, each error path, and each compensation path.
- Derive tests from the state/transition table: cover every valid transition and assert every illegal transition is rejected.
- Include fault-injection tests: simulate step failures and assert that compensation runs to completion.
- Test timeout and escalation paths explicitly — do not leave them uncovered.
