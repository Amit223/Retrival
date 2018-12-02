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
     * @param nextTerm
     * @param nextnextTerm
     * @return true if next _token is percent || percentage
     * the nextterm= per and nextnextterm= cent.
     */
    public static boolean isPercent(String nextTerm, String nextnextTerm) {
        if (nextTerm.equalsIgnoreCase("Percent")
                || nextTerm.equalsIgnoreCase("Percentage")) {
            return true;
        } else if (nextTerm.equalsIgnoreCase("per")  //use the gusses we cut the puncuation in the end.
                && nextnextTerm.equalsIgnoreCase("cent")) {
            return true;
        } else {
            return false;
        }
    }

}
