# Logging Guidelines

## Principles

- Structured logging throughout — emit key-value pairs or JSON; never concatenate strings into log messages.
- Define all log field keys in a global constants file; never use inline string literals as keys.
- Log at the point of handling, not at intermediate layers. Log once; never log and re-throw.

## Log Levels

| Level   | Use when                                                   |
| ------- | ---------------------------------------------------------- |
| `debug` | Detailed diagnostics; disabled in production               |
| `info`  | Normal operational events (startup, shutdown, config load) |
| `warn`  | Unexpected but recoverable condition                       |
| `error` | Failure requiring attention; process continues             |
| `fatal` | Unrecoverable failure; process exits immediately           |

## By Artifact Type

- **Services:** `error` and `fatal` in production; `info` / `warn` / `debug` in development mode.
- **CLI tools:** errors to `stderr` only; silent on success.
- **Libraries:** never log — return errors to the caller instead.

## Recommended Libraries

- Go: `zap` or `slog`
- Java: `logback` + `logstash-logback-encoder`
- C++: `spdlog`
