# agentic-sdlc-advanced

Agentic SDLC Advanced Excercise - Build an MCP server.

## Steps

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
