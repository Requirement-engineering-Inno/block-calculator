@US-02
Feature: Create accounts
  As an admin I want to create manager accounts, and as a manager I want to create worker accounts,
  so that every employee has his own login.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |

  Scenario: The admin creates a manager
    Given I am logged in as "admin"
    When I create user "boss2" with role "MANAGER"
    Then I am redirected to "/users"
    And the account "boss2" exists with role "MANAGER"

  Scenario: The admin creates a worker
    Given I am logged in as "admin"
    When I create user "carl" with role "WORKER"
    Then the account "carl" exists with role "WORKER"

  Scenario: A manager creates a worker
    Given I am logged in as "boss"
    When I create user "carl" with role "WORKER"
    Then I am redirected to "/users"
    And the account "carl" exists with role "WORKER"

  Scenario: A manager cannot create another manager
    Given I am logged in as "boss"
    When I create user "boss2" with role "MANAGER"
    Then the response status is 403
    And the account "boss2" does not exist

  Scenario: A worker cannot manage accounts
    Given I am logged in as "ann"
    When I open "/users"
    Then the response status is 403

  Scenario: A duplicate login is rejected
    Given I am logged in as "boss"
    When I create user "ann" with role "WORKER"
    Then I see an error message

  Scenario: A short password is rejected
    Given I am logged in as "boss"
    When I create user "carl" with role "WORKER" and password "123"
    Then I see an error message
    And the account "carl" does not exist

  Scenario: The manager sees the workers in the user list
    Given I am logged in as "boss"
    When I open "/users"
    Then the user list contains "ann"

  Scenario: The e-mail is required
    Given I am logged in as "boss"
    When I create user "carl" with role "WORKER" and email "not-an-email"
    Then I see an error message
    And the account "carl" does not exist

  Scenario: Two employees cannot share an e-mail
    Given I am logged in as "boss"
    When I create user "carl" with role "WORKER" and email "ann@example.test"
    Then I see an error message
    And the account "carl" does not exist
