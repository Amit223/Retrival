import ParseObjects.*;
import ParseObjects.Number;

import java.util.Timer;
import java.util.Vector;

public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        ReadFile readFile=new ReadFile();
        readFile.read("C:\\Users\\AMIT MOSHE\\Desktop\\אוניברסיטה\\סמסטר ה\\אחזור\\corpus\\");
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        duration=duration/1000000;
        System.out.println(duration);

    }

}
