Here,The user interacts with the system using the following set of UI commands (each one is a class in the UI_Cmd directory):

ChooseService: this command allows the user to choose which service of the coffee maker to use.  It changes the system mode.

DescribeRecipe: this command allows the user to describe a recipe (it is used in the EditRecipe and AddRecipe modes).

ChooseRecipe: this command allows a user to choose a recipe (it is used in the ChooseRecipe and DeleteRecipe mode).

InsertMoney: this command allows the user to insert money into the machine

Reset: this command aborts the current command and returns to the waiting state.

CheckInventory: this command stores the current inventory as a string that is accessible from the command

AddInventory: this command allows the user to add inventory to the system.

TakeMoneyFromTray: this (somewhat fictitious) command allows a user to retrieve money from the coin return tray, where extra money is deposited after a purchase.

Wrote cucumber test scripts that can describe the user story tests.  Each test step will likely involve either creating a UI Command and evaluating it, or querying the state of the CoffeeMakerUI class. 
