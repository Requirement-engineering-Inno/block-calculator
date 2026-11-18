@US-02
Feature: Find accounts in a long list
  As a manager I want to search and filter employees and page through them,
  because there can be very many workers.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | anna     | WORKER  |
      | annette  | WORKER  |
      | bob      | WORKER  |
      | carl     | WORKER  |
    And user "carl" is blocked

  Scenario: Search by a part of the login
    Given I am logged in as "boss"
    When I search users for "ann"
    Then the user list contains "anna"
    And the user list contains "annette"
    And the user list does not contain "bob"

  Scenario: Filter blocked accounts
    Given I am logged in as "boss"
    When I filter users by role "WORKER" and status "false"
    Then the user list contains "carl"
    And the user list does not contain "anna"

  Scenario: The admin filters managers
    Given I am logged in as "admin"
    When I filter users by role "MANAGER" and status ""
    Then the user list contains "boss"
    And the user list does not contain "anna"

  Scenario: A search without results shows nothing
    Given I am logged in as "boss"
    When I search users for "zzz"
    Then the user list shows 0 users

  Scenario: A manager does not find managers or the admin
    Given I am logged in as "boss"
    When I search users for "admin"
    Then the user list shows 0 users
