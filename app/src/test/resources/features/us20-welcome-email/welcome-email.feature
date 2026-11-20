@US-20
Feature: Welcome e-mail for a new employee
  As a manager I want the new employee to receive an e-mail with his login, password and role,
  so that I do not have to pass the credentials on myself.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
    And I am logged in as "boss"

  Scenario: The e-mail contains the login, the password and the role
    When I create user "carl" with role "WORKER" and email "carl@company.test"
    Then a welcome e-mail was sent to "carl@company.test"
    And the welcome e-mail to "carl@company.test" contains "carl"
    And the welcome e-mail to "carl@company.test" contains "secret1"
    And the welcome e-mail to "carl@company.test" contains "Worker"

  Scenario: The e-mail uses the language of the manager
    When I switch the language to "ru"
    And I create user "carl" with role "WORKER" and email "carl@company.test"
    Then the welcome e-mail to "carl@company.test" contains "Работник"
    And the welcome e-mail to "carl@company.test" contains "Логин: carl"

  Scenario: The admin creates a manager and the e-mail names the role
    Given I am logged in as "admin"
    When I create user "boss2" with role "MANAGER" and email "boss2@company.test"
    Then the welcome e-mail to "boss2@company.test" contains "Manager"

  Scenario: An invalid e-mail address creates nothing and sends nothing
    When I create user "carl" with role "WORKER" and email "carl-at-company"
    Then I see an error message
    And no e-mail was sent

  Scenario: A broken mail server does not stop the registration
    Given sending of e-mails fails
    When I create user "carl" with role "WORKER" and email "carl@company.test"
    Then I am redirected to "/users"
    And the account "carl" exists with role "WORKER"
    And no e-mail was sent

  Scenario: The e-mail address is shown in the profile of the employee
    When I create user "carl" with role "WORKER" and email "carl@company.test"
    And I open the profile of "carl"
    Then the page contains "carl@company.test"
