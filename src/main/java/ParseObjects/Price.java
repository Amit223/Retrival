package ParseObjects;

import java.util.Vector;

/**
 * this static class parse String to Price term form using {@link #Parse(String)}:
 * "{@link Number} dollars/u.s. dollars"  => {@link Number} dollars
 * "${@link Number}"  => {@link Number} dollars
 * ***Our Extra: we add that we can recognize and parse:
 * "$number modifier/fraction" => {@link Number} dollars
 * "number modifier/fraction dollars/u.s. dollars" => {@link Number} dollars
 * so we can retrieve that's form also.
 */
public class Price {

    /**
     * "{@link Number} dollars/u.s. dollars"  => {@link Number} dollars
     * "${@link Number}"  => {@link Number} dollars
     * ***Our Extra: we add that we can recognize and parse:
     * "$number modifier/fraction" => {@link Number} dollars
     * "number modifier/fraction dollars/u.s. dollars" => {@link Number} dollars
     * so we can retrieve that's form also.
     *
     * @param price
     * @return price after parse.
     */
    public static String Parse(String price) {
        String out = "";
        try {
            String[] splitPrice = price.split(" ");
            splitPrice[0] = RemoveComas(splitPrice[0]);
            if (splitPrice.length == 1) {//only number!!
                out = toNum(splitPrice[0]);
            } else if (splitPrice.length == 2)//number fraction or number modifier
            {
                String next = splitPrice[1];
                if (Character.isDigit(next.charAt(0))) {//fraction

                    out = toNum(splitPrice[0], next);
                    out = out; //--> 30, 3/4-> 30 3/4; 30K ,3/4-> 30K 3/4
                } else {//modifier
                    if (splitPrice[1].equalsIgnoreCase("Trillion")) {//cant conver to int
                        out = addmodifier(splitPrice[0], "Million");
                        out.substring(0, out.length() - 5);
                        out = toNum(out);
                        out = out.substring(0, out.length() - 1);
                        out = out + "00000 M";
                    } else if (splitPrice[1].equalsIgnoreCase("billion") || splitPrice[1].equalsIgnoreCase("bn") || splitPrice[1].equalsIgnoreCase("b")) {
                        out = addmodifier(splitPrice[0], "Million");
                        out.substring(0, out.length() - 5);
                        out = toNum(out);
                        out = out.substring(0, out.length() - 1);
                        out = out + "000 M";
                    } else {
                        out = addmodifier(splitPrice[0], splitPrice[1]);
                        out = toNum(out);
                    }
                }
            }
            out = out + " Dollars";
            return out;
        } catch (Exception e) {
            return "";
        }
    }

    /**helpful functions to the {@link #Parse(String)} **/

    /**
     * @param num-     the number
     * @param modifier - million, billion...
     * @return the num plus the modifier- 1 million - > 1000000
     */
    private static String addmodifier(String num, String modifier) {
        String out = num;
        double number = Double.parseDouble(num);
        if (modifier.equalsIgnoreCase("Thousand")) {
            number = number * 1000;
            out = Double.toString(number);
        } else if (modifier.equalsIgnoreCase("Million") || modifier.equalsIgnoreCase("m")) {
            number = number * 1000000;
            out = Double.toString(number);
        }
        return out;

    }

    /**
     * @param s - double in string
     * @return if its integer cut the ".0"
     * @see {@link Number#MabyeInteger(String)}
     */
    private static String MabyeInteger(String s) {//s is double in string
        int index = s.indexOf(".");
        if (index == s.length() - 2 && s.charAt(s.length() - 1) == '0') {//1.0
            s = s.substring(0, s.length() - 2);
        }
        return s;
    }

    /**
     * @see {@link Number#RemoveComas(String)}
     * @param s
     * @return the string without ","
     */
    private static String RemoveComas(String s) {
        StringBuilder sb = new StringBuilder(s);
        int index = sb.indexOf(",");
        while (index > -1) {
            sb.deleteCharAt(index);
            index = sb.indexOf(",");
        }
        return sb.toString();
    }

    /**
     * @see {@link Number#toNum(String)}
     * @param number- single number in for
     * @return the number
     */
    private static String toNum(String number) {
        return toNum(number, "");
    }

    /**
     * @see {@link Number#toNum(String, String)}
     * @param number
     * @param fraction
     * @return
     */
    private static String toNum(String number, String fraction) {
        String out = "";
        double frac = 0;
        if (fraction != null && !fraction.equals("")) {
            String[] fracParts = fraction.split("/");
            double x = Double.parseDouble(fracParts[0]);
            double y = Double.parseDouble(fracParts[1]);
            frac = x / y;
        }
        double num = Double.parseDouble(number) + frac;
        if (num < 1000000) {
            out = Double.toString(num);
            out = MabyeInteger(out);
        } else {//bigger than M
            num = num / 1000000;
            out = Double.toString(num);
            out = MabyeInteger(out);
            out = out + "M";
        }

        return out;
    }

    /**helpful functions to the sort part in the #Parser**/

    /**
     * helpful function for the #Parser
     * @param nextTerm
     * @param nextnextTerm
     * @return true if next _token is dollars ||
     * the nextterm= u.s and nextnextterm= dollars.
     */
    public static boolean isPrice(String nextTerm, String nextnextTerm) {
        if (nextTerm.equalsIgnoreCase("dollars")) {
            return true;
        } else if (nextTerm.equalsIgnoreCase("u.s")  //use the gusses we cut the puncuation in the end.
                && nextnextTerm.equalsIgnoreCase("dollars")) {
            return true;
        } else {
            return false;
        }
    }

}
