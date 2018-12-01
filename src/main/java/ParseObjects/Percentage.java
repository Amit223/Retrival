package ParseObjects;

import java.util.Vector;

public class Percentage {
    public static String Parse(String percent){
        String out=Number.Parse(percent);
        if(out.equals("")){//parse didnt work
            return "";
        }
        String newOut=out+"%";
        return newOut;
    }

    /**
     * @param nextTerm - check if its percent or precentage.
     * @return
     */
    public static boolean isPercent(String nextTerm) {
        if (nextTerm.equalsIgnoreCase("Percent")
                || nextTerm.equalsIgnoreCase("Percentage"))
            return true;
        return false;
    }
}
