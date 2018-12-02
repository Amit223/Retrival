package ParseObjects;
import ADT.HashMapIgnoreCase;

import javax.print.DocFlavor;
import java.util.Vector;

public class Date {
    private static final HashMapIgnoreCase months;

    static {
        months = new HashMapIgnoreCase();
        months.put("January", 1);
        months.put("Jan", 1);
        months.put("February", 2);
        months.put("Feb", 2);
        months.put("March", 3);
        months.put("Mar", 3);
        months.put("April", 4);
        months.put("Apr", 4);
        months.put("May", 5);
        months.put("June", 6);
        months.put("Jun", 6);
        months.put("July", 7);
        months.put("Jul", 7);
        months.put("August", 8);
        months.put("Aug", 8);
        months.put("September", 9);
        months.put("Sep", 9);
        months.put("October", 10);
        months.put("Oct", 10);
        months.put("November", 11);
        months.put("Nov", 11);
        months.put("December", 12);
        months.put("Dec", 12);
    }

    /**
     * @param s- 05 May/ May 05 / May 2000 / 2000 May /
     * @return
     */
    public static Vector<String> Parse(String s) {
        String[] date = s.split(" ");
        String month;
        String num;
        String s1 = "";
        Vector<String>ans= new Vector<>();
        try {
            if (Character.isDigit(date[0].charAt(0))) {//num- month
                num = date[0];
                month = date[1];
            } else {//month-num
                month = date[0];
                num = date[1];
            }
            if (isYear(num)) {
                s1 = num + "-" + getMonthNumber(month);
                ans.add(Number.Parse(num));
            } else {
                s1 = getMonthNumber(month) + "-" + num;
            }
            ans.add(s1);

            return ans;
        } catch (Exception e) {
            return ans;
        }
    }

    /**
     * @param num
     * @return true if year number, false if day number- not year
     */
    private static boolean isYear(String num) {
        int numInInt = Integer.parseInt(num);
        if (numInInt > 31)
            return true;
        return false;
    }

    /**
     * @param month
     * @return the month in number
     */

    private static String getMonthNumber(String month) {
        int out = 0;
        out = months.get(month);
        String toreturn = Integer.toString(out);
        if (toreturn.length() == 1) {
            toreturn = "0" + toreturn;
        }
        return toreturn;
    }

    /**
     * @param month
     * @return true if its month.
     */
    public static boolean isMonth(String month){
        return months.containsKey(month);
    }
}