@US-21
Feature: Projects live inside production areas
  As a user I want all projects to be inside their production area,
  so that it is clear where a project belongs and which prices it uses.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
    And project "Alpha" exists
    And project "Beta" exists
    And worker "ann" is assigned to project "Alpha"
    And calculation "Walls" exists in project "Alpha" created by "ann"

  Scenario: A worker sees the areas he can use
    Given I am logged in as "ann"
    When I open "/projects"
    Then the project list contains "Alpha"
    And the project list contains "Other calculations"
    And the project list does not contain "Beta"

  Scenario: Every row of the area list opens the area
    Given I am logged in as "boss"
    When I open "/projects"
    Then every row of the list opens its page

  Scenario: The page of an area lists its projects
    Given I am logged in as "ann"
    When I open the production area "Alpha"
    Then the calculation list contains "Walls"
    And I see the "new project" button
    And I do not see the "area settings" button
    And every row of the list opens its page

  Scenario: A manager sees the settings button on the area page
    Given I am logged in as "boss"
    When I open the production area "Alpha"
    Then I see the "area settings" button

  Scenario: A worker cannot open an area he is not assigned to
    Given I am logged in as "ann"
    When I open the production area "Beta"
    Then the response status is 403

  Scenario: Settings gather general values, users and the account for managers
    Given I am logged in as "boss"
    When I open "/settings"
    Then the settings tabs are shown
    When I open "/users"
    Then the settings tabs are shown
    When I open my account
    Then the settings tabs are shown

  Scenario: A worker has only his account in the settings
    Given I am logged in as "ann"
    When I open my account
    Then the response status is 200
    And the settings tabs are not shown

  Scenario: The access level of a colleague is changed from the list
    Given calculation "Walls" is shared with "ann" as "read-only"
    And I am logged in as "boss"
    When I open calculation "Walls"
    Then the access level of "ann" can be changed in the list
