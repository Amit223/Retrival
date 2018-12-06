import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;
import javafx.util.Pair;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        Pair<Integer,Integer> t=new Pair<>(5,20);
        String s=t.toString();
        System.out.println(s);
    }
}

