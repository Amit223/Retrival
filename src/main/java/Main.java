import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        String s = ReadFile.readStopWords("C:\\Users\\liadber\\Downloads\\אחזור");
        StopWords.setStopwords(s);
        Parser p = new Parser();
        p.Parse("12 bn u.s. dollars", true, "Paris");
        p.printTermList();

    }
}

