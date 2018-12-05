import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
       Vector<String> vector=new Vector<>();
        vector.add("a");
        vector.add("b");
        vector.add("c");
        String s=vector.toString();
        String[] values=s.split(",");
        vector=new Vector();
        vector.add(values[0].substring(1,values[0].length()));//the country
        vector.add(values[1]);//the coin
        vector.add(values[2].substring(0,values[2].length()-1));//population
    }
}

