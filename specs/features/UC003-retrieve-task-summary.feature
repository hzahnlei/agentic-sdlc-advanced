# specs/features/UC003-retrieve-task-summary.feature
#
# SINGLE SOURCE OF TRUTH for use case behaviour.
# This file is:
#   (1) referenced by specs/use_cases/UC003-retrieve-task-summary.md  (documentation)
#   (2) executed by the acceptance test suite                          (living documentation)
#
# Rules:
#   - Scenario IDs (e.g. UC003-S01) must match the use case document.
#   - Step definitions live in: src/test/java/io/github/hzahnlei/steps/
#   - Keep scenarios focused: one behaviour per scenario.

Feature: Retrieve Task Summary Statistics
  As an AI Agent
  I want to retrieve task counts grouped by status
  So that I can validate that a prior bulk insertion produced the expected data distribution

  Background:
    Given the MCP server is running

  # ---------------------------------------------------------------------------
  # Empty database
  # ---------------------------------------------------------------------------

  Scenario: UC003-S01 Summary with no tasks returns all-zero counts
    Given the task database is empty
    When  the AI Agent requests the task summary at "GET /v1/mcp/tasks/summary"
    Then  the response status is 200
    And   the summary count for "TODO" is 0
    And   the summary count for "IN_PROGRESS" is 0
    And   the summary count for "DONE" is 0
    And   the summary total is 0

  # ---------------------------------------------------------------------------
  # Summary reflects inserted tasks
  # ---------------------------------------------------------------------------

  Scenario: UC003-S02 Summary correctly reflects tasks inserted across all statuses
    Given the following tasks exist in the database:
      | title   | status      |
      | Task 1  | TODO        |
      | Task 2  | TODO        |
      | Task 3  | TODO        |
      | Task 4  | IN_PROGRESS |
      | Task 5  | IN_PROGRESS |
      | Task 6  | DONE        |
    When  the AI Agent requests the task summary at "GET /v1/mcp/tasks/summary"
    Then  the response status is 200
    And   the summary count for "TODO" is 3
    And   the summary count for "IN_PROGRESS" is 2
    And   the summary count for "DONE" is 1

  # ---------------------------------------------------------------------------
  # Total consistency invariant
  # ---------------------------------------------------------------------------

  Scenario: UC003-S03 Summary total equals the sum of all per-status counts
    Given the following tasks exist in the database:
      | title   | status      |
      | Task A  | TODO        |
      | Task B  | IN_PROGRESS |
      | Task C  | DONE        |
      | Task D  | DONE        |
    When  the AI Agent requests the task summary at "GET /v1/mcp/tasks/summary"
    Then  the response status is 200
    And   the summary total equals the sum of all per-status counts
