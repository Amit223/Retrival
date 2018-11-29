import ParseObjects.Distances;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

public class Main {
    public static void main(String[] args) {
       // Test2();
        
    }



    public static void Test2(){
        String dis="10,000.3 km";
        System.out.println(Distances.Parse(dis));
    }
}
