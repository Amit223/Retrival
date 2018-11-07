package ParseObjects;

import java.util.Vector;

public class Number {

    /**
     *
     * @param number-string represent full number- 1,000/ 1 Million  / 10.35 / 1 Thousends  / 1 Billion / 10 3/4  /10 3/4 Thousands
     * @return the number parsed by rules
     */
    public static Vector<String> Parse(String number){
        Vector<String> toReturn =new Vector<String>();
        String out="";
        String [] splitNum=number.split(" ");
        splitNum[0]=RemoveComas(splitNum[0]);
        if(splitNum.length==1){//only number!!
            out=toNum(splitNum[0]);
        }
        else if(splitNum.length==2)//number fraction or number modifier
        {
            String next=splitNum[1];
            if(Character.isDigit(next.charAt(0))){//fraction
                out=toNum(splitNum[0]);
                out=out + " "+ next; //--> 30, 3/4-> 30 3/4; 30K ,3/4-> 30K 3/4

            }
            else{//modifier
                if(splitNum[1].equalsIgnoreCase("Trillion")){//cant conver to int
                    out=addmodifier(splitNum[0],"Million");
                    out.substring(0,out.length()-5);
                    out=toNum(out);
                    out=out.substring(0,out.length()-1);
                    out=out+"00B";
                }
                else{
                    out=addmodifier(splitNum[0],splitNum[1]);
                    out=toNum(out);
                }

            }
        }
        else if(splitNum.length==3) //number fraction modifier
        {

        }
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
        if(modifier.equalsIgnoreCase("Thousand")){
            out=out+"000";
        }
        else if(modifier.equalsIgnoreCase("Million")||modifier.equalsIgnoreCase("m")){
            out=out+"000000";
        }
        else if(modifier.equalsIgnoreCase("Billion")|modifier.equalsIgnoreCase("bn")||modifier.equalsIgnoreCase("b")){
            out=out+"000000000";
        }
        return out;

    }

    /**
     *
     * @param s- single number in for
     * @return
     */
    private static String toNum(String s) {
        String out="";
        try {
            double num=Double.parseDouble(s);
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
            System.out.println("hi");
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
}
