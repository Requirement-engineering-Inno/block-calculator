@US-23
Feature: Welcome tour
  As a new user I want a short tour on my first visit,
  and I want to be able to run it again later.

  Background:
    Given the following users exist:
      | username | role   |
      | ann      | WORKER |

  Scenario: The tour starts by itself on the first visit
    Given I am logged in as "ann"
    When I open "/projects"
    Then the welcome tour starts by itself

  Scenario: The tour does not start again once it was finished
    Given I am logged in as "ann"
    When I finish the welcome tour
    Then the response status is 204
    When I open "/projects"
    Then the welcome tour does not start by itself

  Scenario: The tour can be started again from the home page
    Given I am logged in as "ann"
    And I finish the welcome tour
    When I open "/projects"
    Then I see the "tour" button
