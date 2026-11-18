@US-13
Feature: Freeze the prices of a calculation
  As a manager I want to freeze a calculation,
  so that its prices do not change when the project prices change later.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
    And project "Alpha" exists
    And worker "ann" is assigned to project "Alpha"
    And calculation "Walls" exists in project "Alpha" created by "ann"

  Scenario: A manager freezes a calculation
    Given I am logged in as "boss"
    When I freeze calculation "Walls"
    Then calculation "Walls" is stored with:
      | frozen | true |
    And I see the "frozen" badge

  Scenario: A frozen calculation keeps its prices, a normal one follows the project
    Given calculation "Roof" exists in project "Alpha" created by "ann"
    And calculation "Walls" is frozen
    And I am logged in as "boss"
    When I update project "Alpha" with:
      | sandAmount |
      | 2          |
    And I open calculation "Walls"
    Then the result shows:
      | total | 95200.00 |
    When I open calculation "Roof"
    Then the result shows:
      | total | 115600.00 |

  Scenario: Unfreezing recalculates with the current prices
    Given calculation "Walls" is frozen
    And I am logged in as "boss"
    And I update project "Alpha" with:
      | sandAmount |
      | 2          |
    When I unfreeze calculation "Walls"
    Then calculation "Walls" is stored with:
      | frozen | false |
    And the result shows:
      | total | 115600.00 |

  Scenario: The frozen mark is shown in the list
    Given calculation "Walls" is frozen
    And I am logged in as "ann"
    When I open the calculation list
    Then the calculation list shows "Walls" as frozen

  Scenario: A frozen calculation cannot be edited
    Given calculation "Walls" is frozen
    And I am logged in as "boss"
    When I edit calculation "Walls" changing:
      | full |
      | 10   |
    Then I see an error message
    And calculation "Walls" is stored with:
      | full | 1800 |

  Scenario: The edit button is hidden on a frozen calculation
    Given calculation "Walls" is frozen
    And I am logged in as "ann"
    When I open calculation "Walls"
    Then I see the "frozen" badge
    And I do not see the "edit" button

  Scenario: A worker cannot freeze
    Given I am logged in as "ann"
    When I freeze calculation "Walls"
    Then the response status is 403
    And calculation "Walls" is stored with:
      | frozen | false |

  Scenario: Only managers see the freeze button
    Given I am logged in as "ann"
    When I open calculation "Walls"
    Then I do not see the "freeze" button
    When I am logged in as "boss"
    And I open calculation "Walls"
    Then I see the "freeze" button
