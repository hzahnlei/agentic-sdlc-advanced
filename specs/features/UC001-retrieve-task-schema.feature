# specs/features/UC001-retrieve-task-schema.feature
#
# SINGLE SOURCE OF TRUTH for use case behaviour.
# This file is:
#   (1) referenced by specs/use_cases/UC001-retrieve-task-schema.md  (documentation)
#   (2) executed by the acceptance test suite                         (living documentation)
#
# Rules:
#   - Scenario IDs (e.g. UC001-S01) must match the use case document.
#   - Step definitions live in: src/test/java/io/github/hzahnlei/steps/
#   - Keep scenarios focused: one behaviour per scenario.

Feature: Retrieve Task Schema
  As an AI Agent
  I want to retrieve the JSON Schema for the Task resource
  So that I can construct valid Task payloads before submitting bulk insertions

  Background:
    Given the MCP server is running

  # ---------------------------------------------------------------------------
  # Happy path
  # ---------------------------------------------------------------------------

  Scenario: UC001-S01 Schema is retrieved successfully
    When  the AI Agent requests the Task schema at "GET /v1/mcp/schema/tasks"
    Then  the response status is 200
    And   the response body is a valid JSON Schema object

  # ---------------------------------------------------------------------------
  # Schema content — required and optional fields
  # ---------------------------------------------------------------------------

  Scenario: UC001-S02 Schema identifies title and status as required, description as optional
    When  the AI Agent requests the Task schema at "GET /v1/mcp/schema/tasks"
    Then  the response status is 200
    And   the schema "required" array contains "title"
    And   the schema "required" array contains "status"
    And   the schema "required" array does not contain "description"

  # ---------------------------------------------------------------------------
  # Schema content — status enum constraint
  # ---------------------------------------------------------------------------

  Scenario: UC001-S03 Schema restricts status to the declared enum values
    When  the AI Agent requests the Task schema at "GET /v1/mcp/schema/tasks"
    Then  the response status is 200
    And   the schema property "status" allows exactly the values "TODO", "IN_PROGRESS", "DONE"
