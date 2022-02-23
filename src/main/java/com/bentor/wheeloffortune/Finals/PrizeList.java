package com.bentor.wheeloffortune.Finals;

import com.bentor.wheeloffortune.Classes.Prize;

import java.util.ArrayList;

public class PrizeList {

    public static ArrayList<Prize> prizeInitialize(){
        ArrayList<Prize> prizeList = new ArrayList<>();
        Prize fiveThousand = new Prize(5000, '-');
        Prize tenThousand = new Prize(10000, '-');
        Prize twentyFiveThousand = new Prize(25000, '-');
        Prize fiftyThousand = new Prize(50000, '-');
        Prize hundredThousand = new Prize(100000, '-');
        Prize twoHundredAndFiftyThousand = new Prize(250000, '-');
        Prize fiveHundredThousand = new Prize(500000, '-');
        Prize million = new Prize(1000000, '-');
        Prize bankrupt = new Prize(0, 'b');
        Prize looseHalf = new Prize(0, 'h');
        Prize doubleMoney = new Prize(0, 'd');
        Prize buyLetter = new Prize(0, 'l');
        Prize buyTeamOut = new Prize(0, 'o');
        Prize nothing = new Prize(0, 'n');
        prizeList.add(fiveThousand);
        prizeList.add(fiveThousand);
        prizeList.add(fiveThousand);
        prizeList.add(fiveThousand);
        prizeList.add(tenThousand);
        prizeList.add(tenThousand);
        prizeList.add(tenThousand);
        prizeList.add(tenThousand);
        prizeList.add(twentyFiveThousand);
        prizeList.add(twentyFiveThousand);
        prizeList.add(twentyFiveThousand);
        prizeList.add(fiftyThousand);
        prizeList.add(fiftyThousand);
        prizeList.add(fiftyThousand);
        prizeList.add(hundredThousand);
        prizeList.add(hundredThousand);
        prizeList.add(twoHundredAndFiftyThousand);
        prizeList.add(fiveHundredThousand);
        prizeList.add(million);
        prizeList.add(bankrupt);
        prizeList.add(looseHalf);
        prizeList.add(looseHalf);
        prizeList.add(doubleMoney);
        prizeList.add(doubleMoney);
        prizeList.add(buyLetter);
        prizeList.add(buyLetter);
        prizeList.add(buyTeamOut);
        prizeList.add(buyTeamOut);
        prizeList.add(nothing);
        return prizeList;
    }

}
