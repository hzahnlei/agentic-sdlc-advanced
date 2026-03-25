# ADR-001: Use Structured Logging Instead of Plain Text Logging

| Field             | Value         |
| ----------------- | ------------- |
| **Date**          | 2026-02-28    |
| **Status**        | Accepted      |
| **Deciders**      | Project owner |
| **Supersedes**    | —             |
| **Superseded by** | —             |

## Context and Problem Statement

Our services emit log output that is consumed in two ways: by developers reading a terminal during
development, and by a log aggregation platform (e.g., Grafana Loki, Elasticsearch) in production.

With plain text logging, log lines look like this:

```
ERROR 2026-02-28 14:03:21 Failed to process order 4711: timeout after 5000ms
```

Searching, filtering, and alerting on plain text requires fragile regex patterns and breaks
whenever the message wording changes. The team needs to be able to filter by order ID, error type,
or duration without parsing free-form strings.

## Decision Drivers

- Log entries must be filterable and alertable by structured fields (order ID, duration, error type) without regex.
- Developer experience: human-readable output in local development.
- Consistency: field names must be uniform across all services for cross-service correlation.

## Considered Options

1. Plain text logging (`printf`-style)
2. Semi-structured logging (fixed prefix + free text)
3. Structured logging (key-value pairs / JSON in production)

## Decision Outcome

**Chosen option: Structured logging (option 3)**, because it makes every field individually
queryable and alertable without any parsing, and supports pretty-printing for local development.

### Positive Consequences

- Log entries can be filtered, aggregated, and alerted on by any field (`order_id`, `duration_ms`)
  without regex.
- Adding a new field to a log entry is a non-breaking change; changing the message wording is
  irrelevant to queries.
- Consistent field names across services make cross-service correlation straightforward.
- Development mode can pretty-print the same structured data for human readability.

### Negative Consequences / Risks

- Slightly more verbose to write than `log.Printf("Failed: %v", err)`.
- Developers must remember to use the key-value API and resist interpolating values into message strings.
- A global constants file for field keys must be maintained; discipline required to keep it up to date.

## Pros and Cons of the Options

### Option 1 — Plain text logging

- **Pro**: Simple to write.
- **Con**: Unqueryable without fragile regex; breaks alerting when message wording changes.

### Option 2 — Semi-structured logging

- **Pro**: Marginally better than plain text for human reading.
- **Con**: Still not reliably machine-parseable; regex required for any field extraction.

### Option 3 — Structured logging

- **Pro**: Every field individually queryable and alertable.
- **Pro**: Non-breaking to add fields or change message wording.
- **Con**: Slightly more verbose API.

## Notes

Libraries never log — they return errors to the caller. Only the application entry point and
service handlers emit log output, keeping structured logging contained to executable artifacts.
