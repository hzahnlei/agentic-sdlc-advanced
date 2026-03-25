# specs/features/UC004-discover-mcp-tools.feature
#
# SINGLE SOURCE OF TRUTH for use case behaviour.
# This file is:
#   (1) referenced by specs/use_cases/UC004-discover-mcp-tools.md  (documentation)
#   (2) executed by the acceptance test suite                       (living documentation)
#
# Rules:
#   - Scenario IDs (e.g. UC004-S01) must match the use case document.
#   - Step definitions live in: src/test/java/io/github/hzahnlei/steps/
#   - Keep scenarios focused: one behaviour per scenario.

Feature: Discover Available MCP Tools
  As an AI Agent
  I want to retrieve the list of available MCP tools
  So that I can plan my interaction sequence without hard-coded knowledge of the server's capabilities

  Background:
    Given the MCP server is running

  # ---------------------------------------------------------------------------
  # Happy path — all tools present
  # ---------------------------------------------------------------------------

  Scenario: UC004-S01 Help response lists all four expected MCP tools
    When  the AI Agent requests the tool directory at "GET /v1/mcp/help"
    Then  the response status is 200
    And   the response contains a tool named "mcp-schema-tasks"
    And   the response contains a tool named "mcp-tasks"
    And   the response contains a tool named "mcp-tasks-summary"
    And   the response contains a tool named "mcp-help"

  # ---------------------------------------------------------------------------
  # Tool entry completeness
  # ---------------------------------------------------------------------------

  Scenario: UC004-S02 Each tool entry contains name, description, method, and path
    When  the AI Agent requests the tool directory at "GET /v1/mcp/help"
    Then  the response status is 200
    And   every tool entry has a non-blank "name"
    And   every tool entry has a non-blank "description"
    And   every tool entry has a non-blank "method"
    And   every tool entry has a non-blank "path"
