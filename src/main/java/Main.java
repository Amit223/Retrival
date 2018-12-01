import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {


        //System.out.println(Distance.Parse("10 3/4 km"));
        /**Parser parser = new Parser();
        long startTime = System.nanoTime();
        parser.Parse("14 million dollars", true, "paris");
        long endTime = System.nanoTime();
        System.out.println("Took "+(endTime - startTime) + " ns");
        parser.printTermList();**/
        String theToken="percentage";
        PorterStemmer _stemmer = new PorterStemmer();
            _stemmer.add("java".toCharArray(), 4);
            _stemmer.stem();
        _stemmer.add(("python").toCharArray(), 6);
        _stemmer.stem();
        System.out.println(_stemmer.toString());


        System.out.println(Price.Parse("15 thousand"));
        System.out.println(Number.Parse("15 trillion"));
    }
}