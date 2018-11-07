package ParseObjects;

import java.util.Dictionary;
import java.util.Vector;

public class Date {

    /**
     *
     * @param s- 05 May/ May 05 / May 2000 / 2000 May /
     * @return
     */
    public static Vector<String> Parse(String s){
        String [] date=s.split(" ");
        String month;
        String num;
        String s1="";
        if(Character.isDigit(date[0].charAt(0))){//num- month
            num=date[0];
            month=date[1];
        }
        else{//month-num
            month=date[0];
            num=date[1];
        }
        if(isYear(num)){
            s1=num+"-"+getMonthNumber(month);
        }
        else{
            s1=getMonthNumber(month)+"-"+num;
        }

        Vector<String> toReturn =new Vector<String>();
        toReturn.add(s1);
        toReturn.add(getMonthShort(month));
        return toReturn;
    }

    /**
     *
     * @param num
     * @return true if year number, false if day number- not year
     */
    private static boolean isYear(String num){
       int numInInt=Integer.parseInt(num);
       if(numInInt>31)
           return true;
       return false;
    }

    /**
     *
     * @param month
     * @return the month in number
     */

    private static String getMonthNumber(String month){
        int out=0;
        if(month.equalsIgnoreCase("January")||month.equalsIgnoreCase("Jan")){
            out= 1;
        }
        else if(month.equalsIgnoreCase("February")||month.equalsIgnoreCase("Feb")){
            out= 2;
        }
        else if(month.equalsIgnoreCase("March")||month.equalsIgnoreCase("Mar")){
            out= 3;
        }
        else if(month.equalsIgnoreCase("April")||month.equalsIgnoreCase("Apr")){
            out= 4;
        }
        else if(month.equalsIgnoreCase("May")){
            out= 5;
        }
        else if(month.equalsIgnoreCase("June")||month.equalsIgnoreCase("Jun")){
            out= 6;
        }
        else if(month.equalsIgnoreCase("July")||month.equalsIgnoreCase("Jul")){
            out= 7;
        }
        else if(month.equalsIgnoreCase("August")||month.equalsIgnoreCase("Aug")){
            out= 8;
        }
        else if(month.equalsIgnoreCase("September")||month.equalsIgnoreCase("Sep")){
            out= 9;
        }
        else if(month.equalsIgnoreCase("October")||month.equalsIgnoreCase("Oct")){
            out= 10;
        }
        else if(month.equalsIgnoreCase("November")||month.equalsIgnoreCase("Nov")){
            out= 11;
        }
        else if(month.equalsIgnoreCase("December")||month.equalsIgnoreCase("Dec")){
            out= 12;
        }
        String toreturn=Integer.toString(out);
        if(toreturn.length()==1){
            toreturn="0"+toreturn;
        }
        return toreturn;
    }

    private static String getMonthShort(String month){
        if(month.equalsIgnoreCase("January")||month.equalsIgnoreCase("Jan")){
            return "Jan";
        }
        else if(month.equalsIgnoreCase("February")||month.equalsIgnoreCase("Feb")){
            return "Feb";
        }
        else if(month.equalsIgnoreCase("March")||month.equalsIgnoreCase("Mar")){
            return "Mar";
        }
        else if(month.equalsIgnoreCase("April")||month.equalsIgnoreCase("Apr")){
            return "Apr";
        }
        else if(month.equalsIgnoreCase("May")){
            return "May";
        }
        else if(month.equalsIgnoreCase("June")||month.equalsIgnoreCase("Jun")){
            return "Jun";
        }
        else if(month.equalsIgnoreCase("July")||month.equalsIgnoreCase("Jul")){
            return "Jul";
        }
        else if(month.equalsIgnoreCase("August")||month.equalsIgnoreCase("Aug")){
            return "Aug";
        }
        else if(month.equalsIgnoreCase("September")||month.equalsIgnoreCase("Sep")){
            return "Sep";
        }
        else if(month.equalsIgnoreCase("October")||month.equalsIgnoreCase("Oct")){
            return "Oct";
        }
        else if(month.equalsIgnoreCase("November")||month.equalsIgnoreCase("Nov")){
            return "Nov";
        }
        else if(month.equalsIgnoreCase("December")||month.equalsIgnoreCase("Dec")){
            return "Dec";
        }
        else return "";
    }

}
