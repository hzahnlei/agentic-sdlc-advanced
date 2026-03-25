# System Context

<!--
This document describes the technical environment of this system.
For the AI coding assistant: use the diagrams below to understand which
dependencies and integrations exist. Never introduce dependencies that
are not listed here without explicit discussion.
-->

## C4 Context Diagram

The AI Agent (Claude, GPT-4o) is the sole user of the system. It connects to the MCP Task Server
at the start of a session to discover available tools, then uses those tools to insert tasks in bulk
and validate the results. The MCP Task Server persists all data in a PostgreSQL database and exposes
no human-facing UI.

```plantuml
@startuml
!include <C4/Context>

LAYOUT_WITH_LEGEND()

title System Context: MCP Task Server

Person(aiAgent, "AI Agent", "External AI system (e.g. Claude, GPT-4o). Discovers MCP tools, inserts tasks in bulk, and validates results.")

System(mcpServer, "MCP Task Server", "Spring Boot application. Exposes four MCP tools over HTTP+SSE: schema inspection, bulk task insertion, summary statistics, and tool discovery.")

SystemDb_Ext(postgres, "PostgreSQL", "Relational database. Persists Task records (id, title, description, status, timestamps).")

Rel(aiAgent, mcpServer, "Calls MCP tools", "HTTP+SSE / REST")
Rel(mcpServer, postgres, "Reads and writes Task records", "JDBC / JPA")
@enduml
```

## C4 Container Diagram

The system consists of two independently deployable units: the Spring Boot application and the
PostgreSQL database. The application implements the MCP server, registers the four tools
programmatically via `McpToolsConfiguration`, and communicates with the database over JDBC.
There are no other external services or message brokers.

```plantuml
@startuml
!include <C4/Container>

LAYOUT_WITH_LEGEND()

title Container Diagram: MCP Task Server

Person(aiAgent, "AI Agent", "External AI system (e.g. Claude, GPT-4o)")

System_Boundary(system, "MCP Task Server") {
    Container(app, "Spring Boot Application", "Java 25, Spring Boot 3.5.9, Spring AI 1.0.4", "Implements the MCP server. Registers four MCP tools over HTTP+SSE transport. Handles task schema exposure, bulk insertion, summary statistics, and tool discovery.")
    ContainerDb(db, "Task Database", "PostgreSQL 16", "Stores the 'task' table: id BIGSERIAL, title VARCHAR(255) NOT NULL, description TEXT, status VARCHAR(50) NOT NULL, created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ.")
}

Rel(aiAgent, app, "Calls MCP tools", "HTTP+SSE (GET /sse, POST /message) and REST")
Rel(app, db, "Reads and writes Task records", "JDBC / Spring Data JPA")
@enduml
```

## C4 Component Diagram

The Spring Boot application follows a four-layer clean architecture (domain → usecase → infra → app)
enforced by ArchUnit. The HTTP+SSE transport is fully managed by Spring AI — the application code
only registers tool handlers and delegates to use-case services. The domain and use-case layers have
no framework dependencies.

```plantuml
@startuml
!include <C4/Component>

LAYOUT_WITH_LEGEND()

title Component Diagram: Spring Boot Application

Person(aiAgent, "AI Agent")
ContainerDb(db, "Task Database", "PostgreSQL 16", "")

Container_Boundary(app, "Spring Boot Application") {

    Component(mcpAdapter, "HTTP+SSE Adapter", "Spring AI McpServer (auto-configured)", "Manages SSE connections (GET /sse). Routes incoming MCP messages (POST /message) to registered tool handlers.")

    Component(mcpConfig, "McpToolsConfiguration", "Spring @Configuration — infra/mcp", "Programmatically registers four MCP tools: mcp-schema-tasks, mcp-tasks, mcp-tasks-summary, mcp-help. Each tool delegates to the corresponding REST controller.")

    Component(controllers, "REST Controllers", "Spring MVC generated stubs — infra", "Delegate-pattern handlers for GET /v1/mcp/schema/tasks, POST /v1/mcp/tasks, GET /v1/mcp/tasks/summary, GET /v1/mcp/help. Generated from specs/APIs/provided/tasks.json.")

    Component(schemaProvider, "TaskSchemaProvider", "Spring Service — usecase/task", "Builds and returns the JSON Schema document that describes the TaskInput shape. Single source of truth for the insertion schema (ADR-005).")

    Component(insertUseCase, "InsertTasksUseCase", "Spring Service — usecase/task", "Validates a batch of TaskInput records and persists them atomically via the TaskRepository port.")

    Component(summaryUseCase, "GetTasksSummaryUseCase", "Spring Service — usecase/task", "Queries the database for task counts grouped by TaskStatus and constructs a TaskSummary read model.")

    Component(repoAdapter, "TaskRepositoryAdapter", "Spring Data JPA — infra/persistence", "Implements the TaskRepository output port. Maps between Task domain objects and TaskJpaEntity persistence objects.")

    Component(domain, "Task, TaskStatus", "Java record + enum — domain/task", "Task aggregate root with fields id, title, description, status, createdAt, updatedAt. TaskStatus enum: TODO, IN_PROGRESS, DONE.")
}

Rel(aiAgent, mcpAdapter, "Connects and sends MCP messages", "HTTP+SSE")
Rel(mcpAdapter, mcpConfig, "Dispatches tool calls to")
Rel(mcpConfig, controllers, "Delegates to REST handlers")
Rel(controllers, schemaProvider, "Uses (UC001)")
Rel(controllers, insertUseCase, "Uses (UC002)")
Rel(controllers, summaryUseCase, "Uses (UC003)")
Rel(insertUseCase, repoAdapter, "Persists tasks via TaskRepository port")
Rel(summaryUseCase, repoAdapter, "Queries counts via TaskRepository port")
Rel(repoAdapter, db, "Reads and writes", "JDBC / JPA")
Rel(insertUseCase, domain, "Creates Task aggregates")
Rel(summaryUseCase, domain, "Reads TaskStatus values")
@enduml
```

## Provided APIs

See [`specs/APIs/provided/`](../APIs/provided/) for machine-readable specifications.

| Spec File                  | Protocol                 | Description                                                                               |
| -------------------------- | ------------------------ | ----------------------------------------------------------------------------------------- |
| `APIs/provided/tasks.json` | REST / MCP over HTTP+SSE | Four MCP tools for the AI Agent: mcp-schema-tasks, mcp-tasks, mcp-tasks-summary, mcp-help |

## Consumed APIs

The MCP Task Server does not call any external APIs. It exposes tools only; all interactions are
initiated by the AI Agent.

| External System | Protocol | Spec Location | Notes |
| --------------- | -------- | ------------- | ----- |
| —               | —        | —             | None  |

## Infrastructure

| Concern           | Decision                                       |
| ----------------- | ---------------------------------------------- |
| Deployment target | Docker (containerised Spring Boot application) |
| Container runtime | Docker / Docker Compose (local development)    |
| CI/CD             | GitHub CI                                      |
| Local dev tooling | Devbox                                         |
| Database          | PostgreSQL 16                                  |
| Build tool        | Maven                                          |
