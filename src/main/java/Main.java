import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        String toLower="A".toLowerCase();
        String notS="acq";
        if(toLower.compareTo(notS)>0)
            System.out.println("yay");
    }
}

