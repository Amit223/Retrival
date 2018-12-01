import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {


        System.out.println(Price.Parse("15 thousand"));
        System.out.println(Number.Parse("15 trillion"));
    }
}