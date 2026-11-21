@US-16
Feature: Role based access
  As the company I want every role to see only its own pages,
  so that workers cannot change projects, prices and accounts.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
    And project "Alpha" exists

  Scenario Outline: Management pages by role
    Given I am logged in as "<user>"
    When I open "<path>"
    Then the response status is <status>

    Examples:
      | user  | path          | status |
      | ann   | /projects/new | 403    |
      | ann   | /users        | 403    |
      | boss  | /projects     | 200    |
      | boss  | /projects/new | 200    |
      | boss  | /users        | 200    |
      | admin | /projects     | 200    |
      | admin | /users        | 200    |

  Scenario: Everybody who is signed in can open the production area list
    Given I am logged in as "ann"
    When I open "/projects"
    Then the response status is 200

  Scenario: Everybody who is signed in can open the calculation list
    Given I am logged in as "ann"
    When I open the calculation list
    Then the response status is 200

  Scenario: Management pages are closed for workers inside an area
    Given I am logged in as "ann"
    When I open the settings of production area "Alpha"
    Then the response status is 403

  Scenario: Management pages are closed for anonymous users
    Given I am not logged in
    When I open "/projects"
    Then I am redirected to the login page

  Scenario: The admin has the same rights on calculations as a manager
    Given calculation "Walls" exists in project "Alpha" created by "ann"
    And I am logged in as "admin"
    When I open calculation "Walls"
    Then I see the "edit" button
    And I see the "delete" button
    And I see the "freeze" button

  Scenario: Workers do not see the management links
    Given I am logged in as "ann"
    When I open the calculation list
    Then the page does not contain "Users"
    And the page does not contain "New production area"
    And the page does not contain "Currencies"
