# specs/features/UC001-sample_use_case.feature
#
# SINGLE SOURCE OF TRUTH for use case behaviour.
# This file is:
#   (1) referenced by specs/use_cases/UC001-sample_use_case.md  (documentation)
#   (2) executed by the acceptance test suite                    (living documentation)
#
# Rules:
#   - Scenario IDs (e.g. UC001-S01) must match the use case document.
#   - Step definitions live in: <test-source-root>/steps/ (or equivalent per language)
#   - Keep scenarios focused: one behaviour per scenario.
#   - Use exact terms from specs/glossary.md.

Feature: TODO: Use Case Name
  As a TODO: Actor
  I want to TODO: Goal
  So that TODO: Business Value

  Background:
    Given TODO: a precondition that applies to all scenarios
    And   TODO: another common precondition

  # ---------------------------------------------------------------------------
  # Happy path
  # ---------------------------------------------------------------------------

  Scenario: UC001-S01 Successful TODO: action
    Given TODO: specific precondition
    When  TODO: actor performs action with TODO: input
    Then  TODO: expected outcome
    And   TODO: additional assertion (e.g. side effect, event emitted)

  # ---------------------------------------------------------------------------
  # Validation / error paths
  # ---------------------------------------------------------------------------

  Scenario: UC001-S02 TODO: invalid input is rejected
    Given TODO: precondition
    When  TODO: actor performs action with invalid TODO: input
    Then  the request is rejected with error code "TODO_ERROR_CODE"
    And   TODO: assert no side effects occurred

  # ---------------------------------------------------------------------------
  # Business rule enforcement (reference BR-NNN from domain_model/business_rules.md)
  # ---------------------------------------------------------------------------

  Scenario: UC001-S03 BR-001 TODO: business rule name is enforced
    Given TODO: state that would violate BR-001
    When  TODO: action is attempted
    Then  the request is rejected with error code "TODO_BR001_ERROR_CODE"
