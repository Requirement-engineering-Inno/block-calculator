@US-15
Feature: Switch the interface language
  As a user I want to switch the language of the interface,
  so that I work in the language of my region.

  Background:
    Given the following users exist:
      | username | role   |
      | ann      | WORKER |

  Scenario: The interface is in English by default
    Given I am logged in as "ann"
    When I open the calculation list
    Then the page contains "New project"

  Scenario: A user switches to Russian
    Given I am logged in as "ann"
    When I switch the language to "ru"
    Then the page contains "Новый проект"

  Scenario: The chosen language is remembered
    Given I am logged in as "ann"
    When I switch the language to "ru"
    And I open the calculation list
    Then the page contains "Новый проект"

  Scenario: A user switches back to English
    Given I am logged in as "ann"
    When I switch the language to "ru"
    And I switch the language to "en"
    Then the page contains "New project"

  Scenario: The login page can be switched before signing in
    Given I am not logged in
    When I open "/login?lang=ru"
    Then the page contains "Вход"
