@US-10
Feature: Edit a calculation
  As a worker with write access I want to correct a calculation,
  so that the numbers are right, and the system should remember who changed it last and when.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
      | bob      | WORKER  |
    And project "Alpha" exists
    And worker "ann" is assigned to project "Alpha"
    And worker "bob" is assigned to project "Alpha"
    And calculation "Walls" exists in project "Alpha" created by "ann"

  Scenario: The author edits his calculation
    Given I am logged in as "ann"
    When I edit calculation "Walls" changing:
      | full |
      | 500  |
    Then I am redirected to the calculation page
    And calculation "Walls" is stored with:
      | full       | 500 |
      | updated by | ann |

  Scenario: The result is recalculated after the edit
    Given I am logged in as "ann"
    When I edit calculation "Walls" changing:
      | ws  |
      | 100 |
    Then the result shows:
      | sand kg | 13600.00 |

  Scenario: A colleague with read-write access edits, the author stays the same
    Given calculation "Walls" is shared with "bob" as "read-write"
    And I am logged in as "bob"
    When I edit calculation "Walls" changing:
      | full |
      | 600  |
    Then the calculation page shows:
      | author     | ann |
      | updated by | bob |

  Scenario: A colleague with read-only access cannot edit
    Given calculation "Walls" is shared with "bob" as "read-only"
    And I am logged in as "bob"
    When I edit calculation "Walls" changing:
      | full |
      | 600  |
    Then the response status is 403
    And calculation "Walls" is stored with:
      | full | 1800 |

  Scenario: A read-only colleague does not get the edit form
    Given calculation "Walls" is shared with "bob" as "read-only"
    And I am logged in as "bob"
    When I open the edit form of calculation "Walls"
    Then the response status is 403

  Scenario: A worker without access cannot edit
    Given I am logged in as "bob"
    When I edit calculation "Walls" changing:
      | full |
      | 600  |
    Then the response status is 403

  Scenario: A manager edits any calculation
    Given I am logged in as "boss"
    When I edit calculation "Walls" changing:
      | batch |
      | 2     |
    Then calculation "Walls" is stored with:
      | batch      | 2    |
      | author     | ann  |
      | updated by | boss |

  Scenario: An invalid edit is rejected and nothing changes
    Given I am logged in as "ann"
    When I edit calculation "Walls" changing:
      | full | half |
      | 0    | 0    |
    Then I see an error message
    And calculation "Walls" is stored with:
      | full | 1800 |
      | half | 200  |
