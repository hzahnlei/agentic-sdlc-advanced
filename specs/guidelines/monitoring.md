# Monitoring Guidelines

CLI tools and libraries require no monitoring. Apply the points below to services.

## Health Endpoints

Expose two endpoints for the orchestration platform:

- `/healthz/live` — liveness: is the process running?
- `/healthz/ready` — readiness: is the service ready to accept traffic?

Report not-ready during startup and graceful shutdown; never report not-live unless truly broken.

## Metrics

- Emit Prometheus-format metrics.
- Define all metric names and label keys in a global constants file.
- Minimum metrics to expose:

| Metric                         | Type      |
| ------------------------------ | --------- |
| Request count (by status code) | Counter   |
| Request duration               | Histogram |
| In-flight requests             | Gauge     |
| Dependency health (DB, broker) | Gauge     |

## Alerting

Alert on: error rate, latency (p50/p95/p99), saturation (CPU, memory, queue depth).

## Distributed Tracing

Instrument all inter-service calls with OpenTelemetry. Propagate `traceId` in logs and metrics for correlation.
