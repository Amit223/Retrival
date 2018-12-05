package ParseObjects;
import ADT.HashMapIgnoreCase;
import java.util.Vector;
/**
 * this static class parse String using {@link #Parse(String)} to Date term form:
 * "DD Month" => MM-DD
 * "Month DD" => MM-DD
 * "MONTH YYYY" => YYYY-MM, Number
 * ***Our Extra: we add the {@link Date} term to the _termList as one term, and the year as {@link Number} term.
 * that's in order to retrieve in the next part documents that include also this Number or if someone want to retrieve.
 *
 */
public class Date {
    private static final HashMapIgnoreCase _months;

    /**
     * static constructor for the {@link #_months}
     */
    static {
        _months = new HashMapIgnoreCase();
        _months.put("January", 1);
        _months.put("Jan", 1);
        _months.put("February", 2);
        _months.put("Feb", 2);
        _months.put("March", 3);
        _months.put("Mar", 3);
        _months.put("April", 4);
        _months.put("Apr", 4);
        _months.put("May", 5);
        _months.put("June", 6);
        _months.put("Jun", 6);
        _months.put("July", 7);
        _months.put("Jul", 7);
        _months.put("August", 8);
        _months.put("Aug", 8);
        _months.put("September", 9);
        _months.put("Sep", 9);
        _months.put("October", 10);
        _months.put("Oct", 10);
        _months.put("November", 11);
        _months.put("Nov", 11);
        _months.put("December", 12);
        _months.put("Dec", 12);
    }

    /**
     * ***Our Extra: we add the {@link Date} term to the _termList as one term, and the year as {@link Number} term.
     * that's in order to retrieve in the next part documents that include also this Number or if someone want to retrieve.
     * "DD Month" => MM-DD
     * "Month DD" => MM-DD
     * "Month YYYY" => YYYY-MM, NUMBER
     * @param s- 05 May/ May 05 / May 2000 / 2000 May /
     * @return ans - vector that include the Date Term and if it's year so the year as Number term also.
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

    /**helpful private functions to the  {@link #Parse(String)}**/

    /**
     * *** Our Guess: year is num>31
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
        out = _months.get(month);
        String toreturn = Integer.toString(out);
        if (toreturn.length() == 1) {
            toreturn = "0" + toreturn;
        }
        return toreturn;
    }

    /**
     * helpful static function to the parser.
     * @param month
     * @return true if its month.
     */
    public static boolean isMonth(String month){
        return _months.containsKey(month);
    }
}