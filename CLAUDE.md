# agentic-sdlc-advanced

## Purpose

This repository implements an MCP server so that AI assistants can strore tasks in a relational databse.
This will also enable AI assistants to retrieve tasks form that databse.

## Domain Glossary

See [specs/glossary.md](specs/glossary.md) for the full glossary.

## Architecture

See [specs/architecture/system_context.md](specs/architecture/system_context.md) for diagrams and full context.

### What to avoid

- Edits outside this repository

## Key Invariants / Business Rules

See [specs/domain_model/business_rules.md](specs/domain_model/business_rules.md) for all rules.

## API Specs

Machine-readable specs live in `specs/APIs/`.

- **Provided APIs**: `specs/APIs/provided/` — generate server stubs from these specs (delegate pattern). Do not implement API handlers manually.
- **Consumed APIs**: `specs/APIs/consumed/` — generate client stubs from these specs. Do not write HTTP clients manually.

## Use Cases / Behaviour

BDD feature files in `specs/features/` are the authoritative description of system behaviour.
They are referenced from `specs/use_cases/` and executed as acceptance tests.

## Specs Structure

```text
specs/
├── glossary.md                    # Ubiquitous language
├── api/
│   ├── consumed/                  # OpenAPI/AsyncAPI/IDL of external APIs we call
│   └── provided/                  # OpenAPI/AsyncAPI/IDL of APIs we expose
├── domain_model/
│   ├── domain_model.md            # Entities, value objects, aggregates (+ PlantUML)
│   └── business_rules.md          # Domain invariants and policies (BR-NNN)
├── architecture/
│   └── system_context.md          # C4 context/container diagrams, infrastructure
├── use_cases/                     # One file per use case (Cockburn-style + sequence diagram)
├── features/                      # Gherkin BDD scenarios (executable acceptance tests)
├── guidelines/                    # General and project-specific guidelines
└── ADRs/                          # Architecture Decision Records (MADR format)
```

## Documentation

MkDocs with Material theme renders this project's documentation.

- **Config**: `mkdocs.yml` (root)
- **Docs root**: `specs/` — spec files are served directly as documentation pages
- **Build locally**: `python -m venv .venv && source .venv/bin/activate && pip install -r requirements-docs.txt && mkdocs serve`
- **Published by CI**: GitLab Pages job on `main` branch

Diagrams in `specs/` use PlantUML (fenced ` ```plantuml ` blocks) and Mermaid (fenced ` ```mermaid ` blocks).
API specs are rendered inline: OpenAPI via `<swagger-ui src="..."/>`, AsyncAPI via the asyncapi plugin.
Protobuf and Gherkin feature files are embedded as syntax-highlighted code blocks using snippets.

## Guidelines

@specs/guidelines/
