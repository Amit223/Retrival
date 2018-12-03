import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        try {
            File f = new File("Downloads/ktrue.txt");
            f.createNewFile();

            RandomAccessFile raf = new RandomAccessFile(f, "r");
            int lineNumPosting = (int) raf.length() / 12;
            raf.seek(raf.length());
            raf.writeChar('c');
        }
        catch (Exception e){
            System.out.println("hi");
        }
    }

}