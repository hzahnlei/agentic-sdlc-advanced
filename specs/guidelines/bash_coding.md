# Bash Guidelines

Write Bash scripts that are idiomatic, robust, and readable. ShellCheck compliance is mandatory — run in CI.

## Setup

Every script starts with:

```bash
#!/usr/bin/env bash
set -euo pipefail
```

- `-e` — exit immediately on error
- `-u` — treat unset variables as errors
- `-o pipefail` — propagate errors through pipes

## Variables

- Quote all expansions: `"$VAR"`, `"${VAR}"`.
- Use `readonly` for constants: `readonly MAX_RETRIES=3`.
- Use `local` for all variables inside functions.

## Functions

- Extract reusable logic into named functions.
- Define functions before they are called.
- Use explicit `return <code>`; never rely on the exit code of the last statement.

## Error Handling

- Print errors to `stderr`: `echo "Error: ..." >&2`.
- Use `trap` for cleanup: `trap cleanup EXIT`.
- Check required commands exist: `command -v docker >/dev/null || { echo "docker not found" >&2; exit 1; }`.

## Style

- Prefer `[[ ]]` over `[ ]` for conditionals.
- Prefer `printf` over `echo` for portability.
- Use `$(command)` for command substitution, not backticks.

## Unix Philosophy

- Silent on success; errors to `stderr`.
- Exit `0` on success; non-zero on failure.
- Do one thing; compose with pipes.
