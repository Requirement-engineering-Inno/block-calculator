@US-14
Feature: Export a calculation to PDF
  As a user I want to download a calculation as a PDF,
  so that I can print it or send it.

  Background:
    Given the following users exist:
      | username | role    |
      | boss     | MANAGER |
      | ann      | WORKER  |
      | bob      | WORKER  |
    And project "Alpha" exists
    And worker "ann" is assigned to project "Alpha"
    And calculation "Walls" exists in project "Alpha" created by "ann"

  Scenario: A worker exports his calculation
    Given I am logged in as "ann"
    When I export calculation "Walls" to PDF
    Then the PDF is returned

  Scenario: A manager exports any calculation
    Given I am logged in as "boss"
    When I export calculation "Walls" to PDF
    Then the PDF is returned

  Scenario: The PDF works with the Russian interface (Cyrillic font)
    Given I am logged in as "ann"
    When I switch the language to "ru"
    And I export calculation "Walls" to PDF
    Then the PDF is returned

  Scenario: A worker without access cannot export
    Given I am logged in as "bob"
    When I export calculation "Walls" to PDF
    Then the response status is 403

  Scenario: The PDF button is on the calculation page
    Given I am logged in as "ann"
    When I open calculation "Walls"
    Then I see the "pdf" button

  Scenario: The PDF uses the time zone of the browser
    Given I am logged in as "ann"
    When I export calculation "Walls" to PDF in time zone "Asia/Novosibirsk"
    Then the PDF is returned

  Scenario: An unknown time zone falls back to GMT
    Given I am logged in as "ann"
    When I export calculation "Walls" to PDF in time zone "Mars/Olympus"
    Then the PDF is returned

  Scenario: The calculation page offers printing
    Given I am logged in as "ann"
    When I open calculation "Walls"
    Then I see the "print" button

  Scenario: The PDF can be opened inline for printing
    Given I am logged in as "ann"
    When I open the PDF of calculation "Walls" for printing
    Then the PDF is shown inline
    And the PDF is returned

  Scenario: Printing is not possible without access
    Given I am logged in as "bob"
    When I open the PDF of calculation "Walls" for printing
    Then the response status is 403
