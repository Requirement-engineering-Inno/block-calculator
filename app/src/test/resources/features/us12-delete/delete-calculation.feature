@US-12
Feature: Delete calculations
  As a manager I want to delete calculations,
  and workers must not be able to delete anything.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
    And project "Alpha" exists
    And worker "ann" is assigned to project "Alpha"
    And calculation "Walls" exists in project "Alpha" created by "ann"

  Scenario: A manager deletes a calculation
    Given I am logged in as "boss"
    When I delete calculation "Walls"
    Then I am redirected to "/calculations"
    And calculation "Walls" no longer exists

  Scenario: The admin deletes a calculation
    Given I am logged in as "admin"
    When I delete calculation "Walls"
    Then calculation "Walls" no longer exists

  Scenario: A worker cannot delete even his own calculation
    Given I am logged in as "ann"
    When I delete calculation "Walls"
    Then the response status is 403
    And calculation "Walls" still exists

  Scenario: The delete button is shown to managers only
    Given I am logged in as "ann"
    When I open calculation "Walls"
    Then I do not see the "delete" button
    When I am logged in as "boss"
    And I open calculation "Walls"
    Then I see the "delete" button
