package com.techelevator.view;

import org.junit.Assert;
import org.junit.Test;

public class VendingMachineItemTest {
    @Test
    public void vending_machine_returns_chip_dispensing_message() {
        VendingMachineItem chipItem = new VendingMachineItem("A1", "Potato Crisps", "3.05", "Chip");
        String chipDispensingMessage = chipItem.toString();
        Assert.assertEquals("$3.05 Potato Crisps\nCrunch Crunch, Yum!", chipDispensingMessage);

    }

    @Test
    public void vending_machine_returns_candy_dispensing_message() {
        VendingMachineItem candyItem = new VendingMachineItem("B3", "Wonka Bar", "1.50", "Candy");
        String candyDispensingMessage = candyItem.toString();
        Assert.assertEquals("$1.5 Wonka Bar\nMunch Munch, Yum!", candyDispensingMessage);
    }

    @Test
    public void vending_machine_returns_drink_dispensing_message() {
        VendingMachineItem drinkItem = new VendingMachineItem("C2", "Dr. Salt", "1.50", "Drink");
        String drinkDispensingMessage = drinkItem.toString();
        Assert.assertEquals("$1.5 Dr. Salt\nGlug Glug, Yum!", drinkDispensingMessage);
    }

    @Test
    public void vending_machine_returns_gum_dispensing_message() {
        VendingMachineItem gumItem = new VendingMachineItem("D1", "U-Chews", "0.85", "Gum");
        String gumDispensingMessage = gumItem.toString();
        Assert.assertEquals("$0.85 U-Chews\nChew Chew, Yum!", gumDispensingMessage);

    }

    @Test
    public void sell_item_decreases_item_amount_by_one() {
        VendingMachineItem vendingMachineItem = new VendingMachineItem("D1", "U-Chews", "0.85", "Gum");
        vendingMachineItem.sellItem();
        Assert.assertEquals(4, vendingMachineItem.getItemAmount());

    }

    @Test
    public void sold_out_item_does_not_decrease_item_amount() {
        VendingMachineItem vendingMachineItem = new VendingMachineItem("D1", "U-Chews", "0.85", "Gum");
        vendingMachineItem.sellItem();
        vendingMachineItem.sellItem();
        vendingMachineItem.sellItem();
        vendingMachineItem.sellItem();
        vendingMachineItem.sellItem();
        Assert.assertEquals(0, vendingMachineItem.getItemAmount());

    }
}
