@US-11
Feature: Archive calculations
  As a worker with write access I want to archive a calculation I no longer need,
  so that it is shown in grey but is not lost.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
      | bob      | WORKER  |
    And project "Alpha" exists
    And worker "ann" is assigned to project "Alpha"
    And calculation "Walls" exists in project "Alpha" created by "ann"

  Scenario: The author archives his calculation
    Given I am logged in as "ann"
    When I archive calculation "Walls"
    Then calculation "Walls" is stored with:
      | archived | true |
    When I open the calculation list
    Then the calculation list shows "Walls" as archived

  Scenario: An archived calculation can be restored
    Given calculation "Walls" is archived
    And I am logged in as "ann"
    When I restore calculation "Walls"
    Then calculation "Walls" is stored with:
      | archived | false |

  Scenario: A colleague with read-write access archives
    Given calculation "Walls" is shared with "bob" as "read-write"
    And I am logged in as "bob"
    When I archive calculation "Walls"
    Then calculation "Walls" is stored with:
      | archived | true |

  Scenario: A colleague with read-only access cannot archive
    Given calculation "Walls" is shared with "bob" as "read-only"
    And I am logged in as "bob"
    When I archive calculation "Walls"
    Then the response status is 403
    And calculation "Walls" is stored with:
      | archived | false |

  Scenario: An archived calculation is still readable and marked
    Given calculation "Walls" is archived
    And I am logged in as "ann"
    When I open calculation "Walls"
    Then I see the "archived" badge
    And I see the "restore" button
    And I do not see the "archive" button

  Scenario: A manager archives any calculation
    Given I am logged in as "boss"
    When I archive calculation "Walls"
    Then calculation "Walls" is stored with:
      | archived | true |
