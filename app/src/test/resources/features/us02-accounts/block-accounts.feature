@US-02
Feature: Block and unblock accounts
  As a manager I want to block the account of a worker who left,
  so that he can no longer sign in.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | boss2    | MANAGER |
      | ann      | WORKER  |

  Scenario: A manager blocks a worker
    Given I am logged in as "boss"
    When I block user "ann"
    Then I am redirected to "/users"
    And the account "ann" is blocked

  Scenario: A manager unblocks a worker
    Given user "ann" is blocked
    And I am logged in as "boss"
    When I unblock user "ann"
    Then the account "ann" is active

  Scenario: A manager cannot block another manager
    Given I am logged in as "boss"
    When I block user "boss2"
    Then the response status is 403
    And the account "boss2" is active

  Scenario: The admin can block a manager
    Given I am logged in as "admin"
    When I block user "boss2"
    Then the account "boss2" is blocked
