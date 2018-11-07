package ParseObjects;

import java.util.Vector;

public class Price {

    public static Vector <String> Parse(String price){
        Vector<String> toReturn =new Vector<String>();
        String out="";
        String [] splitPrice=price.split(" ");
        splitPrice[0]=RemoveComas(splitPrice[0]);
        if(splitPrice.length==1){//only number!!
            out=toNum(splitPrice[0]);
        }
        else if(splitPrice.length==2)//number fraction or number modifier
        {
            String next=splitPrice[1];
            if(Character.isDigit(next.charAt(0))){//fraction

                out=toNum(splitPrice[0]);
                out=out + " "+ next; //--> 30, 3/4-> 30 3/4; 30K ,3/4-> 30K 3/4
            }
            else{//modifier
                out=addmodifier(splitPrice[0],splitPrice[1]);
                out=toNum(out);
            }
        }
        else if(splitPrice.length==3) //number fraction modifier--unknown
        {

        }
        out=out+ " Dollars";
        toReturn.add(out);
        return toReturn;
    }

    /**
     *
     * @param num- the number
     * @param modifier - million, billion...
     * @return the num plus the modifier- 1 million - > 1000000
     */
    private static String addmodifier(String num, String modifier) {
        String out=num;
        double number=Double.parseDouble(num);
        if(modifier.equalsIgnoreCase("Thousand")){
            number=number*1000;
            out=Double.toString(number);
        }
        else if(modifier.equalsIgnoreCase("Million")||modifier.equalsIgnoreCase("m")){
            number=number*1000000;
            out=Double.toString(number);
        }
        else if(modifier.equalsIgnoreCase("Billion")|modifier.equalsIgnoreCase("bn")||modifier.equalsIgnoreCase("b")){
            number=number*1000000000;
            out=Double.toString(number);
            out=out+"000000000";
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
            out=out+ " M";
        }

        return out;
    }

}
