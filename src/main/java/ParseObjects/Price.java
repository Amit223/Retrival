package ParseObjects;

public class Price {
    /**
     * todo check if milionBilion is stick to the number.
     * todo check if $ in the begining of the number.
     * todo. sent to this function if the word dollars='dollars/$'. and sent more 3 words earlier.
     * todo. sent to this function if the word contains $. in price without $.

     * @param price - only number, 1.7320, 22 3/4, 450,000
     * - price that inculde all the strings that describe the price
     * @param milionBilion: m, bn, billion, million, trillion.
     * @param uS: U.S.
     * @param dollars - Dollars/dollar/$
     * @return priceAfterParse - Number M/"" Dollars. for example: 1.7320 Dollars, 1 M Dollars
     */
    public static String Parse(String price, String milionBilion, String uS, String dollars){
        String priceAfterParse="";

        if(!uS.equals("U.S."));
        return priceAfterParse;
    }
}
