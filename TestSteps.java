package edu.ncsu.csc326.coffeemaker;

import static org.junit.Assert.*;
import cucumber.api.java.en.*;
import cucumber.api.java.Before;
import edu.ncsu.csc326.coffeemaker.UICmd.*;

/**
 * Contains the step definitions for the cucumber tests. This parses the
 * Gherkin steps and translates them into meaningful test steps.
 *
 * Uses ONLY methods that exist on CoffeeMakerUI:
 *   - ui.getMode()
 *   - ui.getStatus()
 *   - ui.getMoneyInTray()
 *   - ui.getMoneyInserted()
 *   - ui.getRecipes()
 *   - ui.UI_Input(Command)
 *
 * Inventory is checked via CheckInventory command + getInventory() string,
 * since CoffeeMakerUI has no direct getInventoryCoffee() etc. methods.
 */
public class TestSteps {

    private CoffeeMakerUI ui;
    private CoffeeMaker cm;
    private RecipeBook rb;
    private String lastInventoryString;

    @Before
    public void setUp() {
        rb = new RecipeBook();
        cm = new CoffeeMaker(rb, new Inventory());
        ui = new CoffeeMakerUI(cm);
        lastInventoryString = "";
    }

    // =========================================================
    // GIVEN STEPS
    // =========================================================

    @Given("^the coffee maker is in WAITING mode$")
    public void is_in_waiting_mode() {
        ui.UI_Input(new Reset());
        assertEquals(CoffeeMakerUI.Mode.WAITING, ui.getMode());
    }

    @Given("^an empty recipe book$")
    public void empty_book() {
        int count = 0;
        for (Recipe r : ui.getRecipes()) {
            if (r != null && !r.getName().equals("")) count++;
        }
        assertEquals("Recipe book should be empty", 0, count);
    }

    @Given("^a default recipe book$")
    public void default_book() {
        // Add a standard Coffee recipe: price=50, coffee=3, milk=1, sugar=1, choc=0
        ui.UI_Input(new ChooseService(1)); // ADD_RECIPE
        ui.UI_Input(new DescribeRecipe(createRecipe("Coffee", 50, 3, 1, 1, 0)));
        // Return to clean WAITING state
        ui.UI_Input(new Reset());
    }

    @Given("^I have added 3 valid recipes$")
    public void add_three_recipes() {
        for (int i = 0; i < 3; i++) {
            ui.UI_Input(new ChooseService(1));
            ui.UI_Input(new DescribeRecipe(createRecipe("Recipe" + i, 50, 1, 1, 1, 0)));
        }
    }

    // =========================================================
    // WHEN STEPS
    // =========================================================

    @When("^I choose the (.*) service$")
    public void choose_service(String service) {
        int selection;
        switch (service) {
            case "ADD_RECIPE":        selection = 1; break;
            case "DELETE_RECIPE":     selection = 2; break;
            case "EDIT_RECIPE":       selection = 3; break;
            case "ADD_INVENTORY":     selection = 4; break;
            case "CHECK_INVENTORY":   selection = 5; break;
            case "PURCHASE_BEVERAGE": selection = 6; break;
            default:                  selection = 0; break;
        }
        ui.UI_Input(new ChooseService(selection));
    }

    @When("^I enter recipe details as \"(.*)\", (-?\\d+), (-?\\d+), (-?\\d+), (-?\\d+), (-?\\d+)$")
    public void enter_recipe(String name, int price, int coffee, int milk, int sugar, int chocolate) {
        ui.UI_Input(new DescribeRecipe(createRecipe(name, price, coffee, milk, sugar, chocolate)));
    }

    @When("^I try to add a 4th recipe \"(.*)\"$")
    public void add_fourth_recipe(String name) {
        ui.UI_Input(new ChooseService(1));
        ui.UI_Input(new DescribeRecipe(createRecipe(name, 50, 1, 1, 1, 0)));
    }

    @When("^I select the \"(.*)\" recipe$")
    public void select_recipe(String name) {
        Recipe[] recipes = ui.getRecipes();
        for (int i = 0; i < recipes.length; i++) {
            if (recipes[i] != null && recipes[i].getName().equals(name)) {
                ui.UI_Input(new ChooseRecipe(i));
                return;
            }
        }
        fail("Recipe not found in book: " + name);
    }

    @When("^I insert (-?\\d+) cents$")
    public void insert_money(int amt) {
        ui.UI_Input(new InsertMoney(amt));
    }

    @When("^I reset the coffee maker$")
    public void reset_machine() {
        ui.UI_Input(new Reset());
    }

    @When("^I check the inventory$")
    public void check_inventory() {
        CheckInventory cmd = new CheckInventory();
        ui.UI_Input(cmd);
        lastInventoryString = cmd.getInventory();
        if (lastInventoryString == null) lastInventoryString = "";
    }

    @When("^I take money from the tray$")
    public void take_money_from_tray() {
        ui.UI_Input(new TakeMoneyFromTray());
    }

    @When("^I add inventory with coffee (-?\\d+), milk (-?\\d+), sugar (-?\\d+), chocolate (-?\\d+)$")
    public void add_inventory_all(int coffee, int milk, int sugar, int chocolate) {
        ui.UI_Input(new AddInventory(coffee, milk, sugar, chocolate));
    }

    // =========================================================
    // THEN STEPS
    // =========================================================

    @Then("^the system should be in (.*) mode$")
    public void check_mode(String mode) {
        assertEquals(CoffeeMakerUI.Mode.valueOf(mode), ui.getMode());
    }

    @Then("^the status should be (OK|RECIPE_NOT_ADDED|OUT_OF_RANGE|INSUFFICIENT_FUNDS|WRONG_MODE|UNKNOWN_ERROR)$")
    public void check_status(String status) {
        assertEquals(CoffeeMakerUI.Status.valueOf(status), ui.getStatus());
    }

    @Then("^the recipe \"(.*)\" should be added successfully$")
    public void recipe_added(String name) {
        boolean found = false;
        for (Recipe r : ui.getRecipes()) {
            if (r != null && r.getName().equals(name)) {
                found = true;
                break;
            }
        }
        assertTrue("Expected recipe '" + name + "' to exist in recipe book", found);
    }

    @Then("^the recipe \"(.*)\" should not be added$")
    public void recipe_not_added(String name) {
        for (Recipe r : ui.getRecipes()) {
            if (r != null) {
                assertNotEquals("Recipe '" + name + "' should NOT be in recipe book", name, r.getName());
            }
        }
    }

    @Then("^the recipe \"(.*)\" should not exist in the recipe book$")
    public void recipe_not_in_book(String name) {
        for (Recipe r : ui.getRecipes()) {
            if (r != null) {
                assertNotEquals("Recipe '" + name + "' should have been deleted", name, r.getName());
            }
        }
    }

    @Then("^only one recipe named \"(.*)\" should exist$")
    public void only_one_recipe(String name) {
        int count = 0;
        for (Recipe r : ui.getRecipes()) {
            if (r != null && r.getName().equals(name)) count++;
        }
        assertEquals("Expected exactly one recipe named '" + name + "'", 1, count);
    }

    @Then("^the recipe \"(.*)\" should have price (\\d+)$")
    public void recipe_has_price(String name, int expectedPrice) {
        for (Recipe r : ui.getRecipes()) {
            if (r != null && r.getName().equals(name)) {
                assertEquals("Price of recipe '" + name + "' is wrong", expectedPrice, r.getPrice());
                return;
            }
        }
        fail("Recipe '" + name + "' not found when checking price");
    }

    @Then("^the recipe \"(.*)\" should have coffee amount (\\d+)$")
    public void recipe_has_coffee_amount(String name, int expectedAmt) {
        for (Recipe r : ui.getRecipes()) {
            if (r != null && r.getName().equals(name)) {
                assertEquals("Coffee amount of recipe '" + name + "' is wrong", expectedAmt, r.getAmtCoffee());
                return;
            }
        }
        fail("Recipe '" + name + "' not found when checking coffee amount");
    }

    @Then("^the change in the tray should be (\\d+) cents$")
    public void check_change(int expectedChange) {
        assertEquals("Change in tray is wrong", expectedChange, ui.getMoneyInTray());
    }

    @Then("^the inventory string should not be empty$")
    public void inventory_not_empty() {
        assertNotNull("Inventory string should not be null", lastInventoryString);
        assertFalse("Inventory string should not be empty", lastInventoryString.trim().isEmpty());
    }

    @Then("^the inventory string should contain \"(.*)\"$")
    public void inventory_contains(String expected) {
        // Re-fetch inventory if not already fetched in this scenario
        if (lastInventoryString.isEmpty()) {
            CheckInventory cmd = new CheckInventory();
            // Need to be in CHECK_INVENTORY mode first
            ui.UI_Input(new ChooseService(5));
            ui.UI_Input(cmd);
            lastInventoryString = cmd.getInventory();
            if (lastInventoryString == null) lastInventoryString = "";
        }
        assertTrue(
            "Inventory string should contain '" + expected + "' but was: " + lastInventoryString,
            lastInventoryString.contains(expected)
        );
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    /**
     * Creates a Recipe object. Invalid values (negative) are caught and left
     * at default (0), which will cause addRecipe to still be attempted so we
     * can check the system's response to bad input.
     */
    private Recipe createRecipe(String name, int price, int coffee, int milk, int sugar, int chocolate) {
        Recipe r = new Recipe();
        r.setName(name);
        try { r.setPrice(Integer.toString(price)); }       catch (Exception e) { /* leave default */ }
        try { r.setAmtCoffee(Integer.toString(coffee)); }  catch (Exception e) { /* leave default */ }
        try { r.setAmtMilk(Integer.toString(milk)); }      catch (Exception e) { /* leave default */ }
        try { r.setAmtSugar(Integer.toString(sugar)); }    catch (Exception e) { /* leave default */ }
        try { r.setAmtChocolate(Integer.toString(chocolate)); } catch (Exception e) { /* leave default */ }
        return r;
    }
}
