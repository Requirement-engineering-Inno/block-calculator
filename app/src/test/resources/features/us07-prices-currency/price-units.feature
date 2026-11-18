@US-07
Feature: Prices in kg, m3 or per bag
  As a manager I want to enter material prices in the unit the supplier uses,
  so that I do not convert them by hand.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
    And project "Alpha" exists
    And I am logged in as "boss"

  Scenario: Prices per m3 and per bag are converted to kg
    Given project "Alpha" has these prices:
      | material | amount | unit    |
      | sand     | 1600   | PER_M3  |
      | stone    | 2900   | PER_M3  |
      | cement   | 500    | PER_BAG |
    And calculation "Walls" exists in project "Alpha" created by "boss"
    When I open calculation "Walls"
    Then the result shows:
      | sand cost   | 20400.00 |
      | stone cost  | 40800.00 |
      | cement cost | 34000.00 |
      | total       | 95200.00 |

  Scenario: The density of the project is used for the conversion
    Given project "Alpha" has these prices:
      | material | amount | unit   |
      | sand     | 1600   | PER_M3 |
      | stone    | 0      | PER_KG |
      | cement   | 0      | PER_KG |
    When I update project "Alpha" with:
      | sandDensity |
      | 800         |
    And I create a calculation:
      | title | project | full | half | batch | ws  | roundUp |
      | Walls | Alpha   | 1800 | 200  | 4     | 150 | true    |
    Then the result shows:
      | sand cost | 40800.00 |

  Scenario: Every project has its own prices
    Given project "Beta" exists
    And project "Beta" has these prices:
      | material | amount | unit   |
      | sand     | 2      | PER_KG |
      | stone    | 2      | PER_KG |
      | cement   | 10     | PER_KG |
    And calculation "Walls" exists in project "Alpha" created by "boss"
    And calculation "Roof" exists in project "Beta" created by "boss"
    When I open calculation "Walls"
    Then the result shows:
      | total | 95200.00 |
    When I open calculation "Roof"
    Then the result shows:
      | total | 115600.00 |

  Scenario: A cement price per m3 is not allowed
    When I update project "Alpha" with:
      | cementUnit |
      | PER_M3     |
    Then the response status is 400
