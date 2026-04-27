Feature: CoffeeMakerFeature

In this feature, we are going to test the user stories and use cases for the CoffeeMaker
Example.  We have provided a CoffeeMakerMain.java file that you can use to examine the
modal behavior in the coffee maker and issue UI commands to use it, so that we can
adequately test the user stories.

  # =========================================================
  # WAITING STATE
  # =========================================================

  Scenario Outline: Waiting State - Mode Transitions
    Given a default recipe book
    When I choose the <service> service
    Then the system should be in <expected_mode> mode

    Examples:
      | service           | expected_mode     |
      | ADD_RECIPE        | ADD_RECIPE        |
      | DELETE_RECIPE     | DELETE_RECIPE     |
      | EDIT_RECIPE       | EDIT_RECIPE       |
      | ADD_INVENTORY     | ADD_INVENTORY     |
      | CHECK_INVENTORY   | CHECK_INVENTORY   |
      | PURCHASE_BEVERAGE | PURCHASE_BEVERAGE |

  Scenario: Waiting State - Reset returns to WAITING
    Given a default recipe book
    When I choose the ADD_RECIPE service
    And I reset the coffee maker
    Then the system should be in WAITING mode

  Scenario: Waiting State - Status is OK at start
    Given a default recipe book
    Then the status should be OK

  # =========================================================
  # ADD A RECIPE
  # =========================================================

  Scenario: Add a Recipe - successfully adds valid recipe
    Given an empty recipe book
    When I choose the ADD_RECIPE service
    And I enter recipe details as "Coffee", 50, 3, 1, 1, 0
    Then the recipe "Coffee" should be added successfully
    And the system should be in WAITING mode
    And the status should be OK

  Scenario: Add a Recipe - cannot add duplicate recipe name
    Given an empty recipe book
    When I choose the ADD_RECIPE service
    And I enter recipe details as "Coffee", 50, 3, 1, 1, 0
    When I choose the ADD_RECIPE service
    And I enter recipe details as "Coffee", 60, 2, 2, 1, 0
    Then only one recipe named "Coffee" should exist

  Scenario: Add a Recipe - cannot add more than 3 recipes
    Given an empty recipe book
    And I have added 3 valid recipes
    When I try to add a 4th recipe "ExtraLatte"
    Then the recipe "ExtraLatte" should not be added
    And the status should be RECIPE_NOT_ADDED

  Scenario: Add a Recipe - returns to WAITING after add
    Given an empty recipe book
    When I choose the ADD_RECIPE service
    And I enter recipe details as "Mocha", 75, 2, 2, 1, 2
    Then the system should be in WAITING mode

  # =========================================================
  # DELETE A RECIPE
  # =========================================================

  Scenario: Delete a Recipe - successfully deletes existing recipe
    Given a default recipe book
    When I choose the DELETE_RECIPE service
    And I select the "Coffee" recipe
    Then the recipe "Coffee" should not exist in the recipe book
    And the system should be in WAITING mode
    And the status should be OK

  Scenario: Delete a Recipe - deleted slot can accept new recipe
    Given a default recipe book
    When I choose the DELETE_RECIPE service
    And I select the "Coffee" recipe
    When I choose the ADD_RECIPE service
    And I enter recipe details as "NewDrink", 40, 2, 1, 1, 0
    Then the recipe "NewDrink" should be added successfully

  Scenario: Delete a Recipe - other recipes remain after delete
    Given an empty recipe book
    When I choose the ADD_RECIPE service
    And I enter recipe details as "Coffee", 50, 3, 1, 1, 0
    When I choose the ADD_RECIPE service
    And I enter recipe details as "Latte", 75, 2, 3, 1, 0
    When I choose the DELETE_RECIPE service
    And I select the "Coffee" recipe
    Then the recipe "Coffee" should not exist in the recipe book
    And the recipe "Latte" should be added successfully

  # =========================================================
  # EDIT A RECIPE
  # =========================================================

  Scenario: Edit a Recipe - price is updated correctly
    Given a default recipe book
    When I choose the EDIT_RECIPE service
    And I select the "Coffee" recipe
    And I enter recipe details as "Coffee", 75, 3, 1, 1, 0
    Then the recipe "Coffee" should have price 75
    And the system should be in WAITING mode
    And the status should be OK

  Scenario: Edit a Recipe - name is preserved after edit
    Given a default recipe book
    When I choose the EDIT_RECIPE service
    And I select the "Coffee" recipe
    And I enter recipe details as "Coffee", 75, 4, 2, 1, 0
    Then the recipe "Coffee" should be added successfully

  Scenario: Edit a Recipe - coffee amount is updated correctly
    Given a default recipe book
    When I choose the EDIT_RECIPE service
    And I select the "Coffee" recipe
    And I enter recipe details as "Coffee", 50, 5, 1, 1, 0
    Then the recipe "Coffee" should have coffee amount 5

  # =========================================================
  # ADD INVENTORY
  # =========================================================

  Scenario: Add Inventory - valid amounts increase inventory
    Given a default recipe book
    When I choose the ADD_INVENTORY service
    And I add inventory with coffee 5, milk 3, sugar 2, chocolate 4
    Then the inventory string should contain "Coffee: 20"
    And the inventory string should contain "Milk: 18"
    And the system should be in WAITING mode
    And the status should be OK

  Scenario: Add Inventory - adding sugar increases sugar stock
    Given a default recipe book
    When I choose the ADD_INVENTORY service
    And I add inventory with coffee 0, milk 0, sugar 5, chocolate 0
    Then the inventory string should contain "Sugar: 20"

  Scenario: Add Inventory - negative coffee is rejected
    Given a default recipe book
    When I choose the ADD_INVENTORY service
    And I add inventory with coffee -5, milk 0, sugar 0, chocolate 0
    Then the status should be OUT_OF_RANGE
    And the inventory string should contain "Coffee: 15"

  Scenario: Add Inventory - negative milk is rejected
    Given a default recipe book
    When I choose the ADD_INVENTORY service
    And I add inventory with coffee 0, milk -3, sugar 0, chocolate 0
    Then the status should be OUT_OF_RANGE

  Scenario: Add Inventory - negative chocolate is rejected
    Given a default recipe book
    When I choose the ADD_INVENTORY service
    And I add inventory with coffee 0, milk 0, sugar 0, chocolate -1
    Then the status should be OUT_OF_RANGE

  Scenario: Add Inventory - returns to WAITING after adding
    Given a default recipe book
    When I choose the ADD_INVENTORY service
    And I add inventory with coffee 1, milk 1, sugar 1, chocolate 1
    Then the system should be in WAITING mode

  # =========================================================
  # CHECK INVENTORY
  # =========================================================

  Scenario: Check Inventory - displays inventory and returns to WAITING
    Given a default recipe book
    When I choose the CHECK_INVENTORY service
    And I check the inventory
    Then the inventory string should not be empty
    And the inventory string should contain "Coffee: 15"
    And the inventory string should contain "Milk: 15"
    And the inventory string should contain "Sugar: 15"
    And the inventory string should contain "Chocolate: 15"
    And the system should be in WAITING mode
    And the status should be OK

  # =========================================================
  # PURCHASE BEVERAGE
  # =========================================================

  Scenario: Purchase Beverage - exact payment no change
    Given a default recipe book
    When I choose the PURCHASE_BEVERAGE service
    And I insert 50 cents
    And I select the "Coffee" recipe
    Then the change in the tray should be 0 cents
    And the system should be in WAITING mode
    And the status should be OK

  Scenario: Purchase Beverage - overpayment returns correct change
    Given a default recipe book
    When I choose the PURCHASE_BEVERAGE service
    And I insert 100 cents
    And I select the "Coffee" recipe
    Then the change in the tray should be 50 cents
    And the system should be in WAITING mode

  Scenario: Purchase Beverage - insufficient funds returns money
    Given a default recipe book
    When I choose the PURCHASE_BEVERAGE service
    And I insert 25 cents
    And I select the "Coffee" recipe
    Then the status should be INSUFFICIENT_FUNDS
    And the system should be in WAITING mode

  Scenario: Purchase Beverage - coffee decreases after purchase
    Given a default recipe book
    When I choose the PURCHASE_BEVERAGE service
    And I insert 50 cents
    And I select the "Coffee" recipe
    When I choose the CHECK_INVENTORY service
    And I check the inventory
    Then the inventory string should contain "Coffee: 12"

  Scenario: Purchase Beverage - milk decreases after purchase
    Given a default recipe book
    When I choose the PURCHASE_BEVERAGE service
    And I insert 50 cents
    And I select the "Coffee" recipe
    When I choose the CHECK_INVENTORY service
    And I check the inventory
    Then the inventory string should contain "Milk: 14"

  Scenario: Purchase Beverage - sugar decreases after purchase
    Given a default recipe book
    When I choose the PURCHASE_BEVERAGE service
    And I insert 50 cents
    And I select the "Coffee" recipe
    When I choose the CHECK_INVENTORY service
    And I check the inventory
    Then the inventory string should contain "Sugar: 14"

  Scenario: Purchase Beverage - accumulated money works
    Given a default recipe book
    When I choose the PURCHASE_BEVERAGE service
    And I insert 25 cents
    And I insert 25 cents
    And I select the "Coffee" recipe
    Then the change in the tray should be 0 cents
    And the status should be OK

  Scenario: Purchase Beverage - take money from tray clears it
    Given a default recipe book
    When I choose the PURCHASE_BEVERAGE service
    And I insert 100 cents
    And I select the "Coffee" recipe
    And I take money from the tray
    Then the change in the tray should be 0 cents

  Scenario: Purchase Beverage - insufficient inventory returns money
    Given an empty recipe book
    When I choose the ADD_RECIPE service
    And I enter recipe details as "BigCoffee", 50, 20, 1, 1, 0
    When I choose the PURCHASE_BEVERAGE service
    And I insert 100 cents
    And I select the "BigCoffee" recipe
    Then the status should be INSUFFICIENT_FUNDS
    And the system should be in WAITING mode
