@US-01
Feature: Log in and log out
  As a user I want to sign in with a login and a password
  so that only people of the company use the tool.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
      | bob      | WORKER  |

  Scenario: A user signs in with correct credentials
    When I log in with username "ann" and password "secret1"
    Then I am redirected to "/"

  Scenario: Wrong password is rejected
    When I log in with username "ann" and password "wrong"
    Then I am redirected to "/login?error"

  Scenario: A blocked user cannot sign in
    Given user "bob" is blocked
    When I log in with username "bob" and password "secret1"
    Then I am redirected to "/login?error"

  Scenario: The default administrator exists
    When I log in with username "admin" and password "admin"
    Then I am redirected to "/"

  Scenario: An anonymous user is sent to the login page
    Given I am not logged in
    When I open "/calculations"
    Then I am redirected to the login page

  Scenario: A user logs out
    Given I am logged in as "ann"
    When I log out
    Then I am redirected to "/login"

  Scenario Outline: Logo, styles and scripts load before signing in
    Given I am not logged in
    When I open "<file>"
    Then the response status is 200

    Examples:
      | file          |
      | /icon.svg     |
      | /css/app.css  |
      | /js/app.js    |
      | /js/htmx.min.js |
