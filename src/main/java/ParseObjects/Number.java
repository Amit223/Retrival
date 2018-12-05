package ParseObjects;
/**
 * this static class parse String to Number term form using {@link #Parse(String)}:
 * "Number"<1000 =>Number Modifier(K/M/B/T)/Fraction
 * "Number">1000 =>Number Modifier(K/M/B/T)/Fraction
 * "Number modifier" {modifier=Thousand,Million,Billion,Trillion,m,bn}=>Number Modifier(K/M/B/T)
 * ***Our Extra: we can recognize bn, m as modifier and parse it and we can recognize  fractions for number that bigger than 1000 if they dont have a modifier.
 * we decide to parse also divided form of Fraction to double form in Order to recognize same Numbers such: "10 3/4" and "10.75" to "10.75"
 * and retrieve the same documents to same numbers.
 */
public class Number {

    /**
     * parse String to Number Term form:
     * "Number"<1000 =>Number Modifier(K/M/B/T)/Fraction
     * "Number">1000 =>Number Modifier(K/M/B/T)/Fraction
     * "Number modifier" {modifier=Thousand,Million,Billion,Trillion,m,bn}=>Number Modifier(K/M/B/T)
     * ***Our Extra: we can recognize bn, m as modifier and parse it and we can recognize  fractions for number that bigger than 1000 if they dont have a modifier.
     * we decide to parse also divided form of Fraction to double form in Order to recognize same Numbers such: "10 3/4" and "10.75" to "10.75"
     * and retrieve the same documents to same numbers.
     * @param number-string represent full number- 1,000/ 1 Million  / 10.35 / 1 Thousends  / 1 Billion / 10 3/4  /10 3/4 Thousands
     * @return the number parsed by rules
     */
    public static String Parse(String number) {
        String out = "";
        try {
            String[] splitNum = number.split(" ");
            splitNum[0] = RemoveComas(splitNum[0]);
            if (splitNum.length == 1) {//only number!!
                if (splitNum[0].contains("/")) {
                    out = splitNum[0];
                } else
                    out = toNum(splitNum[0]);
            } else if (splitNum.length == 2)//number fraction or number modifier
            {
                String next = splitNum[1];
                if (Character.isDigit(next.charAt(0))) {//fraction
                    out = toNum(splitNum[0],next);
                    out = out; //--> 30, 3/4-> 30.75 ; 30.0000075K

                } else {//modifier
                    if (splitNum[1].equalsIgnoreCase("Trillion")) {//cant conver to int
                        out = addmodifier(splitNum[0], "Million");
                        out.substring(0, out.length() - 5);
                        out = toNum(out);
                        out = out.substring(0, out.length() - 1);
                        out = out + "00B";
                    } else if(splitNum[1].equalsIgnoreCase("Billion")||splitNum[1].equalsIgnoreCase("bn")||splitNum[1].equalsIgnoreCase("b")){
                        out = addmodifier(splitNum[0], "Million");
                        out.substring(0, out.length() - 5);
                        out = toNum(out);
                        out = out.substring(0, out.length() - 1);
                        out = out + "B";
                    }
                    else{//K or M
                        out = addmodifier(splitNum[0], splitNum[1]);
                        out = toNum(out);
                    }

                }
            }
            return out;
        }
        catch (Exception e){
            return number;
        }
    }

    /**helpful functions to the {@link #Parse(String)} **/

    /**
     *
     * @param num- the number
     * @param modifier - million, billion...
     * @return the num plus the modifier- "1 million" => "1000000"
     */
    private static String addmodifier(String num, String modifier) {
        String out=num;
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
     * help to more classes.
     * @param number- single number in for
     * @return the number
     */
    public static String toNum(String number){
        return toNum(number,"");
    }

    /**
     *
     * @param number
     * @param fraction- if exist, else ""
     * @return the number in string
     */
    private static String toNum(String number, String fraction) {
        String out="";
        double frac=0;
        try {
            if(fraction!=null && !fraction.equals("")){
                String [] fracParts= fraction.split("/");
                double x=Double.parseDouble(fracParts[0]);
                double y=Double.parseDouble(fracParts[1]);
                frac=x/y;
            }
            double num=Double.parseDouble(number)+frac;
            if (num < 1000) {//small
                out = Double.toString(num);
                out = MabyeInteger(out);

            } else if (num < 1000000) {//Thousands
                num = num / 1000;
                out = Double.toString(num);
                out = MabyeInteger(out);
                out = out + "K";
            } else if (num < 1000000000)//Millions
            {
                num = num / 1000000;
                out = Double.toString(num);
                out = MabyeInteger(out);
                out = out + "M";
            } else {//Billions or trillions!
                num = num / 1000000000;
                out = Double.toString(num);
                out = MabyeInteger(out);
                out = out + "B";
            }
        }
        catch (Exception e){
        }
        return out;
    }

    /**
     * {@link Price#MabyeInteger(String)}
     * this method remove from the string ".0" if its Integer.
     * @param s
     * @return s without ".0" if its Integer.
     */
    private static String MabyeInteger(String s){//s is double in string
        int index=s.indexOf(".");
        if(index==s.length()-2&&s.charAt(s.length()-1)=='0'){//1.0
            s=s.substring(0,s.length()-2);
        }
        return s;
    }

    /**
     *@see {@link Price#RemoveComas(String)}
     * @param s
     * @return the string without ","
     */
    public static String RemoveComas(String s){
        StringBuilder sb=new StringBuilder(s);
        int index=sb.indexOf(",");
        while(index>-1){
            sb.deleteCharAt(index);
            index=sb.indexOf(",");
        }
        return sb.toString();
    }

    /**helpful functions to the sort part in the #Parser**/

    /**
     * @param termS - _token in string.
     * @return true - if the string is numeric. else false
     */
    public static boolean isNumeric(String termS) {
        try {
            termS = Number.RemoveComas(termS);
            Double.parseDouble(termS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


