import ParseObjects.Date;
import ParseObjects.Number;
import ParseObjects.Percentage;
import ParseObjects.Price;

import java.util.Vector;

public class Main {
    public static void main(String[] args) {
        Vector<String> out= Date.Parse("14 MAY");
        System.out.println(out);

        out= Date.Parse("14 May");
        System.out.println(out);

        out= Date.Parse("June 4");
        System.out.println(out);

        out= Date.Parse("JUNE 4");
        System.out.println(out);

        out= Date.Parse("Sep 1994");
        System.out.println(out);

        out= Date.Parse("October 1994");
        System.out.println(out);

    }

}
