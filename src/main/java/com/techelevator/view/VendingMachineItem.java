package com.techelevator.view;

public class VendingMachineItem {
    private String slotIdentifier;
    private String name;
    private double price;
    private String type;
    private int itemAmount;

    public VendingMachineItem(String slotIdentifier, String name, String price, String type) {
        this.slotIdentifier = slotIdentifier;
        this.name = name;
        this.price = Double.parseDouble(price);
        this.type = type;
        this.itemAmount = 5;
    }

    public String getSlotIdentifier() {
        return slotIdentifier;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public void sellItem() {
        if (itemAmount > 0) {
            itemAmount--;
        }

    }

    @Override
    public String toString() {
        String itemTypeStatement = "";

        if (type.equals("Chip")) {
            itemTypeStatement = "Crunch Crunch, Yum!";
        } else if (type.equals("Candy")) {
            itemTypeStatement = "Munch Munch, Yum!";
        } else if (type.equals("Drink")) {
            itemTypeStatement = "Glug Glug, Yum!";
        } else if (type.equals("Gum")) {
            itemTypeStatement = "Chew Chew, Yum!";
        }

        return "$" + price + " " + name + "\n" + itemTypeStatement;
    }
}
