# Cloud-Native Service Design Guidelines

Based on the [12-factor app](https://12factor.net) methodology.

## Configuration

- All configuration via environment variables; never baked into the image.
- Fail fast on startup if required configuration is missing.
- Use a well-established config library: `viper` (Go), `spring-dotenv` / `application.yml` (Java), `cxxopts` / `toml++` (C++).

## Statelessness

- Processes are stateless; all persistent state lives in backing services (DB, cache, object storage).
- Never write to local disk for persistent state.

## Startup and Shutdown

- **Graceful shutdown:** handle `SIGTERM`; stop accepting new requests, drain in-flight work, then exit cleanly.
- **Fast startup:** optimize for quick cold start to support rolling deploys and auto-scaling.
- **Readiness probe:** report not-ready during startup and drain; only report not-live if truly broken.

## Resilience

- Apply timeouts and retries with exponential backoff and jitter for all outbound calls.
- Use circuit breakers for downstream dependencies.
- Degrade gracefully when dependencies are unavailable — partial failure is preferable to total failure.

## Observability

- Structured logs to stdout (see Logging Guidelines).
- Metrics and tracing (see Monitoring Guidelines).
- Correlate logs, metrics, and traces via a shared `correlationId` / `traceId`.
