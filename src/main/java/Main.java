import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class Main {
    public static void main(String[] args) {


        //System.out.println(Distance.Parse("10 3/4 km"));
        Parser parser = new Parser();
        long startTime = System.nanoTime();
        parser.Parse("study", true, "paris");
        long endTime = System.nanoTime();
        System.out.println("Took "+(endTime - startTime) + " ns");
        parser.printTermList();
    }
}