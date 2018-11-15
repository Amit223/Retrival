import ParseObjects.*;
import ParseObjects.Number;

import java.util.Timer;
import java.util.Vector;

public class Main {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        ReadFile readFile=new ReadFile();
        readFile.read("C:\\Users\\AMIT MOSHE\\Desktop\\אוניברסיטה\\סמסטר ה\\אחזור\\corpus\\FB396001");
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        duration=duration/1000000000;
        System.out.println(duration);
        System.out.println(ReadFile.getDocs().toString());

    }

}
