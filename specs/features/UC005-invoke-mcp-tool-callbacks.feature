Feature: UC005 Invoke MCP tool callbacks

  Background:
    Given the MCP server is running

  Scenario: UC005-S01 Schema tool returns a JSON Schema object
    When the MCP tool "mcp-schema-tasks" is invoked with "{}"
    Then the tool response is valid JSON
    And the tool response contains field "type" with value "object"

  Scenario: UC005-S02 Tasks tool persists a batch and returns persisted records
    Given the task database is empty
    When the MCP tool "mcp-tasks" is invoked with:
      """
      {"tasks":[{"title":"Plan sprint","status":"TODO"}]}
      """
    Then the tool response is a JSON array of 1 element
    And each element in the tool response has a non-null "id"

  Scenario: UC005-S03 Summary tool reflects task counts
    Given the following tasks exist in the database:
      | title       | status |
      | Write tests | DONE   |
    When the MCP tool "mcp-tasks-summary" is invoked with "{}"
    Then the tool response summary count for "DONE" is 1

  Scenario: UC005-S04 Help tool lists all four tools
    When the MCP tool "mcp-help" is invoked with "{}"
    Then the tool response lists tool names: "mcp-schema-tasks", "mcp-tasks", "mcp-tasks-summary", "mcp-help"
