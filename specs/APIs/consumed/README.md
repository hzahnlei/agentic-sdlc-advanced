# Consumed APIs

This directory contains the machine-readable specifications of all external APIs
that this system **consumes** (calls as a client).

## Supported formats

| Format         | File extension    | Use when                                                |
| -------------- | ----------------- | ------------------------------------------------------- |
| OpenAPI 3.x    | `.yaml` / `.json` | REST APIs                                               |
| AsyncAPI 3.x   | `.yaml` / `.json` | Event-driven / message-based APIs (Kafka, AMQP, ...)    |
| Protobuf IDL   | `.proto`          | gRPC / binary protocols                                 |
| GraphQL schema | `.graphql`        | GraphQL APIs                                            |
| URL reference  | `*.url.txt`       | Spec lives in another repo — put the URL + version here |

## Index

<!--
Add one row per consumed API.
Keep in sync with specs/architecture/system_context.md.
-->

| File        | External System   | Protocol | Notes                    |
| ----------- | ----------------- | -------- | ------------------------ |
| `TODO.yaml` | TODO: System name | REST     | TODO: What we use it for |

## Guidelines

- **Do not implement client code manually** — generate stubs from the spec (e.g., OpenAPI Generator).
- **Pin the spec version** — update the file when the provider releases a new version and document breaking changes in an ADR.
- If the provider publishes their spec publicly, prefer a URL reference over copying the file, to avoid drift.
