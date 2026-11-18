@US-06
Feature: Calculate materials and cost
  As a worker I want to enter the number of full and half blocks and choose a batch,
  so that I get the quantity of cement, stone and sand and the cost without pen and paper.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
    And project "Alpha" exists
    And worker "ann" is assigned to project "Alpha"
    And I am logged in as "ann"
    # Alpha prices in the tests: sand 1, stone 2, cement 10 per kg, currency RUB

  Scenario: The customer's example, batches rounded up
    When I create a calculation:
      | title | project | full | half | batch | ws  | roundUp |
      | Walls | Alpha   | 1800 | 200  | 4     | 150 | true    |
    Then I am redirected to the calculation page
    And the result shows:
      | blocks      | 1900     |
      | batches     | 68       |
      | sand kg     | 20400.00 |
      | stone kg    | 20400.00 |
      | cement kg   | 3400.00  |
      | sand cost   | 20400.00 |
      | stone cost  | 40800.00 |
      | cement cost | 34000.00 |
      | total       | 95200.00 |
      | currency    | RUB      |

  Scenario: Without rounding the batches stay fractional
    When I create a calculation:
      | title | project | full | half | batch | ws  | roundUp |
      | Walls | Alpha   | 1800 | 200  | 4     | 150 | false   |
    Then the result shows:
      | batches   | 67.8571  |
      | sand kg   | 20357.14 |
      | cement kg | 3392.86  |
      | total     | 95000.00 |

  Scenario: A half block counts as 0.5
    When I create a calculation:
      | title | project | full | half | batch | ws  | roundUp |
      | Walls | Alpha   | 10   | 3    | 2     | 150 | false   |
    Then the result shows:
      | blocks | 11.5 |

  Scenario: Batch 1 uses no stone
    When I create a calculation:
      | title | project | full | half | batch | ws  | roundUp |
      | Walls | Alpha   | 100  | 0    | 1     | 150 | true    |
    Then the result shows:
      | batches    | 5       |
      | sand kg    | 2250.00 |
      | stone kg   | 0.00    |
      | cement kg  | 250.00  |
      | total      | 4750.00 |

  Scenario: WS comes from the project when it is not entered
    Given project "Alpha" has default WS 100
    When I create a calculation:
      | title | project | full | half | batch | roundUp |
      | Walls | Alpha   | 28   | 0    | 4     | true    |
    Then the result shows:
      | ws        | 100    |
      | sand kg   | 200.00 |
      | stone kg  | 200.00 |
      | cement kg | 50.00  |

  Scenario: WS entered by hand overrides the project default
    When I create a calculation:
      | title | project | full | half | batch | ws  | roundUp |
      | Walls | Alpha   | 28   | 0    | 4     | 120 | true    |
    Then the result shows:
      | ws      | 120    |
      | sand kg | 240.00 |

  Scenario: The form recalculates while the user types
    When I preview a calculation:
      | project | full | half | batch | ws  | roundUp |
      | Alpha   | 1800 | 200  | 4     | 150 | true    |
    Then the response status is 200
    And the result shows:
      | total | 95200.00 |

  Scenario: A calculation without blocks is rejected
    When I create a calculation:
      | title | project | full | half | batch |
      | Walls | Alpha   | 0    | 0    | 4     |
    Then I see an error message
    And calculation "Walls" no longer exists

  Scenario: An unknown batch is rejected
    When I create a calculation:
      | title | project | full | half | batch |
      | Walls | Alpha   | 100  | 0    | 5     |
    Then I see an error message

  Scenario: A huge number of blocks is rejected
    When I create a calculation:
      | title | project | full        | half | batch |
      | Walls | Alpha   | 20000000000 | 0    | 4     |
    Then the response status is 400

  Scenario: More blocks than the limit are rejected
    When I create a calculation:
      | title | project | full     | half | batch |
      | Walls | Alpha   | 10000001 | 0    | 4     |
    Then I see an error message

  Scenario: A huge WS is rejected
    When I create a calculation:
      | title | project | full | half | batch | ws     |
      | Walls | Alpha   | 100  | 0    | 4     | 100000 |
    Then I see an error message
