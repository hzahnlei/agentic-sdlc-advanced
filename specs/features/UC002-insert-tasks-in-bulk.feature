# specs/features/UC002-insert-tasks-in-bulk.feature
#
# SINGLE SOURCE OF TRUTH for use case behaviour.
# This file is:
#   (1) referenced by specs/use_cases/UC002-insert-tasks-in-bulk.md  (documentation)
#   (2) executed by the acceptance test suite                         (living documentation)
#
# Rules:
#   - Scenario IDs (e.g. UC002-S01) must match the use case document.
#   - Step definitions live in: src/test/java/io/github/hzahnlei/steps/
#   - Keep scenarios focused: one behaviour per scenario.

Feature: Insert Tasks in Bulk
  As an AI Agent
  I want to submit a batch of Task objects for persistence
  So that the database is populated with realistic test data for further analysis

  Background:
    Given the MCP server is running
    And   the task database is empty

  # ---------------------------------------------------------------------------
  # Happy path
  # ---------------------------------------------------------------------------

  Scenario: UC002-S01 Valid batch is inserted and all created tasks are returned
    Given a batch of 3 valid tasks:
      | title                     | description                          | status      |
      | Design database schema    | Define tables, columns, and indexes  | DONE        |
      | Implement MCP server      | Expose four MCP tools via Spring AI  | IN_PROGRESS |
      | Write acceptance tests    |                                      | TODO        |
    When  the AI Agent submits the batch to "POST /v1/mcp/tasks"
    Then  the response status is 201
    And   the response contains 3 task objects
    And   each returned task has a non-null "id"
    And   each returned task has a non-null "createdAt"
    And   each returned task has a non-null "updatedAt"
    And   the task titles match the submitted titles

  # ---------------------------------------------------------------------------
  # Validation — required field missing
  # ---------------------------------------------------------------------------

  Scenario: UC002-S02 Task with blank title is rejected with INVALID_INPUT
    Given a batch containing one task with a blank title:
      | title | status |
      |       | TODO   |
    When  the AI Agent submits the batch to "POST /v1/mcp/tasks"
    Then  the response status is 400
    And   the error code is "INVALID_INPUT"
    And   the error details reference field "[0].title"
    And   no tasks are persisted in the database

  # ---------------------------------------------------------------------------
  # Validation — invalid enum value
  # ---------------------------------------------------------------------------

  Scenario: UC002-S03 Task with unknown status value is rejected with VALIDATION_FAILED
    Given a batch containing one task with an invalid status:
      | title          | status   |
      | Invalid status | ARCHIVED |
    When  the AI Agent submits the batch to "POST /v1/mcp/tasks"
    Then  the response status is 422
    And   the error code is "VALIDATION_FAILED"
    And   the error details reference field "[0].status"
    And   no tasks are persisted in the database

  # ---------------------------------------------------------------------------
  # Validation — empty array
  # ---------------------------------------------------------------------------

  Scenario: UC002-S04 Empty array is rejected with INVALID_INPUT
    When  the AI Agent submits an empty array to "POST /v1/mcp/tasks"
    Then  the response status is 400
    And   the error code is "INVALID_INPUT"
    And   no tasks are persisted in the database

  # ---------------------------------------------------------------------------
  # Idempotency
  # ---------------------------------------------------------------------------

  # ---------------------------------------------------------------------------
  # dueDate field
  # ---------------------------------------------------------------------------

  Scenario: UC002-S06 Tasks with due dates are persisted and returned
    Given a batch of 1 valid tasks:
      | title        | status | dueDate    |
      | Plan release | TODO   | 2026-12-31 |
    When  the AI Agent submits the batch to "POST /v1/mcp/tasks"
    Then  the response status is 201
    And   each returned task has a non-null "dueDate"

  # ---------------------------------------------------------------------------
  # Idempotency
  # ---------------------------------------------------------------------------

  Scenario: UC002-S05 Repeated request with the same Idempotency-Key does not create duplicate records
    Given a batch of 2 valid tasks:
      | title        | status |
      | First task   | TODO   |
      | Second task  | DONE   |
    And   the request carries Idempotency-Key "550e8400-e29b-41d4-a716-446655440000"
    When  the AI Agent submits the batch to "POST /v1/mcp/tasks"
    Then  the response status is 201
    And   the response contains 2 task objects
    When  the AI Agent submits the identical batch again with the same Idempotency-Key
    Then  the response status is 201
    And   the database contains exactly 2 task records
