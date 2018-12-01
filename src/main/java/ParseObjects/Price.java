package ParseObjects;

import java.util.Vector;

public class Price {

    public static String Parse(String price){
        String out="";
        try {
            String[] splitPrice = price.split(" ");
            splitPrice[0] = RemoveComas(splitPrice[0]);
            if (splitPrice.length == 1) {//only number!!
                out = toNum(splitPrice[0]);
            } else if (splitPrice.length == 2)//number fraction or number modifier
            {
                String next = splitPrice[1];
                if (Character.isDigit(next.charAt(0))) {//fraction

                    out = toNum(splitPrice[0]);
                    out = out + " " + next; //--> 30, 3/4-> 30 3/4; 30K ,3/4-> 30K 3/4
                } else {//modifier
                    if (splitPrice[1].equalsIgnoreCase("Trillion")) {//cant conver to int
                        out = addmodifier(splitPrice[0], "Million");
                        out.substring(0, out.length() - 5);
                        out = toNum(out);
                        out = out.substring(0, out.length() - 1);
                        out = out + "00000 M";
                    } else if(splitPrice[1].equalsIgnoreCase("billion")||splitPrice[1].equalsIgnoreCase("bn")||splitPrice[1].equalsIgnoreCase("b")){
                        out = addmodifier(splitPrice[0], "Million");
                        out.substring(0, out.length() - 5);
                        out = toNum(out);
                        out = out.substring(0, out.length() - 1);
                        out = out + "000 M";
                    }
                    else {
                        out = addmodifier(splitPrice[0], splitPrice[1]);
                        out = toNum(out);
                    }
                }
            }
            out = out + " Dollars";
            return out;
        }
        catch (Exception e){
            return "";
        }
    }

    /**
     *
     * @param num- the number
     * @param modifier - million, billion...
     * @return the num plus the modifier- 1 million - > 1000000
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

    private static String MabyeInteger(String s){//s is double in string
        int index=s.indexOf(".");
        if(index==s.length()-2&&s.charAt(s.length()-1)=='0'){//1.0
            s=s.substring(0,s.length()-2);
        }
        return s;
    }

    /**
     *
     * @param s
     * @return the string without ","
     */
    private static String RemoveComas(String s){
        StringBuilder sb=new StringBuilder(s);
        int index=sb.indexOf(",");
        while(index>-1){
            sb.deleteCharAt(index);
            index=sb.indexOf(",");
        }
        return sb.toString();
    }


    private static String toNum(String s){
        String out="";
        double num=Double.parseDouble(s);
        if(num<1000000){
            out = Double.toString(num);
            out = MabyeInteger(out);
        }
        else{//bigger than M
            num=num/1000000;
            out=Double.toString(num);
            out=MabyeInteger(out);
            out=out+ "M";
        }

        return out;
    }

}
