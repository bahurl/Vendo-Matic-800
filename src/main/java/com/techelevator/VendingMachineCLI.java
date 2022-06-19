package com.techelevator;

import com.techelevator.view.Menu;
import com.techelevator.view.PurchaseException;
import com.techelevator.view.VendingMachineItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final String ONE_DOLLAR = "$1.00";
    private static final String TWO_DOLLARS = "$2.00";
    private static final String FIVE_DOLLARS = "$5.00";
    private static final String TEN_DOLLARS = "$10.00";
    //private static final String FEED_MONEY_EXIT = "Exit";
    //After feeding money, user should choose purchase menu options
    private static final String FEED_MONEY_EXIT = "Choose purchase menu options";
    private static final Object[] FEED_MONEY_MENU = {ONE_DOLLAR, TWO_DOLLARS, FIVE_DOLLARS, TEN_DOLLARS, FEED_MONEY_EXIT};


    private Menu menu;
    private static BigDecimal machineBalance = BigDecimal.ZERO;
    private List<VendingMachineItem> vendingMachineItems = new ArrayList<>();
    private File logFile = new File("log.txt");
    private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");


    public VendingMachineCLI(Menu menu) {
        this.menu = menu;
    }

    public static BigDecimal getMachineBalance() {
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
                    System.out.println(System.lineSeparator() + "Current money provided: $"+ machineBalance.setScale(2, RoundingMode.HALF_UP));
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
            System.out.print("$" + item.getPrice().setScale(2, RoundingMode.HALF_UP) + " ");
            System.out.print(item.getName() + " ");
            System.out.println("Stock: " + (item.getItemAmount() == 0 ? "SOLD OUT" : item.getItemAmount()));
        }
    }

    private void feedMoney() {
        while(true) {
            System.out.println(System.lineSeparator() + "Feed money into the machine in values of $1, $2, $5, or $10");
            System.out.println("Current Money Provided: $" + machineBalance.setScale(2, RoundingMode.HALF_UP));
            Object feedMoneyChoice = menu.getChoiceFromOptions(FEED_MONEY_MENU);
            if(feedMoneyChoice.equals(FEED_MONEY_EXIT)) {
                break;
            } else {
                try {
                    BigDecimal moneyChoice = new BigDecimal(feedMoneyChoice.toString().substring(1));
                    machineBalance = machineBalance.add(moneyChoice);
                    logTransactions("FEED MONEY: $"+moneyChoice.setScale(2, RoundingMode.HALF_UP)+" $"+machineBalance.setScale(2, RoundingMode.HALF_UP));
                } catch (NumberFormatException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private void purchaseProduct() throws PurchaseException {
        Scanner input = new Scanner(System.in);

        displayItems();
        System.out.println("Choose item by entering display code: ");
        String displayChoice = input.nextLine();
        VendingMachineItem chosenItem = null;

        for(int i = 0; i < vendingMachineItems.size(); i++) {
            if(displayChoice.equalsIgnoreCase(vendingMachineItems.get(i).getSlotIdentifier())) {
                chosenItem = vendingMachineItems.get(i);
                break;
            }
        }

        if(chosenItem == null) {
            throw new PurchaseException("Product does not exist");
        } else if(chosenItem.getItemAmount() == 0) {
            throw new PurchaseException("Product is sold out");
        } else if(machineBalance.compareTo(chosenItem.getPrice()) == -1) {
            throw new PurchaseException("Balance is not high enough to purchase this item");
        } else {
            BigDecimal originalMachineBalance = machineBalance;
            machineBalance = machineBalance.subtract(chosenItem.getPrice());
            //machineBalance -= chosenItem.getPrice();
            chosenItem.sellItem();
            System.out.println(chosenItem +"\nRemaining balance: " +machineBalance.setScale(2, RoundingMode.HALF_UP));
            logTransactions(chosenItem.getName() +" "+ chosenItem.getSlotIdentifier()+ " $"+originalMachineBalance.setScale(2, RoundingMode.HALF_UP)+" $"+machineBalance.setScale(2, RoundingMode.HALF_UP));
        }
    }

    private void makeChange() {
        logTransactions("GIVE CHANGE: $"+machineBalance.setScale(2, RoundingMode.HALF_UP)+" $0.00");

        Map<String, Integer> changeReturned = new HashMap<>();

        while(machineBalance.doubleValue() > 0) {
            if(machineBalance.doubleValue() >= .25) {
                if(changeReturned.containsKey("Quarter")) {
                    changeReturned.put("Quarter", (changeReturned.get("Quarter") + 1));
                } else {
                    changeReturned.put("Quarter", 1);
                }

                machineBalance = machineBalance.subtract(new BigDecimal(.25));
                //machineBalance -= .25;
            } else if (machineBalance.doubleValue() >= .10) {
                if(changeReturned.containsKey("Dime")) {
                    changeReturned.put("Dime", (changeReturned.get("Dime") + 1));
                } else {
                    changeReturned.put("Dime", 1);
                }

                machineBalance = machineBalance.subtract(new BigDecimal(.10));
                //machineBalance -= 10;
            } else if(machineBalance.doubleValue() >= .05) {
                if(changeReturned.containsKey("Nickel")) {
                    changeReturned.put("Nickel", (changeReturned.get("Nickel") + 1));
                } else {
                    changeReturned.put("Nickel", 1);
                }

                machineBalance = machineBalance.subtract(new BigDecimal(.05));
                //machineBalance -= 5;
            } else {
                machineBalance = BigDecimal.ZERO;
            }
        }

        System.out.println("Change returned:");
        for(Map.Entry<String, Integer> coin: changeReturned.entrySet()) {
            System.out.println(coin.getValue() +" "+ coin.getKey() + (coin.getValue() > 1 ? "s" : ""));
        }

    }

    private void logTransactions(String logMessage){
        try(PrintWriter logOutput = new PrintWriter(new FileOutputStream(logFile, true))){
            logOutput.println(">" + formatter.format(new Date()) + " " + logMessage);
        } catch (FileNotFoundException e){
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Menu menu = new Menu(System.in, System.out);
        VendingMachineCLI cli = new VendingMachineCLI(menu);
        cli.run();
    }
}
