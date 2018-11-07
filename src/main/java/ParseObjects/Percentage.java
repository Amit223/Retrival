package ParseObjects;

import java.util.Vector;

public class Percentage {
    public static Vector<String> Parse(String percent){
        Vector out=Number.Parse(percent);
        String newOut=out.get(0)+"%";
        out.set(0,newOut);
        return out;
    }
}
