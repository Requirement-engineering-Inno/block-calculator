@US-19
Feature: Application level settings
  As a manager I want to keep the wheelbarrow weight and the list of currencies in one place,
  so that every project uses the same values.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |

  Scenario: The settings page is available to managers
    Given I am logged in as "boss"
    When I open "/settings"
    Then the response status is 200
    And the settings page lists the currency "RUB"
    And the settings page lists the currency "USD"
    And the settings page lists the currency "EUR"

  Scenario: A worker cannot open the settings
    Given I am logged in as "ann"
    When I open "/settings"
    Then the response status is 403

  Scenario: A manager changes the wheelbarrow weight
    Given I am logged in as "boss"
    When I set the wheelbarrow weight to "120"
    Then I am redirected to "/settings"
    And the wheelbarrow weight is "120"

  Scenario: The new weight is used by calculations without their own value
    Given project "Alpha" exists
    And I am logged in as "boss"
    When I set the wheelbarrow weight to "100"
    And I create a calculation:
      | title | project | full | half | batch | roundUp |
      | Walls | Alpha   | 28   | 0    | 4     | true    |
    Then the result shows:
      | ws      | 100    |
      | sand kg | 200.00 |

  Scenario Outline: A wrong wheelbarrow weight is rejected
    Given I am logged in as "boss"
    When I set the wheelbarrow weight to "<kg>"
    Then I see an error message
    And the wheelbarrow weight is "150"

    Examples:
      | kg    |
      | 0     |
      | -5    |
      | 20000 |

  Scenario: A manager adds a currency
    Given I am logged in as "boss"
    When I add the currency "zar"
    Then the currency list contains "ZAR"

  Scenario: A new currency can be chosen in a project
    Given I am logged in as "boss"
    And project "Alpha" exists
    When I add the currency "ZAR"
    And I update project "Alpha" with:
      | currency |
      | ZAR      |
    Then project "Alpha" is stored with:
      | currency | ZAR |

  Scenario Outline: A wrong currency code is rejected
    Given I am logged in as "boss"
    When I add the currency "<code>"
    Then I see an error message

    Examples:
      | code  |
      | US    |
      | USDX  |
      | U5D   |

  Scenario: The same currency cannot be added twice
    Given I am logged in as "boss"
    When I add the currency "USD"
    Then I see an error message

  Scenario: A currency that nobody uses can be removed
    Given I am logged in as "boss"
    When I remove the currency "EUR"
    Then the currency list does not contain "EUR"

  Scenario: A currency used by a project cannot be removed
    Given project "Alpha" exists
    And I am logged in as "boss"
    When I remove the currency "RUB"
    Then I see an error message
    And the currency list contains "RUB"

  Scenario: A worker cannot change the settings
    Given I am logged in as "ann"
    When I set the wheelbarrow weight to "100"
    Then the response status is 403
    And the wheelbarrow weight is "150"
