@US-18
Feature: Employee profiles and my account
  As a manager I want to open the profile of an employee to see his projects and calculations,
  and as any user I want to see the information about myself next to the password form.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | boss2    | MANAGER |
      | ann      | WORKER  |
      | bob      | WORKER  |
    And project "Alpha" exists
    And project "Beta" exists
    And worker "ann" is assigned to project "Alpha"
    And calculation "Walls" exists in project "Alpha" created by "ann"

  Scenario: A manager opens the profile of a worker
    Given I am logged in as "boss"
    When I open the profile of "ann"
    Then the response status is 200
    And the profile shows:
      | name         | ann    |
      | username     | ann    |
      | role         | Worker |
      | status       | Active |
      | calculations | 1      |

  Scenario: The profile lists the projects of the worker
    Given I am logged in as "boss"
    When I open the profile of "ann"
    Then the profile lists project "Alpha"
    And the profile lists project "Other calculations"
    And the profile does not list project "Beta"

  Scenario: The profile lists the calculations of the worker
    Given I am logged in as "boss"
    When I open the profile of "ann"
    Then the profile lists calculation "Walls"

  Scenario: A blocked worker is shown as blocked
    Given user "bob" is blocked
    And I am logged in as "boss"
    When I open the profile of "bob"
    Then the profile shows:
      | status | Blocked |

  Scenario: The profile is reachable from the user list
    Given I am logged in as "boss"
    When I open "/users"
    Then the page contains "ann"

  Scenario: A manager cannot open the profile of another manager
    Given I am logged in as "boss"
    When I open the profile of "boss2"
    Then the response status is 403

  Scenario: The admin opens the profile of a manager
    Given I am logged in as "admin"
    When I open the profile of "boss"
    Then the response status is 200
    And the profile shows:
      | role | Manager |

  Scenario: A worker cannot open the profile of a colleague
    Given I am logged in as "bob"
    When I open the profile of "ann"
    Then the response status is 403

  Scenario: An unknown profile gives 404
    Given I am logged in as "boss"
    When I open "/users/000000000000000000000000"
    Then the response status is 404

  Scenario: My account shows the information about me
    Given I am logged in as "ann"
    When I open my account
    Then the response status is 200
    And the profile shows:
      | name         | ann    |
      | role         | Worker |
      | calculations | 1      |
    And the profile lists project "Alpha"
    And the page contains "Change password"

  Scenario: A manager has an account page too
    Given I am logged in as "boss"
    When I open my account
    Then the profile shows:
      | role | Manager |
