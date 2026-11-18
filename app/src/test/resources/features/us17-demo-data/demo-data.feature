@US-17
Feature: Demo data for the presentation
  As the team I want the system to start with demo users, projects and calculations,
  so that the demo does not begin with an empty screen.

  Scenario: Demo accounts exist and can sign in
    When the demo data is loaded
    Then the account "elena" exists with role "MANAGER"
    And the account "ivan" exists with role "WORKER"
    When I log in with username "ivan" and password "demo1234"
    Then I am redirected to "/"

  Scenario: Demo projects and calculations are created
    When the demo data is loaded
    Then project "Cape Town housing" has been created
    And calculation "North wall, section A" still exists
    And calculation "Warehouse east wall" is stored with:
      | frozen | true |

  Scenario: A demo worker sees his own calculations and the shared one
    When the demo data is loaded
    And I am logged in as "maria"
    And I open the calculation list
    Then the calculation list contains "Garage foundation"
    And the calculation list contains "North wall, section A"
    And the calculation list does not contain "Fence posts"

  Scenario: Loading the demo data twice does not duplicate anything
    When the demo data is loaded
    And the demo data is loaded
    Then there is exactly one account "elena"
