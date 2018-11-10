import ParseObjects.*;
import ParseObjects.Number;

import java.util.Vector;

public class Main {
    public static void main(String[] args) {
        Vector<String> out= Between.Parse("Value-added");
        System.out.println(out);

        out= Between.Parse("step-by-step");
        System.out.println(out);

        out= Between.Parse("10-part");
        System.out.println(out);

        out= Between.Parse("6-7");
        System.out.println(out);

        out= Between.Parse("18 24");
        System.out.println(out);


    }

}
