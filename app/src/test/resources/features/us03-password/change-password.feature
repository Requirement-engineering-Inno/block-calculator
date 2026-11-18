@US-03
Feature: Change my own password
  As a user I want to change my password,
  so that nobody else knows it.

  Background:
    Given the following users exist:
      | username | role   |
      | ann      | WORKER |
    And I am logged in as "ann"

  Scenario: A user changes his password
    When I change my password from "secret1" to "newpass1"
    Then I am redirected to "/account/password?changed"
    When I log in with username "ann" and password "newpass1"
    Then I am redirected to "/"

  Scenario: The old password stops working
    When I change my password from "secret1" to "newpass1"
    And I log in with username "ann" and password "secret1"
    Then I am redirected to "/login?error"

  Scenario: The current password must be correct
    When I change my password from "wrong" to "newpass1"
    Then I see an error message

  Scenario: A short new password is rejected
    When I change my password from "secret1" to "123"
    Then I see an error message
