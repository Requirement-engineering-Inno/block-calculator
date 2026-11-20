@US-07
Feature: Currency of a project and its conversion
  As a manager I want to change the currency of a project with a rate I enter,
  so that prices follow the region where the tool is used.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
    And project "Alpha" exists
    And calculation "Walls" exists in project "Alpha" created by "boss"

  Scenario: Changing the currency converts all prices with the rate
    Given I am logged in as "boss"
    When I change the currency of project "Alpha" to "USD" with rate "0.5"
    Then project "Alpha" is stored with:
      | currency     | USD |
      | sandAmount   | 0.5 |
      | stoneAmount  | 1   |
      | cementAmount | 5   |

  Scenario: Results are shown in the new currency
    Given I am logged in as "boss"
    When I change the currency of project "Alpha" to "USD" with rate "0.5"
    And I open calculation "Walls"
    Then the result shows:
      | total    | 47600.00 |
      | currency | USD      |

  Scenario: The currency can also be set directly in the settings
    Given I am logged in as "boss"
    When I update project "Alpha" with:
      | currency |
      | eur      |
    Then project "Alpha" is stored with:
      | currency | EUR |

  Scenario: A rate of zero is rejected
    Given I am logged in as "boss"
    When I change the currency of project "Alpha" to "USD" with rate "0"
    Then I see an error message
    And project "Alpha" is stored with:
      | currency | RUB |

  Scenario: A worker cannot change the currency
    Given worker "ann" is assigned to project "Alpha"
    And I am logged in as "ann"
    When I change the currency of project "Alpha" to "USD" with rate "0.5"
    Then the response status is 403

  Scenario: A conversion that pushes a price above one million is rejected
    Given currency "KRW" is available
    And I am logged in as "boss"
    When I update project "Alpha" with:
      | sandAmount |
      | 900000     |
    And I change the currency of project "Alpha" to "KRW" with rate "1000"
    Then I see an error message
    And project "Alpha" is stored with:
      | currency   | RUB    |
      | sandAmount | 900000 |

  Scenario: A currency outside the application list is rejected
    Given I am logged in as "boss"
    When I change the currency of project "Alpha" to "KRW" with rate "1000"
    Then I see an error message
    And project "Alpha" is stored with:
      | currency | RUB |
