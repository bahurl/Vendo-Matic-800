package com.techelevator.view;

import com.techelevator.VendingMachineCLI;
import org.junit.Test;

public class VendingMachineCLITest {
    @Test
    public void feed_money_increases_balance(){
        VendingMachineCLI vendingMachineCLI = new VendingMachineCLI(new Menu(System.in, System.out));
        vendingMachineCLI.feedMoney();




    }

}
