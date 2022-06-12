package com.techelevator;

import com.techelevator.view.Menu;
import com.techelevator.view.VendingMachingItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;


public class VendingMachineCLI {

    private static final String MAIN_MENU_OPTION_DISPLAY_ITEMS = "Display Vending Machine Items";
    private static final String MAIN_MENU_OPTION_PURCHASE = "Purchase";
    private static final String MAIN_MENU_OPTION_EXIT = "Exit";

    private static final String[] MAIN_MENU_OPTIONS =
            {MAIN_MENU_OPTION_DISPLAY_ITEMS, MAIN_MENU_OPTION_PURCHASE, MAIN_MENU_OPTION_EXIT};
    private static final String PURCHASE_MENU_FEED_MONEY = "Feed Money";
    private static final String PURCHASE_MENU_SELECT_PRODUCT = "Select Product";
    private static final String PURCHASE_MENU_FINISH_TRANSACTION = "Finish Transaction";
    private static final String[] PURCHASE_MENU_OPTIONS =
            {PURCHASE_MENU_FEED_MONEY, PURCHASE_MENU_SELECT_PRODUCT, PURCHASE_MENU_FINISH_TRANSACTION};
    private static final double ONE_DOLLAR = 1;
    private static final double TWO_DOLLARS = 2;
    private static final double FIVE_DOLLARS = 5;
    private static final double TEN_DOLLARS = 10;
    private static final String FEED_MONEY_EXIT = "Exit";
    private static final Object[] FEED_MONEY_MENU = {ONE_DOLLAR, TWO_DOLLARS, FIVE_DOLLARS, TEN_DOLLARS, FEED_MONEY_EXIT};


    private Menu menu;
    private static double machineBalance = 0;
    private List<VendingMachingItem> vendingMachineItems = new ArrayList<>();
    private File logFile = new File("log.txt");
    private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");


    public VendingMachineCLI(Menu menu) {
        this.menu = menu;
    }

    public static double getMachineBalance() {
        return machineBalance;
    }

    public void run() {
        stockVendingMachine();
        while (true) {
            String choice = (String) menu.getChoiceFromOptions(MAIN_MENU_OPTIONS);

            if (choice.equals(MAIN_MENU_OPTION_DISPLAY_ITEMS)) {
                // display vending machine items
                displayItems();
            } else if (choice.equals(MAIN_MENU_OPTION_PURCHASE)) {
                // do purchase
                while (true) {
                    String purchaseChoice = (String) menu.getChoiceFromOptions(PURCHASE_MENU_OPTIONS);
                    if (purchaseChoice.equals(PURCHASE_MENU_FEED_MONEY)) {
                        feedMoney();

                    } else if (purchaseChoice.equals(PURCHASE_MENU_SELECT_PRODUCT)) {
                        purchaseProduct();

                    } else if (purchaseChoice.equals(PURCHASE_MENU_FINISH_TRANSACTION)) {
                        makeChange();
                        break;

                    }

                }
            } else if (choice.equals(MAIN_MENU_OPTION_EXIT)) {
                return;

            }
        }
    }

    private void stockVendingMachine() {
        File inventoryFile = new File("vendingmachine.csv");
        try (Scanner inventory = new Scanner(inventoryFile)) {
            while (inventory.hasNext()) {
                String input = inventory.nextLine();
                String[] itemInfo = input.split("\\|");
                VendingMachingItem item = new VendingMachingItem(itemInfo[0], itemInfo[1], itemInfo[2], itemInfo[3]);
                vendingMachineItems.add(item);
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void displayItems() {
        for (VendingMachingItem item : vendingMachineItems) {

            System.out.print(item.getSlotIdentifier() + ") ");
            System.out.print("$" + item.getPrice() + " ");
            System.out.print(item.getName() + " ");
            System.out.println("Stock: " + (item.getItemAmount() == 0 ? "SOLD OUT" : item.getItemAmount()));
        }
    }

    private void feedMoney() {
        while (true) {
            System.out.println("Feed money in to the machine in values of $1, $2, $5, or $10");
            Object feedMoneyChoice = menu.getChoiceFromOptions(FEED_MONEY_MENU);
            if (feedMoneyChoice.equals(FEED_MONEY_EXIT)) {
                break;
            } else {
                machineBalance += (double) feedMoneyChoice;
                //log trasactions
            }

        }

    }

    private void purchaseProduct() {
        Scanner input = new Scanner(System.in);
        displayItems();
        System.out.println("Choose item by entering display code: ");
        String displayChoice = input.nextLine();
        VendingMachingItem chosenItem = null;
        for (int i = 0; i < vendingMachineItems.size(); i++) {
            if (displayChoice.equalsIgnoreCase(vendingMachineItems.get(i).getSlotIdentifier())) {
                chosenItem = vendingMachineItems.get(i);
                break;
            }
        }
        if (chosenItem == null) {
            System.out.println("Product does not exist");
        } else if (chosenItem.getItemAmount() == 0) {
            System.out.println("Product is sold out");

        } else if (machineBalance < chosenItem.getPrice()) {
            System.out.println("Balance is not high enough to purchase this product");

        } else {
            machineBalance -= chosenItem.getPrice();
            chosenItem.sellItem();
            System.out.println(chosenItem + "\nRemaining balance: " + machineBalance);
            //log transaction
        }

    }

    private void makeChange() {
        //log transaction
        Map<String, Integer> changeReturned = new HashMap<>();
        machineBalance *= 100;
        while (machineBalance > 0) {
            if (machineBalance >= 25) {
                if (changeReturned.containsKey("Quarter")) {
                    changeReturned.put("Quarter", (changeReturned.get("Quarter") + 1));
                } else {
                    changeReturned.put("Quarter", 1);
                }

                machineBalance -= 25;
            } else if (machineBalance >= 10) {
                if (changeReturned.containsKey("Dime")) {
                    changeReturned.put("Dime", (changeReturned.get("Dime") + 1));
                } else {
                    changeReturned.put("Dime", 1);
                }
                machineBalance -= 10;
            } else if (machineBalance >= 5) {
                if (changeReturned.containsKey("Nickel")) {
                    changeReturned.put("Nickel", (changeReturned.get("Nickel") + 1));
                } else {
                    changeReturned.put("Nickel", 1);
                }
                machineBalance -= 5;
            } else {
                machineBalance = 0;
            }
        }
        System.out.println("Change returned:");
        for (Map.Entry<String, Integer> coin : changeReturned.entrySet()) {
            System.out.println(coin.getValue() + " " + coin.getKey() + (coin.getValue() > 1 ? "s" : ""));
        }


    }


    public static void main(String[] args) {
        Menu menu = new Menu(System.in, System.out);
        VendingMachineCLI cli = new VendingMachineCLI(menu);
        cli.run();
    }
}
