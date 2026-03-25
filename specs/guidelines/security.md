# Security Guidelines

Requirements scale with exposure — apply relevant points based on artifact type.

## All Projects

- No secrets in source code or version control; use a secrets manager or environment variables.
- Pin dependency versions; run a vulnerability scanner in the pipeline (e.g., Trivy, OWASP Dependency-Check).
- Keep dependencies up to date; automate with Dependabot or Renovate.

## Services

- Validate and sanitize all input at system boundaries (APIs, queues, file uploads).
- Apply least-privilege to service accounts and database users.
- Enforce TLS for all external communication; verify certificates.
- Minimize attack surface — disable unused endpoints, features, and ports.
- Rotate credentials and certificates automatically.
- Audit-log security-relevant events (authentication, permission changes, sensitive data access).
