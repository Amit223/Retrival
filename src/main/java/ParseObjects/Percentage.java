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
}
