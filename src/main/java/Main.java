import ParseObjects.Between;
import ParseObjects.Distances;
import ParseObjects.Number;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class Main {
    public static void main(String[] args) {
       // Test2();
       // Test();
       // Test3();

        Test4();
        
    }

    private static void Test4() {
        Indexer indexer=new Indexer();
        String details=indexer.getCityDetails("los");
        String[] strings=details.split("-");
        System.out.println(Arrays.toString(strings));
    }

    private static void Test3() {
        ReadFile readFile=new ReadFile();
        String s=readFile.readStopWords("C:\\Users\\AMIT MOSHE\\Desktop\\אוניברסיטה\\סמסטר ה\\אחזור\\test");
        System.out.println(s);
    }

    private static void Test() {
        Vector <String> s= Between.Parse("13 million-12 million");
        System.out.println(s.toString());
    }


    public static void Test2(){
        String dis="10,000.3 km";
        System.out.println(Distances.Parse(dis));
    }
}
