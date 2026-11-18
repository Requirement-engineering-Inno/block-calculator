@US-02
Feature: The user list is split into pages
  As a manager I want a long list of workers shown page by page.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
    And 20 workers named "worker" exist
    And I am logged in as "boss"

  Scenario: The first page has 15 users
    When I open "/users"
    Then the user list shows 15 users

  Scenario: The second page has the rest
    When I open "/users?page=2"
    Then the user list shows 5 users

  Scenario: A page number out of range shows the last page
    When I open "/users?page=99"
    Then the user list shows 5 users

  Scenario: The search is applied before paging
    When I search users for "worker1"
    Then the user list shows 10 users
