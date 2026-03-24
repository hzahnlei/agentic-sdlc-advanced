# Provided APIs

This directory contains the machine-readable specifications of all APIs that
this system **provides** (exposes to consumers).

## Supported formats

| Format         | File extension    | Use when                                                    |
| -------------- | ----------------- | ----------------------------------------------------------- |
| OpenAPI 3.x    | `.yaml` / `.json` | REST APIs                                                   |
| AsyncAPI 3.x   | `.yaml` / `.json` | Events / messages published to consumers (Kafka, AMQP, ...) |
| Protobuf IDL   | `.proto`          | gRPC / binary protocols                                     |
| GraphQL schema | `.graphql`        | GraphQL APIs                                                |

## Index

<!--
Add one row per provided API.
Keep in sync with specs/architecture/system_context.md.
-->

| File        | Protocol | Consumers            | Notes |
| ----------- | -------- | -------------------- | ----- |
| `TODO.yaml` | REST     | TODO: Who calls this | TODO  |

## Guidelines

- **Spec first** — write or update the spec before implementing. The spec is the contract.
- **Do not implement handlers manually** — generate server stubs from the spec (delegate pattern).
- **Treat breaking changes as major version bumps** and document the decision in an ADR.
- Validate request/response against the spec in CI to prevent spec drift.
