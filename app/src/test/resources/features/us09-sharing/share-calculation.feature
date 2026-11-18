@US-09
Feature: Share calculations with colleagues
  As the author of a calculation I want to open it to colleagues as read-only or read/write,
  and as a manager I want to share it with all workers of my project in one click.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
      | bob      | WORKER  |
      | carl     | WORKER  |
    And project "Alpha" exists
    And worker "ann" is assigned to project "Alpha"
    And worker "bob" is assigned to project "Alpha"
    And calculation "Walls" exists in project "Alpha" created by "ann"

  Scenario: A worker shares his calculation read-only
    Given I am logged in as "ann"
    When I share calculation "Walls" with "bob" as "read-only"
    Then calculation "Walls" grants "bob" "read-only" access
    When I am logged in as "bob"
    And I open calculation "Walls"
    Then the response status is 200
    And I do not see the "edit" button

  Scenario: A worker shares his calculation read-write
    Given I am logged in as "ann"
    When I share calculation "Walls" with "bob" as "read-write"
    Then calculation "Walls" grants "bob" "read-write" access
    When I am logged in as "bob"
    And I open calculation "Walls"
    Then I see the "edit" button

  Scenario: Without sharing a colleague cannot open the calculation
    Given I am logged in as "bob"
    When I open calculation "Walls"
    Then the response status is 403

  Scenario: A shared calculation appears in the list of the colleague
    Given calculation "Walls" is shared with "bob" as "read-only"
    And I am logged in as "bob"
    When I open the calculation list
    Then the calculation list contains "Walls"

  Scenario: Changing the access level of a colleague
    Given calculation "Walls" is shared with "bob" as "read-only"
    And I am logged in as "ann"
    When I share calculation "Walls" with "bob" as "read-write"
    Then calculation "Walls" grants "bob" "read-write" access

  Scenario: The author takes the access back
    Given calculation "Walls" is shared with "bob" as "read-only"
    And I am logged in as "ann"
    When I stop sharing calculation "Walls" with "bob"
    Then calculation "Walls" grants "bob" no access
    When I am logged in as "bob"
    And I open calculation "Walls"
    Then the response status is 403

  Scenario: The sharing is listed on the calculation page
    Given calculation "Walls" is shared with "bob" as "read-only"
    And I am logged in as "ann"
    When I open calculation "Walls"
    Then calculation "Walls" is shared with "bob" as "read-only" on the page

  Scenario: A manager shares a calculation with all workers of the project
    Given calculation "Floor" exists in project "Alpha" created by "boss"
    And I am logged in as "boss"
    When I share calculation "Floor" with all workers of its project as "read-only"
    Then calculation "Floor" grants "ann" "read-only" access
    And calculation "Floor" grants "bob" "read-only" access
    And calculation "Floor" grants "carl" no access

  Scenario: Only a manager can share with all workers of a project
    Given I am logged in as "ann"
    When I share calculation "Walls" with all workers of its project as "read-only"
    Then the response status is 403
    When I open calculation "Walls"
    Then I do not see the "share all" button

  Scenario: The "share with all" button is shown to managers
    Given I am logged in as "boss"
    When I open calculation "Walls"
    Then I see the "share all" button

  Scenario: A colleague with read-write access cannot share further
    Given calculation "Walls" is shared with "bob" as "read-write"
    And I am logged in as "bob"
    When I share calculation "Walls" with "carl" as "read-only"
    Then the response status is 403
    And calculation "Walls" grants "carl" no access
