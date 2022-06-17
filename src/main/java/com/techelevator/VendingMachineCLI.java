package com.techelevator;

import com.techelevator.view.Menu;
import com.techelevator.view.PurchaseException;
import com.techelevator.view.VendingMachineItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;


public class VendingMachineCLI {

    //Main menu options
    private static final String MAIN_MENU_OPTION_DISPLAY_ITEMS = "Display Vending Machine Items";
    private static final String MAIN_MENU_OPTION_PURCHASE = "Purchase";
    private static final String MAIN_MENU_OPTION_EXIT = "Exit";
    private static final String[] MAIN_MENU_OPTIONS =
            {MAIN_MENU_OPTION_DISPLAY_ITEMS, MAIN_MENU_OPTION_PURCHASE, MAIN_MENU_OPTION_EXIT};

    //purchase menu options
    private static final String PURCHASE_MENU_FEED_MONEY = "Feed Money";
    private static final String PURCHASE_MENU_SELECT_PRODUCT = "Select Product";
    private static final String PURCHASE_MENU_FINISH_TRANSACTION = "Finish Transaction";
    private static final String[] PURCHASE_MENU_OPTIONS =
            {PURCHASE_MENU_FEED_MONEY, PURCHASE_MENU_SELECT_PRODUCT, PURCHASE_MENU_FINISH_TRANSACTION};

    //Feed money menu options
    private static final double ONE_DOLLAR = 1;
    private static final double TWO_DOLLARS = 2;
    private static final double FIVE_DOLLARS = 5;
    private static final double TEN_DOLLARS = 10;
    private static final String FEED_MONEY_EXIT = "Exit";
    private static final Object[] FEED_MONEY_MENU = {ONE_DOLLAR, TWO_DOLLARS, FIVE_DOLLARS, TEN_DOLLARS, FEED_MONEY_EXIT};


    private Menu menu;
    private static double machineBalance = 0;
    private List<VendingMachineItem> vendingMachineItems = new ArrayList<>();
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

        while (true) { //Main menu - Display items, purchase, exit
            String choice = (String) menu.getChoiceFromOptions(MAIN_MENU_OPTIONS);

            if (choice.equals(MAIN_MENU_OPTION_DISPLAY_ITEMS)) {
                displayItems();
            } else if (choice.equals(MAIN_MENU_OPTION_PURCHASE)) {

                while (true) { //Purchase menu - Feed money, select product, finish transaction
                    //out.print(System.lineSeparator() + "Current money provided: $"+ VendingMachineCLI.getMachineBalance());
                    System.out.println("Current money provided: $"+ machineBalance);
                    String purchaseChoice = (String) menu.getChoiceFromOptions(PURCHASE_MENU_OPTIONS);
                    if (purchaseChoice.equals(PURCHASE_MENU_FEED_MONEY)) {
                        feedMoney();
                    } else if (purchaseChoice.equals(PURCHASE_MENU_SELECT_PRODUCT)) {
                        try{
                            purchaseProduct();
                        } catch (PurchaseException e){
                            System.out.println("Error: " + e.getMessage());
                        }
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
                VendingMachineItem item = new VendingMachineItem(itemInfo[0], itemInfo[1], itemInfo[2], itemInfo[3]);
                vendingMachineItems.add(item);
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void displayItems() {
        for (VendingMachineItem item : vendingMachineItems) {

            System.out.print(item.getSlotIdentifier() + ") ");
            System.out.print("$" + item.getPrice() + " ");
            System.out.print(item.getName() + " ");
            System.out.println("Stock: " + (item.getItemAmount() == 0 ? "SOLD OUT" : item.getItemAmount()));
        }
    }

    public void feedMoney() {
        while (true) {
            System.out.println("Feed money in to the machine in values of $1, $2, $5, or $10");
            Object feedMoneyChoice = menu.getChoiceFromOptions(FEED_MONEY_MENU);
            if (feedMoneyChoice.equals(FEED_MONEY_EXIT)) {
                break;
            } else {
                machineBalance += (double) feedMoneyChoice;
                logTransactions("FEED MONEY: $" + feedMoneyChoice + " $" + machineBalance);
            }
        }
    }

    private void purchaseProduct() throws PurchaseException {
        Scanner input = new Scanner(System.in);

        displayItems();

        System.out.println("Choose item by entering display code: ");
        String displayChoice = input.nextLine();
        VendingMachineItem chosenItem = null;

        for (int i = 0; i < vendingMachineItems.size(); i++) {
            if (displayChoice.equalsIgnoreCase(vendingMachineItems.get(i).getSlotIdentifier())) {
                chosenItem = vendingMachineItems.get(i);
                break;
            }
        }

        if (chosenItem == null) {
            throw new PurchaseException("Product does not exist");
        } else if (chosenItem.getItemAmount() == 0) {
            throw new PurchaseException("Product is sold out");
        } else if (machineBalance < chosenItem.getPrice()) {
            throw new PurchaseException("Balance is not high enough to purchase this product");
        } else {
            double originalBalance = machineBalance;
            machineBalance -= chosenItem.getPrice();
            chosenItem.sellItem();
            System.out.println(chosenItem + "\nRemaining balance: " + machineBalance);
            logTransactions(chosenItem.getName() + " " + chosenItem.getSlotIdentifier() + " $" + originalBalance + " $" + machineBalance );
        }
    }

    private void makeChange() {
        logTransactions("GIVE CHANGE: $" + machineBalance + " $0.00");

        Map<String, Integer> changeReturned = new HashMap<>();

        machineBalance *= 100;

        while (machineBalance > 0) {
            if (machineBalance >= 25) { //adds quarters
                if (changeReturned.containsKey("Quarter")) {
                    changeReturned.put("Quarter", (changeReturned.get("Quarter") + 1));
                } else {
                    changeReturned.put("Quarter", 1);
                }

                machineBalance -= 25;
            } else if (machineBalance >= 10) { //adds dimes

                if (changeReturned.containsKey("Dime")) {
                    changeReturned.put("Dime", (changeReturned.get("Dime") + 1));
                } else {
                    changeReturned.put("Dime", 1);
                }

                machineBalance -= 10;
            } else if (machineBalance >= 5) { //adds nickels
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

        //loops through the map and prints out the change
        for (Map.Entry<String, Integer> coin : changeReturned.entrySet()) {
            System.out.println(coin.getValue() + " " + coin.getKey() + (coin.getValue() > 1 ? "s" : ""));
        }

    }
    private void logTransactions(String logMessage){
        try(PrintWriter logOutput = new PrintWriter(new FileOutputStream(logFile, true))){
            logOutput.println(">" + formatter.format(new Date()) + " " + logMessage);

        }
        catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        }

    }

    public static void main(String[] args) {
        Menu menu = new Menu(System.in, System.out);
        VendingMachineCLI cli = new VendingMachineCLI(menu);
        cli.run();
    }
}
