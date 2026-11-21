@US-08
Feature: Save calculations and find them again
  As a worker I want my calculations to be saved with their author and dates,
  so that nothing gets lost and the production can be tracked by project.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
      | bob      | WORKER  |
    And project "Alpha" exists
    And project "Beta" exists
    And worker "ann" is assigned to project "Alpha"
    And worker "bob" is assigned to project "Alpha"

  Scenario: A saved calculation keeps its author and the project
    Given I am logged in as "ann"
    When I create a calculation:
      | title | project | full | half | batch | ws  | roundUp |
      | Walls | Alpha   | 1800 | 200  | 4     | 150 | true    |
    Then calculation "Walls" is stored with:
      | author     | ann   |
      | updated by | ann   |
      | project    | Alpha |
      | full       | 1800  |
      | half       | 200   |
      | batch      | 4     |
      | ws         | 150   |
      | roundUp    | true  |
    And the calculation page shows:
      | title      | Walls |
      | project    | Alpha |
      | author     | ann   |
      | updated by | ann   |

  Scenario: A project without a production area goes to the built-in area
    Given I am logged in as "ann"
    When I create a calculation:
      | title | full | half | batch |
      | Walls | 100  | 0    | 4     |
    Then calculation "Walls" is stored with:
      | project | Other calculations |

  Scenario: A worker sees only his own calculations
    Given calculation "Walls" exists in project "Alpha" created by "ann"
    And calculation "Roof" exists in project "Alpha" created by "bob"
    And I am logged in as "ann"
    When I open the calculation list
    Then the calculation list contains "Walls"
    And the calculation list does not contain "Roof"

  Scenario: A manager sees all calculations of all projects
    Given calculation "Walls" exists in project "Alpha" created by "ann"
    And calculation "Roof" exists in project "Beta" created by "bob"
    And I am logged in as "boss"
    When I open the calculation list
    Then the calculation list contains "Walls"
    And the calculation list contains "Roof"

  Scenario: A manager sees who created a calculation
    Given calculation "Walls" exists in project "Alpha" created by "ann"
    And I am logged in as "boss"
    When I open calculation "Walls"
    Then the calculation page shows:
      | author     | ann |
      | updated by | ann |

  Scenario: The list can be filtered by project
    Given calculation "Walls" exists in project "Alpha" created by "ann"
    And calculation "Roof" exists in project "Beta" created by "bob"
    And I am logged in as "boss"
    When I open the calculation list filtered by project "Alpha"
    Then the calculation list contains "Walls"
    And the calculation list does not contain "Roof"

  Scenario: A worker cannot open a calculation of a colleague
    Given calculation "Roof" exists in project "Alpha" created by "bob"
    And I am logged in as "ann"
    When I open calculation "Roof"
    Then the response status is 403

  Scenario: Times are shown in GMT and can be converted by the browser
    Given I am logged in as "ann"
    When I create a calculation:
      | title | project | full | half | batch |
      | Walls | Alpha   | 100  | 0    | 4     |
    Then the creation time is shown in GMT with a machine readable instant
