import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {


        Parser parser = new Parser();
        parser.ParseSentence("-15 3/4");
        parser.printTermList();
    }
}