# agentic-sdlc-advanced

Agentic SDLC Advanced Excercise - Build an MCP server.

## Usage

### Prerequisites

- Docker and Devbox installed locally

### Running the Server

```bash
devbox shell    # Activate tool chain
make db         # Start PostgreSQL server
make run        # Start MCP server
```

Now register the server with you AI chat bot and you are good to go.

### Shutting down

Pess CTRL+C in the console to stop the MCP Tasks Server.
Then run the following command to shut down PostgreSQL.

```bash
make db-down
```

## Steps I Took to Create this Repository

### Creating the Repository

Cloning the project:

```bash
git clone https://github.com/hzahnlei/agentic-sdlc-advanced.git
cd agentic-sdlc-advanced
devbox init # Create empty devbox.json
code .
```

First prompt to create scaffolding:

```text
Add required scaffolding files and folders for a Java MCP development project:
- Tool chain managed with devbox 0.17.0
  - Java openSDK 25
  - GNU Make 4.4.1
  - Maven 3.9.12
- Java dependencies managed by Maven
  - Spring Boot 4.0.4, including Spring Web, Spring AI, and Spring Data JPA
  - JUnit 6.0.3
  - PostgreSQL client/driver
  - pom.xml
- Maven group ID = io.github.hzahnlei (reverse-domain convention for GitHub-hosted projects)
- Maven artifact ID = mcp-server
- Makefile to build, test, run server, and run PostgreSQL database (Docker, postgres:alpine)
- Folder specs/ for functional and technical specifications
- GitHub actions for CI builds will follow later

Assume Docker and Devbox to be installed locally (prerequisites).
```

```bash
devbox shell
code .
```

VS Code reports some issues in `pom.xml`.
The most recent versions I picked were not available on Maven Central.
I asked Claude to fix these.
Very quickly one finds himself in the fix-loop.

### Adding Spec Templates and System Prompt

I added template files to the `specs/` folder.
I also added `CLAUDE.md` as a system prompt.
`specs/guidelines` holds many guidelines for humans and AIs.
Therefore, `CLAUDE.md` is rudimentary and mainly referring to `specs/`.

### Deriving Specification from Agentic SDLC Advanced Tasks.pdf

I derived functional and technical specs from `Agentic SDLC Advanced Tasks.pdf` with the help of Claude.
The examples and guidelines I provided in the preceeding step, were guiding Claude in the process.

### Implementing the MCP Server

Now that the technical and fucnctional requirements are documented, I have Claude implement them:

```text
The specification under `specs/` defines functional and technical requirements.
Implement these accordingly.
- Adhere to the `specs/guidelines/`.
- Have Spring Data JPA create the database schema (no Flyway/Liquibase).
- Generate the REST controller from the openAPI spec (`specs/APIs/provided/tasks.json`).
  Add openAPI tooling to `pom.xml`.
- Also implement step definitions for BDD testing (`specs/APIs/provided/tasks.json`).
```

### Adding the MCP Server to Claude

Created file `.mcp.json` in project root to configure MCP server for this project only:

```json
{
  "mcpServers": {
    "mcp-task-server": {
      "type": "sse",
      "url": "http://localhost:8080/sse"
    }
  }
}
```

MCP server is now recognized as `mcp-task-server`.
However, I had to adapt the Tomcat configuration to fix a `SO_LINGER` for macOS and JDK ≥21.
Also, Claude used stdio as the transport, even though ADR-002 opted for HTTP/SSE.
Finally the Tasks MCP Server was recognized by Claude.

However, the schema created by Claud did not foresee a due date.
Therefore, I asked Claud to add it to the DB schema and API:

```text
Due dates for tasks are making sense.
Add a due date field to the database table and API specification.
```

Ultimately I could manually test the server with this prompt:

```text
Use the mcp-schema-tasks tool to read the task input schema.
Then use the mcp-tasks tool to insert 1,000 diverse tasks in batches of 50.
Tasks should be realistic and varied:

- status: mix of TODO, IN_PROGRESS, and DONE
- title: drawn from software development, operations, and business domains
- dueDate: spread across 2026-01 to 2026-12, roughly 20 % of tasks without a due date

When all batches are done, call mcp-tasks-summary to confirm the total count.
```

I used the above prompt as it is more precise than the one given in the requirements:

```text
Please inspect the task schema at /mcp/schema/tasks.
Then generate and insert 1000 diverse tasks with random statuses, titles, and due dates using the /mcp/tasks endpoint.
```

Checking the database:

```bash
docker exec -it <container_name> psql -U postgres -d mcpdb    # Use psql CLI client
\dt public.*    # Show all tables in schema "public"
SELECT count(*) FROM tasks;    # How many tasks are there?
```
