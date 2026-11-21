@US-05
Feature: Assign workers to projects
  As a manager I want to assign workers to my projects, and a worker can be in several projects,
  so that workers only work on the projects meant for them.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
      | bob      | WORKER  |
    And project "Alpha" exists
    And project "Beta" exists

  Scenario: A manager assigns workers to a project
    Given I am logged in as "boss"
    When I update project "Alpha" with:
      | workers |
      | ann,bob |
    Then project "Alpha" is stored with:
      | workers | ann,bob |

  Scenario: A manager removes a worker from a project
    Given worker "ann" is assigned to project "Alpha"
    And worker "bob" is assigned to project "Alpha"
    And I am logged in as "boss"
    When I update project "Alpha" with:
      | workers |
      | bob     |
    Then project "Alpha" is stored with:
      | workers | bob |

  Scenario: One worker can belong to several projects
    Given worker "ann" is assigned to project "Alpha"
    And worker "ann" is assigned to project "Beta"
    And I am logged in as "ann"
    When I open the new calculation form
    Then the new calculation form offers project "Alpha"
    And the new calculation form offers project "Beta"

  Scenario: A worker does not see projects he is not assigned to
    Given worker "ann" is assigned to project "Alpha"
    And I am logged in as "ann"
    When I open the new calculation form
    Then the new calculation form offers project "Alpha"
    And the new calculation form offers project "Other projects"
    And the new calculation form does not offer project "Beta"

  Scenario: A worker cannot create a calculation in a project he is not assigned to
    Given worker "ann" is assigned to project "Alpha"
    And I am logged in as "ann"
    When I create a calculation:
      | title | project | full | half | batch |
      | Roof  | Beta    | 100  | 0    | 4     |
    Then the response status is 403
    And calculation "Roof" no longer exists

  Scenario: A manager searches workers to assign
    Given the following users exist:
      | username | role   |
      | anna     | WORKER |
      | carl     | WORKER |
    And user "carl" is blocked
    And I am logged in as "boss"
    When I search workers for "an"
    Then the response status is 200
    And the worker search results contain "ann"
    And the worker search results contain "anna"
    And the worker search results do not contain "bob"
    And the worker search results do not contain "carl"

  Scenario: Search matches the login case-insensitively
    Given I am logged in as "boss"
    When I search workers for "BOB"
    Then the worker search results contain "bob"

  Scenario: Workers can search colleagues to share a calculation
    Given I am logged in as "ann"
    When I search workers for "bob"
    Then the worker search results contain "bob"

  Scenario: Search is closed for anonymous users
    Given I am not logged in
    When I search workers for "bob"
    Then I am redirected to the login page
