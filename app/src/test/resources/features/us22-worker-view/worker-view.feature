@US-22
Feature: Workers see quantities and proportions, never money
  As a manager I want workers to see only the amounts of materials and the mix proportions,
  so that prices and costs stay inside the management.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
    And project "Alpha" exists
    And worker "ann" is assigned to project "Alpha"
    And calculation "Walls" exists in project "Alpha" created by "ann" with 100 full and 0 half blocks using batch 4

  Scenario: A worker sees quantities and proportions on the project page
    Given I am logged in as "ann"
    When I open calculation "Walls"
    Then the result shows:
      | blocks | 100 |
    And the proportions of the mix are shown
    And the page shows no prices

  Scenario: A manager sees the costs
    Given I am logged in as "boss"
    When I open calculation "Walls"
    Then the page shows prices

  Scenario: The live preview of a worker has no money
    Given I am logged in as "ann"
    When I preview a calculation:
      | project | full | half | batch |
      | Alpha   | 100  | 0    | 4     |
    Then the result shows:
      | blocks | 100 |
    And the page shows no prices

  Scenario: The project list of a worker has no money
    Given I am logged in as "ann"
    When I open the production area "Alpha"
    Then the calculation list contains "Walls"
    And the page shows no prices

  Scenario: The area list of a worker has no prices
    Given I am logged in as "ann"
    When I open "/projects"
    Then the page shows no prices

  Scenario: A worker still exports a PDF
    Given I am logged in as "ann"
    When I export calculation "Walls" to PDF
    Then the PDF is returned
