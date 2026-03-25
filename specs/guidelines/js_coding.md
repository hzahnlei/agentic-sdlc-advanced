# JavaScript / TypeScript Coding Guidelines

Applies to all services that include a frontend or a Node.js/Deno backend component.
Language-agnostic guidance is in the [General Coding Guidelines](general_coding.md).
Error handling is in the [Error Handling Guidelines](error_handling.md).

## TypeScript vs. JavaScript

- Always use TypeScript.
- Enable strict mode in `tsconfig.json`: `"strict": true`.
- Avoid `any`; if unavoidable, document the reason with a comment.
- Use `unknown` for external/untyped values (`JSON.parse`, APIs, user input).

## Code Style

- Enforce formatting with **Prettier** and linting with **ESLint** (configured in CI).
- Naming conventions:
  - `camelCase` for variables and functions.
  - `PascalCase` for classes, interfaces, type aliases, and React/Vue components.
  - `SCREAMING_SNAKE_CASE` for module-level constants.
  - Files: `kebab-case.ts` for modules, `PascalCase.tsx` for UI components.
- Keep files ≤ 300 lines.
- No trailing whitespace; end every file with a single newline.

## Modules and Imports

- Use ES modules (`import`/`export`) exclusively — no CommonJS `require()`.
- Group imports in this order, separated by blank lines: external packages → internal aliases → relative paths.
- Prefer named over default exports (better refactoring support).
- Avoid circular dependencies; enforce with ESLint `import/no-cycle` or a custom architecture check.

## Type Safety

- Prefer `interface` over `type` for object shapes; use `type` for unions, intersections, and mapped types.
- Avoid type assertions (`as`); derive types from runtime validators instead.
- Use **zod**, **valibot**, or similar to validate and type external data at the boundary.
- Never widen to `any` to satisfy the compiler — fix the type.

## Async Programming

- Use `async`/`await` over raw Promise chains.
- Every Promise must be awaited or explicitly handled — no floating Promises.
- Always handle rejections — no unhandled-rejection errors in production.
- Use `Promise.all` for independent concurrent operations; `Promise.allSettled` when partial failure is acceptable.
- Avoid mixing `async`/`await` and `.then()`/`.catch()` in the same function.

## Error Handling

- Never swallow errors — propagate or handle explicitly (see [Error Handling Guidelines](error_handling.md)).
- Create typed custom error classes that extend `Error`; include a machine-readable `code` property.
- Handle errors at boundaries only (API handlers, event listeners, top-level entry points).
- No empty `catch` blocks; at minimum, log the error before re-throwing or recovering.

## Immutability

- Prefer `const` over `let`; never use `var`.
- Treat arrays and objects as immutable — spread (`[...arr]`, `{...obj}`), `structuredClone()`, or library utilities; no in-place mutation.
- In React/Vue, never mutate state directly — always produce new values.

## Testing

- Use **Jest** or **Vitest** as the test runner.
- Co-locate unit tests next to the module being tested: `foo.ts` → `foo.test.ts`.
- Unit-test pure functions and business logic; integration-test HTTP routes and DB interactions.
- Mock only external I/O (HTTP clients, DB adapters) — never internal business logic.
- Aim for high coverage on domain/use-case code; UI snapshot tests are a last resort.

## Frontend Components

- One component per file; file name matches the component name.
- Type all props via interfaces; no untyped prop objects.
- Keep components small — extract sub-components when render logic grows complex.
- Lift shared state only as far up the tree as necessary; prefer local state.
- Use composition and hooks/composables over class inheritance.
- Avoid side effects in render functions; use lifecycle hooks or effect primitives.

## Build and Tooling

- Use **Vite** or **esbuild** for bundling; avoid Webpack for new projects.
- Specify the required Node.js version in `package.json` under `engines.node`.
- Separate `dependencies` (runtime) from `devDependencies` (build/test) correctly.
- Never import `node_modules` paths directly in browser code — use package names.
- Commit `package-lock.json` or `pnpm-lock.yaml`; do not commit `node_modules`.

## Security

- Sanitize all user input before display or persistence; use a dedicated sanitization library (e.g., **DOMPurify** for HTML).
- Never use `eval()`, `new Function()`, or `setTimeout(string, ...)`.
- Validate and type all data from external APIs at the service boundary.
- Use HTTPS for all external requests; never downgrade to HTTP in production code.
- Run `npm audit` / `pnpm audit` in CI and fail on high-severity vulnerabilities.
- Never store secrets or tokens in source code or `localStorage` — use environment variables and secure cookies.
