@US-04
Feature: Manage projects and their settings
  As a manager I want to create projects with their own prices and a currency from the application list,
  so that every project is calculated with its own conditions.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | boss2    | MANAGER |
      | ann      | WORKER  |

  Scenario: The built-in production area "Other projects" exists
    Given I am logged in as "boss"
    When I open "/projects"
    Then the project list contains "Other calculations"

  Scenario: A manager creates a project with prices
    Given I am logged in as "boss"
    When I create a project:
      | name  | currency | sandAmount | sandUnit | stoneAmount | stoneUnit | cementAmount | cementUnit |
      | Alpha | RUB      | 1600       | PER_M3   | 2900        | PER_M3    | 500          | PER_BAG    |
    Then I am redirected to "/projects"
    And project "Alpha" is stored with:
      | currency     | RUB     |
      | sandAmount   | 1600    |
      | sandUnit     | PER_M3  |
      | stoneAmount  | 2900    |
      | cementAmount | 500     |
      | cementUnit   | PER_BAG |

  Scenario: A project can use any currency from the list
    Given I am logged in as "boss"
    When I create a project:
      | name  | currency |
      | Alpha | EUR      |
    Then project "Alpha" is stored with:
      | currency | EUR |

  Scenario: A currency that is not in the list is rejected
    Given I am logged in as "boss"
    When I create a project:
      | name  | currency |
      | Alpha | XXX      |
    Then I see an error message
    And project "Alpha" has not been created

  Scenario: Every manager sees all projects
    Given project "Alpha" exists
    And I am logged in as "boss2"
    When I open "/projects"
    Then the project list contains "Alpha"
    And the project list contains "Other calculations"

  Scenario: A negative price is rejected
    Given I am logged in as "boss"
    When I create a project:
      | name  | sandAmount |
      | Alpha | -5         |
    Then I see an error message

  Scenario: A worker cannot open the production area settings
    Given project "Alpha" exists
    And I am logged in as "ann"
    When I open the settings of production area "Alpha"
    Then the response status is 403

  Scenario: A price of exactly one million is accepted
    Given I am logged in as "boss"
    When I create a project:
      | name  | sandAmount | stoneAmount | cementAmount |
      | Alpha | 1000000    | 1000000     | 1000000      |
    Then I am redirected to "/projects"
    And project "Alpha" is stored with:
      | sandAmount | 1000000 |

  Scenario Outline: A price above one million is rejected
    Given I am logged in as "boss"
    When I create a project:
      | name  | <field>  |
      | Alpha | <amount> |
    Then I see an error message
    And project "Alpha" has not been created

    Examples:
      | field        | amount       |
      | sandAmount   | 1000001      |
      | stoneAmount  | 100000000000 |
      | cementAmount | 5000000      |
